package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Rightpad extends Macro {

    @Override
    public String execute(Map data, String s) {
        String so = s
        if (valid && (!obj1 || !obj2)) {
            def pat = ~/\A(\d+)(?:\D)?(.*)\z/ // pattern
            def mat = value =~ pat // matcher
            if (mat.matches()) {
                obj1 = mat.group(1).to_Integer()
                obj2 = mat.group(2) + ' '
            }
            else {
                log.error "macro value [$value] does not match pattern [$pat]"
                valid = false
            }
        }
        if (valid && s && obj1 && obj2) {
            so = s.padRight(obj1, obj2[0]).take(obj1)
        }
        so
    }

}
