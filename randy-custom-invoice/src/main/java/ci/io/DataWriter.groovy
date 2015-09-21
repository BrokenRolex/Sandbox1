package ci.io

import ci.data.Invoice
import ci.map.BatchMap
import ci.map.Group
import ci.map.Sequence
import ci.map.Position
import ci.map.Macro
import java.util.regex.*

@groovy.util.logging.Log4j
class DataWriter {

    BatchMap batchMap
    Map summary
    File outFile
    BufferedOutputStream bos

    void write_header () {
        bos = outFile.newOutputStream()
        writeGroup('batch-header', summary)
    }

    void write_trailer () {
        writeGroup('batch-trailer', summary)
        bos.flush()
        bos.close()
        bos = null
    }

    void write_invoice (Invoice invoice) {
        Map data = summary + invoice.invoice
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
        Group group = batchMap.getGroupByName(groupName)
        if (group) {
            group.sequences.each { Sequence sequence ->
                List record = []
                sequence.positions.each { Position position ->
                    String field = ''
                    if (position.source == 'value') { field = position.value }
                    else if (position.source == 'data') { field = data[position.value] }
                    position.macros.each { Macro macro ->
                        field = macro.execute(data, field)
                    }
                    record[position.num-1] = field
                }
                String line = record.collect{(it ?: '').replaceAll(Pattern.quote(fldsep),'')}.join(fldsep)
                println line
                bos << line
                bos << recsep
            }
        }
    }

}