package ci.io

import script.*
import ci.map.BatchMap
import ci.data.Invoice
import groovy.util.Node
import groovy.io.FileType
import groovy.util.logging.Log4j

@Log4j
class DataReader {
    List detailPaths = ['invoice.item', 'invoice.tax', 'invoice.charge']
    XmlParser xp = new XmlParser()
    Node root
    File inFile
    File inDir = Props.instance.getFileProp('in.dir')
    Map summary
    BatchMap batchMap
    String custid
    
    DataReader (File f) {
        this.inFile = f
    }

    void readSummary () {
        log.info "reading summary data from [$inFile]"
        root = xp.parse(inFile)
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

        custid = summary['buyer.custid']
        log.info "custid [$custid]"
        batchMap = DataDigester.instance.buildBatchMap(custid)
    }

    void eachInvoice (Closure closure) {
        if (!summary) {
            throw new Exception("oops")
        }
        log.info "reading invoices from [$inFile]"
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

            closure.call(new Invoice(invoice: invoice, items: items, taxes: taxes, charges: charges))
        }
    }
}