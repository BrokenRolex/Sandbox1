package ci.io

import script.Props
import ci.data.*
import ci.map.*
import java.util.regex.*

@groovy.util.logging.Log4j
class DataWriter {

    DataReader dataReader
    Writer wtr
    File outDir = Props.instance.getFileProp('out.dir')
    File outFile
    File datFile
    File sumFile
    
    DataWriter (DataReader dr) {
        this.dataReader = dr
        dataReader.readSummary()
    }
    
    void write () {
        datFile = new File(outDir, dataReader.inFile.name - '.xml' + '.dat')
        sumFile = new File(outDir, datFile.name - '.dat' + '.sum')
        List files = [dataReader.inFile,datFile,sumFile]
        if ((files.collect{it.name} as Set).size() != 3) {
            throw new Exception("")
        }
        log.info "writing [$sumFile]"
        sumFile.withWriter('UTF-8') { wtr ->
            dataReader.summary.each { k, v -> wtr << "$k=$v\n" }
        }
        log.info "writing [$datFile]"
        write_header()
        write_invoices()
        write_trailer()
    }
    
    void write_header () {
        wtr = datFile.newWriter('UTF-8')
        writeGroup('batch-header', dataReader.summary)
    }

    void write_trailer () {
        writeGroup('batch-trailer', dataReader.summary)
        wtr.flush()
        wtr.close()
        wtr = null
    }
    
    void write_invoices () {
        dataReader.eachInvoice() { invoice -> write_invoice(invoice) }
    }

    void write_invoice (Invoice invoice) {
        Map data = dataReader.summary + invoice.invoice
        writeGroup('invoice-header', data)
        invoice.items.each { item ->
            writeGroup('invoice-items', data + item)
        }
        invoice.taxes.each { tax ->
            writeGroup('invoice-taxes', data + tax)
        }
        invoice.charges.each { charge ->
            writeGroup('invoice-charges', data + charge)
        }
        writeGroup('invoice-trailer', data)
    }

    void writeGroup (String groupName, Map data) {
        String fldsep = ','
        String recsep = "\n"
        Group group = dataReader.batchMap.getGroupByName(groupName)
        if (group) {
            log.debug group.toString()
            group.sequences.each { Sequence sequence ->
                log.debug '..' + sequence
                List record = []
                sequence.positions.each { Position position ->

                    String field = position.sourceData(data)

                    log.debug '....' + position + ' = ' + field
                    position.macros.each { Macro macro ->
                        field = macro.execute(data, field)
                        log.debug '......' + macro + ' = ' + field
                    }
                    record[position.num-1] = field
                }
                String line = record.collect{(it ?: '').replaceAll(Pattern.quote(fldsep),'')}.join(fldsep)
                log.debug "data [$line]"
                wtr << line
                wtr << recsep
            }
        }
    }

}