
import org.apache.commons.digester.xmlrules.DigesterLoader
import org.apache.commons.digester.ObjectCreationFactory
import org.xml.sax.Attributes
import groovy.io.FileType
import groovy.util.logging.Log4j
import groovy.xml.*
import java.util.regex.*

//@Grab('com.hdsupply:hds-groovy:3.0')
@groovy.transform.BaseScript(hds.groovy.ScriptWrapper)
import hds.groovy.*

//addStringMethods() 

File dir = new File(this.getClass().protectionDomain.codeSource.location.path).parentFile.parentFile
File mapDir = new File(dir, 'map')
File inDir = new File(dir, 'datain')
File outDir = new File(dir, 'dataout')

// ==========================================================================

def dr = new DataReader(mapDir)

inDir.eachFileMatch(FileType.FILES, ~/.+\.xml/) { xmlFile ->
    dr.summary(xmlFile) { summary, batchMap ->
        DataWriter dw = new DataWriter()
        dw.outFile = new File(outDir, xmlFile.name - '.xml' + '.dat')
        dw.summary = summary
        dw.batchMap = batchMap
        dw.write_header()
        dr.eachInvoice() { invoice, items, taxes, charges ->
            dw.invoice = invoice
            dw.items = items
            dw.taxes = taxes
            dw.charges = charges
            dw.write_invoice()
        }
        dw.write_trailer()
    }
}

// ==========================================================================

import groovy.util.Node
import org.apache.commons.digester.Digester
import org.apache.commons.digester.xmlrules.DigesterLoader

class DataReader {
    List detailPaths = ['invoice.item', 'invoice.tax', 'invoice.charge']
    XmlParser xp = new XmlParser()
    Node root
    File mapDir
    File digesterFile
    Digester digester
    Map summary

    DataReader(File dir) {
        mapDir = dir
        digesterFile = new File(mapDir, 'map-digester-rules.xml')
        digester = DigesterLoader.createDigester(digesterFile.toURI().toURL())
    }

    void summary (File file, Closure closure) {
        xp = new XmlParser()
        root = xp.parse(file)
        Integer invcnt = 0
        Integer itmcnt = 0
        Integer chgcnt = 0
        Integer frtcnt = 0
        Integer savcnt = 0
        Integer taxcnt = 0
        BigDecimal itmamt = BigDecimal.ZERO
        BigDecimal chgamt = BigDecimal.ZERO
        BigDecimal frtamt = BigDecimal.ZERO
        BigDecimal savamt = BigDecimal.ZERO
        BigDecimal taxamt = BigDecimal.ZERO
        summary = [:]
        root.findAll {it.name() != 'invoice'}.each { Node i ->
            i.depthFirst().each { Node node ->
                NodeList children = node.children()
                Node n = node
                List pathlist = []
                while (n != root) {
                    pathlist << n.name()
                    n = n.parent()
                }
                pathlist = pathlist.reverse()
                String path = pathlist.join('.')
                if (children.size() == 0) {
                    summary[path] = ''
                }
                else if (children.size() == 1 && children[0] instanceof String) {
                    summary[path] = node.text()
                }
            }
        }
        //summary.each {k,v-> println "$k=$v"}
        root.invoice.each { invoice ->
            invcnt++
            invoice.item.each {item ->
                itmcnt++
                itmamt += item.amt.text().to_BigDecimal()
            }
            invoice.charge.each {charge ->
                chgcnt++
                BigDecimal amt = charge.amt.text().to_BigDecimal()
                chgamt += amt
                String category = charge.category.text()
                if (category == 'FRT') {
                    frtcnt++
                    frtamt += amt
                }
                else if (category == 'SAV') {
                    savcnt++
                    savamt += amt
                }
            }
            invoice.tax.each {tax ->
                taxcnt++
                taxamt += tax.amt.text().to_BigDecimal()
            }
        }
        summary.INVCNT = invcnt.toString()
        summary.ITMCNT = itmcnt.toString()
        summary.TAXCNT = taxcnt.toString()
        summary.CHGCNT = chgcnt.toString()
        summary.FRTCNT = frtcnt.toString()
        summary.SAVCNT = savcnt.toString()
        summary.MSCCNT = (chgcnt - frtcnt - savcnt).toString()
        summary.ITMAMT = itmamt.toString()
        summary.TAXAMT = taxamt.toString()
        summary.CHGAMT = chgamt.toString()
        summary.FRTAMT = frtamt.toString()
        summary.SAVAMT = savamt.toString()
        summary.MSCAMT = (chgamt.subtract(frtamt).subtract(savamt)).toString()
        summary.INVAMT = (itmamt.add(chgamt).add(taxamt)).toString()

        String custid = summary['buyer.custid']
        BatchMap bm = (BatchMap) digester.parse(new File(mapDir,"${custid}.xml"))

        closure.call(summary, bm)
    }

