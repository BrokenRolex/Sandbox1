package ci.map.macro

import groovy.util.logging.Log4j
import ci.map.Macro

@Log4j
class Add extends Macro {
    @Override
    public String execute(Map data, String sin) {
        String sout = sin.to_BigDecimal().add(getSourceData(sin).to_BigDecimal()).toString()
        log.debug "in=[$sin], out=[$sout], " + this
        sout
    }
}
