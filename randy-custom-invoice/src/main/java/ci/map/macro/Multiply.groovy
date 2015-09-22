package ci.map.macro

import groovy.util.logging.Log4j
import ci.map.Macro

@Log4j
class Multiply extends Macro {

    @Override
    public String execute(Map data, String s) {
        s.to_BigDecimal().multiply(sourceData(data).to_BigDecimal()).toString()
    }

}
