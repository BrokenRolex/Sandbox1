package ci.map.macro

import ci.map.Macro
import groovy.util.logging.Log4j

@Log4j
class Trim extends Macro {

    @Override
    public String execute(Map data, String s) {
        s == null ? '' : s.trim()
    }
}
