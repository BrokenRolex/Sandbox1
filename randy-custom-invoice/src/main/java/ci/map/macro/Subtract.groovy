package ci.map.macro

import groovy.util.logging.Log4j
import ci.map.Macro

@Log4j
class Subtract extends Macro {

    @Override
    public String execute(Map data, String si) {
        String so = si
        log.debug "in=[$si], out=[$so], " + this.toString()
        so
    }

}
