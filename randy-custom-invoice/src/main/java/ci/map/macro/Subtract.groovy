package ci.map.macro

import groovy.util.logging.Log4j
import ci.map.Macro

@Log4j
class Subtract extends Macro {

    @Override
    public String execute(Map data, String s) {
        s.to_BigDecimal().subtract(sourceData(data).to_BigDecimal()).toString()
    }

}
