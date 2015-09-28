package custom_invoices.map.macro

import custom_invoices.map.Macro

@groovy.util.logging.Log4j
class Trim extends Macro {

    @Override
    public String execute(Map data, String s) {
        s == null ? '' : s.trim()
    }
}
