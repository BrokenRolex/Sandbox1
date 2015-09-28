package custom_invoices.map.macro

import custom_invoices.map.Macro

@groovy.util.logging.Log4j
class Decode extends Macro {
    @Override
    String execute (Map data, String s) {
        String so = s
        if (!obj1) {
            Map map = [:]
            (value.split(/,/) as List).collate(2).each { tuple ->
                if (tuple.size == 1) {
                    map[null] = tuple[0]
                }
                else {
                    map[tuple[0]] = tuple[1]
                }
            }
            obj1 = map
        }
        if (obj1) {
            Map map = (Map) obj1
            so = map.containsKey(s) ? map[data] : map[null]
        }
        so
    }
}
