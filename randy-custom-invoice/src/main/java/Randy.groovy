
//@Grab('com.hdsupply:hds-groovy:4.0')
@groovy.transform.BaseScript(script.ScriptWrapper)
import script.*

/*
@Grapes([
  @Grab('hdsupply.com:custom-invoices:1.0'),
  @Grab('commons-digester:commons-digester:2.1'),
  @Grab('commons-beanutils:commons-beanutils:1.9.2'),
  @Grab('commons-/lang:commons-lang:2.6'),
  @Grab('commons-net:commons-net:3.3'),
])
*/
import custom_invoices.io.*

def db = new DataBuilder()
db.eachFile { file ->
    db.build(file)
}

def ds = new DataSender()
ds.eachFile { file ->
    ds.send(file)
}
// props.print()

// ==========================================================================

props.getFileProp('in.dir').eachFileMatch(FileType.FILES, ~/.+\.xml/) { inFile ->
    def dr = new DataReader(inFile)
    def dw = new DataWriter(dr)
    //dw.write()
}
props.getFileProp('out.dir').eachFileMatch(FileType.FILES, ~/.+\.dat/) { File datFile ->
    File sumFile = new File(datFile.parentFile, datFile.name - '.dat' + '.sum')
    if (!sumFile.isFile()) {
        log.warn "cannot deliver [$datFile] because [$sumFile] does not exist"
        return
    }
    //
    Map summary = load_properties_file(sumFile)
    Map sendRules = load_send_rules_file(summary)
    //
    new File('D:/Sandbox1/randy-custom-invoice','customer_filename_template.txt').eachLine {
        sendRules['customer_filename_template'] = it
        String filename = build_customer_filename(summary, sendRules)
        println "$it    :   $filename"
    }
}

Map load_properties_file (File f) {
    if (!f.isFile()) {
        throw new Exception("properties file [$f] is missing")
    }
    Properties p = new Properties()
    f.withInputStream { p.load(it) }
    p as Map
}

Map load_send_rules_file (Map m) {
    if (!m.containsKey('buyer.custid')) {
        throw new Exception("customer number [buyer.custid] missing in summary data")
    }
    String custid = m['buyer.custid']
    File file = new File(props.getFileProp('send.dir'), "${custid}.txt")
    if (!file.isFile()) {
        throw new Exception("send rules file [$file] does not exist")
    }
    load_properties_file(file)
}

String build_customer_filename (Map summary, Map rules) {
    String custid = summary.containsKey('buyer.custid') ? summary['buyer.custid'] : ''
    //
    Map dates = [:]
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat('YYYYMMdd')
    ['rundt', 'begdt', 'enddt'].each { dt ->
        String dtkey = "run.$dt"
        String dtstr = summary.containsKey(dtkey) ? summary[dtkey] : ''
        dates[dt] = sdf.parse(dtstr)
        if (dates[dt] == null) {
            log.error "invalid date [$dtstr] in summary field [$dtkey] data does not match pattern [${sdf.toPattern()}]"
        }
    }
    dates['nowdt'] = new Date()
    //
    String template = '$custid/_/$begdt/_/$enddt/.csv' // default
    if (rules.containsKey('customer_filename_template')) {
        template = rules['customer_filename_template']
    }
    List name = []
    template.split(/\//).each { str ->
        if (str.take(1) == '$') {
            String s = str.drop(1)
            if (s.take(5) in ['begdt', 'enddt', 'rundt', 'nowdt']) {
                String date = s.take(5)
                String fmt = s.drop(5) ?: 'YYYYMMdd'
                // fmt should be valid for Java's SimpleDateFormat.
                // Java does not support an exact implementation of Perl's strftime.
                // Next 3 lines are a hack to support some strftime formats currently in use.
                fmt = fmt.contains('%d') ? fmt.replaceAll('%d','dd') : fmt
                fmt = fmt.contains('%H') ? fmt.replaceAll('%H','HH') : fmt
                fmt = fmt.contains('%M') ? fmt.replaceAll('%M','mm') : fmt
                try {
                    name << dates[date].format(fmt)
                }
                catch (e) {
                    log.error "token [$str] has an invalid date format [$fmt] in template [$template], token ignored"
                }
            }
            else if (s.take(6) == 'custid') {
                name << custid
            }
            else {
                log.error "token [$str] is an unknown function in template [$template], token ignored"
            }
        }
        else {
            name << str
        }
    }
    name.join('')
}

// ==========================================================================

/*
 @Log4j
 class AbsValue extends Macro {
 String execute (Map m, String s) {
 s.to_BigDecimal().abs().toString()
 }
 }
 @Log4j
 class Add extends Macro {
 String execute (Map data, String s) {
 sin.to_BigDecimal().add(getSourceData(sin).to_BigDecimal()).toString()
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
 */