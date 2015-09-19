package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Printformat extends Macro {
    String execute (Map data, String si) {
        String so = si
        log.debug "in=[$si], out=[$so], " + this.toString()
        so
    }
}
