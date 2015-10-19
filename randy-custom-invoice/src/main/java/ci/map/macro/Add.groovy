package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Add extends Macro {
    @Override
    public String execute(Map data, String s) {
        s.to_BigDecimal().add(sourceData(data).to_BigDecimal()).toString()
    }
}
