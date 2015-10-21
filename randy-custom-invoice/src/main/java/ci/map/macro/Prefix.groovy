package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Prefix extends Macro {
    @Override
    public String execute(Map data, String s) {
        sourceData(data) + (s == null ? '' : s)
    }
}
