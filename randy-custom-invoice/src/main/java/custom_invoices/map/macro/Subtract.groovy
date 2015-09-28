package custom_invoices.map.macro

import custom_invoices.map.Macro

@groovy.util.logging.Log4j
class Subtract extends Macro {

    @Override
    public String execute(Map data, String s) {
        s.to_BigDecimal().subtract(sourceData(data).to_BigDecimal()).toString()
    }

}
