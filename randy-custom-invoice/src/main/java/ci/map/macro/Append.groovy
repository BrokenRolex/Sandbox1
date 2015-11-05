package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Append extends Macro {
    @Override
    public String execute(Map map, String s) {
        (s == null ? '' : s) + this.data(map)
    }
}
