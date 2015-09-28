package custom_invoices.io

import script.*
import groovy.io.FileType

@groovy.util.logging.Log4j
class DataBuilder {
    DataReader dr
    DataWriter dw
    
    void eachFile (Closure c) {
        Props.instance.getFileProp('in.dir').eachFileMatch(FileType.FILES, ~/.+\.xml/, c)
    }

    void build (File file) {
        log.debug "build [$file]"
        dr = new DataReader(file)
        dw = new DataWriter(dr)
        dw.write()
    }
    
}
