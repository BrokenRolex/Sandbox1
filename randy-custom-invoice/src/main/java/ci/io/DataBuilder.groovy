package ci.io

import script.*
import groovy.util.logging.Log4j
import groovy.io.FileType

@Log4j
class DataBuilder {
    
    
    void eachFile (Closure c) {
        Props.instance.getFileProp('in.dir').eachFileMatch(FileType.FILES, ~/.+\.xml/, c)
    }

    void build (File file) {
        log.debug "build [$file]"
        def dr = new DataReader(file)
        def dw = new DataWriter(dr)
        dw.write()
    }
    
}
