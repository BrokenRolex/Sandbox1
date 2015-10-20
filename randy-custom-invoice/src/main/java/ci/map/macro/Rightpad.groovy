package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Rightpad extends Macro {
    @Override
    public String execute(Map data, String s) {
        if (!obj1) {
            obj1 = value.toInteger()
            if (obj1 < 0)  {
                obj1 = Integer.valueOf(0)
            }
        }
        Integer len = obj1
        len == 0 ? '' : (s ?: '').padRight(len, ' ').take(len)
    }
}
