package custom_invoices.map.macro

import groovy.util.logging.Log4j
import custom_invoices.map.Macro

@groovy.util.logging.Log4j
class Multiply extends Macro {

    @Override
    public String execute(Map data, String s) {
        s.to_BigDecimal().multiply(sourceData(data).to_BigDecimal()).toString()
    }

}