    void eachInvoice (Closure closure) {
        root.invoice.each { Node i ->
            Map det
            Map invoice = [:].withDefault{ it = ''}
            detailPaths.each { invoice[it] = []}
            i.depthFirst().each { Node node ->
                if (node == root) {
                    return
                }
                NodeList children = node.children()
                Node n = node
                List pathlist = []
                while (n != root) {
                    pathlist << n.name()
                    n = n.parent()
                }
                pathlist = pathlist.reverse()
                String path = pathlist.join('.')
                if (pathlist.size() == 2) {
                    if (path in detailPaths) {
                        if (!invoice.containsKey(path)) {
                            invoice[path] = []
                        }
                        invoice[path] << (det = [:].withDefault{ it = '' })
                    }
                    else {
                        det = invoice
                    }
                }
                if (children.size() == 0) {
                    det[path] = ''
                }
                else if (children.size() == 1 && children[0] instanceof String) {
                    det[path] = node.text()
                }
            }

            // --------------------------------------------

            invoice.ITMCNT = invoice['invoice.item'].size().toString()
            BigDecimal itemamt = 0
            invoice['invoice.item'].each { item ->
                def amt = item.find { it.key == 'invoice.item.amt' }?.value
                if (amt) {
                    itemamt += amt.toBigDecimal()
                }
            }
            invoice.ITMAMT = itemamt.toString()
            //
            invoice.TAXCNT = invoice['invoice.tax'].size().toString()
            BigDecimal taxamt = 0
            invoice['invoice.tax'].each { tax ->
                def amt = tax.find { it.key == 'invoice.tax.amt' }?.value
                if (amt) {
                    taxamt += amt.toBigDecimal()
                }
            }
            invoice.TAXAMT = taxamt.toString()
            //
            invoice.CHGCNT = invoice['invoice.charge'].size().toString()
            BigDecimal chgamt = 0
            BigDecimal frtamt = 0
            BigDecimal savamt = 0
            BigDecimal mscamt = 0
            invoice['invoice.charge'].each { charge ->
                def amt = charge.find { it.key == 'invoice.charge.amt' }?.value
                if (amt) {
                    chgamt += amt.toBigDecimal()
                    def category = charge.find { it.key == 'invoice.charge.category' }?.value
                    if (category == 'FRT') {
                        frtamt += amt.toBigDecimal()
                    }
                    else if (category == 'SAV') {
                        savamt += amt.toBigDecimal()
                    }
                    else if (category == 'MSC') {
                        mscamt += amt.toBigDecimal()
                    }
                }
            }
            invoice.CHGAMT = chgamt.toString()
            invoice.FRTAMT = frtamt.toString()
            invoice.SAVAMT = savamt.toString()
            invoice.MSCAMT = mscamt.toString()
            invoice.INVAMT = (itemamt + taxamt + chgamt).toString()

            List items = invoice['invoice.item']
            List taxes = invoice['invoice.tax']
            List charges = invoice['invoice.charge']
            detailPaths.each { invoice.remove(it) }

            closure.call(invoice, items, taxes, charges)
        }
    }
}


// ==========================================================================
// com.hds.custominvoice

@Log4j
class DataWriter {
    BatchMap batchMap
    //File mapDir
    //File digesterFile
    File outFile
    Map summary
    Map invoice
    List items
    List taxes
    List charges
    //
    void write_header () {
        writeGroup('batch-header', summary)
    }
    void write_trailer () {
        writeGroup('batch-trailer', summary)
    }
    void write_invoice () {
        writeGroup('invoice-header', summary + invoice)
        items.each { item ->
            writeGroup('invoice-items', summary + invoice + item)
        }
        taxes.each { tax ->
            writeGroup('invoice-taxes', summary + invoice + tax)
        }
        charges.each { charge ->
            writeGroup('invoice-charges', summary + invoice + charge)
        }
        writeGroup('invoice-trailser', summary + invoice)
    }
    void writeGroup (String name, Map data) {
        String fldsep = ','
        String recsep = "\n"
        println "$name  $data"
        Group group = batchMap.getGroupByName(name)
        if (group) {
            group.sequences.each { Sequence sequence ->
                List record = []
                sequence.positions.each { Position position ->
                    String field = ''
                    if (position.source == 'value') { field = position.value }
                    else if (position.source == 'data') { field = data[position.value] }
                    position.macros.each { Macro macro ->
                        println macro
                        field = macro.execute(data, field)
                    }
                    record[position.num-1] = field
                }
                String line = record.collect{(it ?: '').replaceAll(Pattern.quote(fldsep),'')}.join(fldsep) + recsep
                println line
            }
        }
    }
}

// ==========================================================================

