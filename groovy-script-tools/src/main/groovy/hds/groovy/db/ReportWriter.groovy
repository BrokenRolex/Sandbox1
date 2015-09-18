package hds.groovy.db

import groovy.sql.Sql
import groovy.xml.*

class ReportWriter {
    
    String select = "select null from dual"
    String recordSep = "\n"
    String fieldSep = "\t"
    Boolean includeHeader = true
    File outputFile
    
    void writeText (Sql sql) {
        outputFile.withWriter('UTF-8') { wtr ->
            def writeHeader = { meta ->
                if (includeHeader) {
                    def values = []
                    (1..meta.columnCount).each {
                        values << meta.getColumnLabel(it)
                    }
                    wtr.write(values.join(fieldSep))
                    wtr.write(recordSep)
                }
            }
            sql.eachRow(select, writeHeader) { row ->
                def values = []
                row.toRowResult().values().each{
                    values << (it == null ? "" : it.toString())
                }
                wtr.write(values.join(fieldSep))
                wtr.write(recordSep)
            }
        }
    }

    void writeXml (Sql sql) {
        def builder = new StreamingMarkupBuilder()
        def doc = builder.bind {
            def cols = []
            def h = { meta -> (1..meta.columnCount).each { cols << meta.getColumnLabel(it) } }
            mkp.xmlDeclaration()
            records {
                sql.eachRow(select, h) { row ->
                    def values = [:]
                    cols.each {
                        def value = row[it]
                        if (value == null) value = ''
                        values[it] = value
                    }
                    record(values)
                }
            }
        }
        outputFile.withWriter('UTF-8') { wtr ->
             wtr << XmlUtil.serialize(doc)
        }
    }

}
