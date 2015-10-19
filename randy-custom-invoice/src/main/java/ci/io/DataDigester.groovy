package ci.io

import script.Props
import org.apache.commons.digester.Digester
import org.apache.commons.digester.xmlrules.DigesterLoader
import ci.map.BatchMap

@Singleton
@groovy.util.logging.Log4j
class DataDigester {
    File mapDir = Props.instance.getFileProp('map.dir')
    Digester digester
    File digesterFile
    File mapFile
    
    BatchMap buildBatchMap (String custid) {
        mapFile = new File(mapDir, "${custid}.xml")
        log.info "digesting [$mapFile]"
        if (digester == null) {
            digesterFile = new File(mapDir, Props.instance.getProp('digester.rules'))
            log.info "loading digester rules [$digesterFile]"
            digester = DigesterLoader.createDigester(digesterFile.toURI().toURL())
        }
        (BatchMap) digester.parse(mapFile)
    }

}