@Log4j
class BatchMap {
    String name = ''
    String style = 'text' // maybe xml or json in the future?
    String fldsep = ','
    String recsep = "\n"
    Map groups = [:]
    void addGroup (Group group) {
        log.debug "adding to map: $group"
        groups[group.name] = group
        group.setMap(this)
    }
    Group getGroupByName (String name) {
        groups.containsKey(name) ? groups.get(name) : null
    }
    void setFldsep (String s) {
        fldsep = org.apache.commons.lang.StringEscapeUtils.unescapeJava(s)
    }
    void setRecsep (String s) {
        recsep = org.apache.commons.lang.StringEscapeUtils.unescapeJava(s)
    }
    String toString () {
        String newline = System.getProperty("line.separator")
        StringBuffer buf = new StringBuffer(4096)
        buf.append("batchmap[name=\"").append(name).append("\" style=\"").append(style)
                .append("\" fldsep=\"").append(org.apache.commons.lang.StringEscapeUtils.escapeJava(fldsep))
                .append("\" recsep=\"").append(org.apache.commons.lang.StringEscapeUtils.escapeJava(recsep))
                .append("\"]").append(newline)
        groups.each { k, Group group ->
            buf.append("  ").append(group).append(newline)
            group.sequences.each { Sequence sequence ->
                buf.append("    ").append(sequence).append(newline)
                sequence.positions.each { Position position ->
                    buf.append("      ").append(position).append(newline)
                    position.macros.each { Macro macro ->
                        buf.append("        ").append(macro).append(newline)
                    }
                }
            }
        }
        buf.toString()
    }
}

@Log4j
class Group {
    String name
    BatchMap map // the batch map that this group belongs to
    List sequences = []
    void addSequence (Sequence s) {
        if (s) {
            s.num = sequences.size()
            sequences << s
        }
    }
    String toString () {
        "group[name=${name}]"
    }
}

@Log4j
class Sequence {
    Integer num
    Group group // the group that this sequence belongs to
    List positions = [] // all the positions in this sequence
    void addPosition (Position p) {
        if (p) { positions << p }
    }
    String toString () {
        "sequence[num=${num}]"
    }
}

@Log4j
class Position {
    Integer num
    String value
    String source
    Sequence sequence // the sequence that this position belongs to
    List macros = [] // all the macros that this position will execute
    void addMacro (Macro m) {
        if (m) { macros << m }
    }
    String toString () {
        "position[value='${value}', source='${source}']"
    }
}

@Log4j
class MacroFactory implements ObjectCreationFactory {
    org.apache.commons.digester.Digester digester
    ClassLoader classLoader

    MacroFactory () {
        super()
        classLoader = Thread.currentThread().getContextClassLoader()
        if (classLoader == null) {
            classLoader = getClass().getClassLoader() // fallback
            if (classLoader == null) {
                throw new Exception("cannot get a classLoader")
            }
        }
    }

    @Override
    Object createObject (Attributes attrs) {
        Map map = [:]
        (0 ..< attrs.getLength()).each { i -> map[attrs.getLocalName(i)] = attrs.getValue(i) }
        if (map.name) {
            String className = map.name.split('_').collect { it.capitalize() }.join('')
            try {
				println className
                Class clazz = classLoader.loadClass('ci.' + className)
                try {
                    Object o = clazz.newInstance()
					println o.getClass().getCanonicalName()
					println o.getClass().getName()
					println o.getClass().getDeclaredClasses()
					println o.getClass().getPackage()
					println o instanceof Macro
System.exit(1)
                    Macro macro = (Macro) clazz.newInstance()
                    macro.name = className
                    macro.value = map.value
                    macro.source = map.source ?: 'value'
                    return macro
                }
                catch (InstantiationException e) {
                    throw new Exception("cannot create a Macro from $map [${e.message}]")
                }
            }
            catch (ClassNotFoundException e) {
                throw new Exception("cannot create a Macro from $map [${e.message}]")
            }
        }
        throw new Exception("cannot create macro from $map, macro name is missing")
    }

}

@Log4j
abstract class Macro {
    Position position // the position that this macro belongs to
    String name // macro name
    String value
    String source // 'data' or 'value'
    def obj1
    def obj2
    Map data
    Boolean valid
    Macro () {
        (name, value, source, valid) = ['', '', '', true]
    }
    abstract String execute (Map data, String s)
    String getSourceData (Map data) {
        String s
        if (source.equals('data')) { s = data[value] }
        else if (source.equals('value')) { s = value }
        else { log.error "invalid source [$source] data will be the empty string" }
        s ?: ''
    }
    String toString () {
        "macro[${name}, value=\"${value}\", source=\"${source}\"]"
    }
}

@Log4j
class AbsValue extends Macro {
    String execute (Map m, String sin) {
        data = m
        String sout = sin.to_BigDecimal().abs().toString()
        log.debug "in=[$sin], out=[$sout], " + this
        sout
    }
}

