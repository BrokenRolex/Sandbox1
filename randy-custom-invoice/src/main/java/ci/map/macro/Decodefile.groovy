package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Decodefile extends Macro {
    @Override
    String execute (Map data, String s) {
        String so = s
        if (!obj1) {
            Map map = [:]
            // "Sort-of" decode -- expects file with 2 tab separated values
            // on each line -- if first matches data, returns second
            map[null] = s
            File file = new File(value)
            if (file.isFile()) {
                file.eachLine { String line ->
                     def (k,v) = line.split(/\t/)
                     map[k] = v
                }
                obj1 = map
            }
            else {
                log.error "file [$file] does not exist"
            }
        }
        if (obj1) {
            Map map = (Map) obj1
            so = map.containsKey(s) ? map[data] : map[null]
        }
        so
    }
}
