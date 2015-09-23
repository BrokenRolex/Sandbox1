package ci.io

import script.Props
import org.apache.commons.digester.Digester
import org.apache.commons.digester.xmlrules.DigesterLoader
import ci.map.BatchMap
import groovy.util.logging.Log4j

@Singleton
@Log4j
class DataDigester {
    Digester digester
    File digesterFile
    File mapDir = Props.instance.getFileProp('map.dir')
    
    BatchMap buildBatchMap (String custid) {
        if (digester == null) {
            digesterFile = new File(mapDir, 'map-digester-rules.xml') // TODO properties
            log.info "loading [$digesterFile]"
            digester = DigesterLoader.createDigester(digesterFile.toURI().toURL())
        }
        File mapFile = new File(mapDir, "${custid}.xml")
        log.info "digesting [$mapFile]"
        (BatchMap) digester.parse(mapFile)
    }

}
