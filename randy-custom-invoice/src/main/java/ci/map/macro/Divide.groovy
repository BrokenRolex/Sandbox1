package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Divide extends Macro {

    @Override
    public String execute(Map data, String s) {
        BigDecimal a = s.to_BigDecimal().divide(a).toString()
        BigDecimal b = sourceData(data).to_BigDecimal()
        (b.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : a.divide(b)).toString()
    }

}