@Log4j
class Add extends Macro {
    String execute (Map data, String sin) {
        String sout = sin.to_BigDecimal().add(getSourceData(sin).to_BigDecimal()).toString()
        log.debug "in=[$sin], out=[$sout], " + this
        sout
    }
}

@Log4j
class Append extends Macro {
    String execute (Map data, String sin) {
        String sout = (sin ?: '') + (getSourceData(sin) ?: '')
        log.debug "in=[$sin], out=[$sout], " + this
        sout
    }
}

@Log4j
class Center extends Macro {
    String execute (Map data, String si) {
        String so = si
        if (!obj1) {
            obj1 = value.to_Integer()
        }
        if (obj1) {
            Integer width = (Integer) obj1
            so = data.center(width).take(width)
        }
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Dateformat extends Macro {
    String execute (Map data, String si) {
        String so = si // assumed to be a string of 8 digits
        try { so = Date.parse('yyyyMMdd',si).format(value) }
        catch (e) {
            log.error "date string [$si] cannot be reformatted from [yyyyMMdd] to [$value] " + this
        }
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Decode extends Macro {
    String execute (Map data, String si) {
        String so = si
        if (!obj1) {
            Map map = [:]
            (value.split(/,/) as List).collate(2).each { tuple ->
                if (tuple.size == 1) {
                    map[null] = tuple[0]
                }
                else {
                    map[tuple[0]] = tuple[1]
                }
            }
            obj1 = map
        }
        if (obj1) {
            Map map = (Map) obj1
            so = map.containsKey(si) ? map[data] : map[null]
        }
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Default extends Macro {
    String execute (Map data, String si) {
        String so = data ? getSourceData(data) : data
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class DeleteWhiteSpace extends Macro {
    String execute (Map data, String si) {
        String so = si?.replaceAll(/\s/,'')
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Divide extends Macro {
    String execute (Map data, String si) {
        String so = BigDecimal.ZERO.toString()
        BigDecimal databd = data.to_BigDecimal()
        BigDecimal sourcebd = getSourceData(data).to_BigDecimal()
        if (databd != BigDecimal.ZERO && sourcebd != BigDecimal.ZERO) {
            so = databd.divide(sourcebd, 2, BigDecimal.ROUND_HALF_UP).toString()
        }
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Left extends Macro {
    String execute (Map data, String si) {
        String so = si
        if (si?.length()) {
            if (!obj1) {
                obj1 = value.to_Integer()
            }
            so = data.take((Integer) obj1)
        }
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class RightPad extends Macro {
    String execute (Map data, String si) {
        String so = si
        if (valid && (!obj1 || !obj2)) {
            def pat = ~/\A(\d+)(?:\D)?(.*)\z/ // pattern
            def mat = value =~ pat // matcher
            if (mat.matches()) {
                obj1 = mat.group(1).to_Integer()
                obj2 = mat.group(2) + ' '
            }
            else {
                log.error "macro value [$value] does not match pattern [$pat]"
                valid = false
            }
        }
        if (valid && si && obj1 && obj2) {
            so = si.padRight(obj1, obj2[0]).take(obj1)
        }
        log.debug "in=[$so], out=[$si], " + this
        so
    }
}

@Log4j
class LeftPad extends Macro {
    String execute (Map data, String si) {
        String so = si
        if (valid && (!obj1 || !obj2)) {
            def pat = ~/\A(\d+)(?:\D)?(.*)\z/ // pattern
            def mat = value =~ pat // matcher
            if (mat.matches()) {
                obj1 = mat.group(1).to_Integer()
                obj2 = mat.group(2) + ' '
            }
            else {
                log.error "macro value [$value] does not match pattern [$pat]"
                valid = false
            }
        }
        if (valid && obj1 && obj2) {
            so = si.padLeft(obj1, obj2[0]).take(obj1)
        }
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Trim extends Macro {
    String execute (Map data, String si) {
        String so = si?.trim()
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Numberformat extends Macro {
    String execute (Map data, String si) {
        String so = si.to_BigDecimal(0.00).toString()
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}


def  addStringMethods () {

        String.metaClass.to_Integer { Integer a = 0 ->
            try {
                ((String) delegate).toInteger()
            } catch (e) {
                a
            }
        }

        String.metaClass.to_Long { Long a = 0L ->
            try {
                ((String) delegate).toLong()
            } catch (e) {
                a
            }
        }

        String.metaClass.to_BigInteger { BigInteger a = 0 ->
            try {
                ((String) delegate).toBigInteger()
            } catch (e) {
                a
            }
        }
		
		String.metaClass.to_BigDecimal { BigDecimal a = 0.00 ->
			try {
				((String) delegate).toBigDecimal()
			} catch (e) {
				a
			}
		}

    }
