package custom_invoices.map.macro

import custom_invoices.map.Macro

@groovy.util.logging.Log4j
class Printformat extends Macro {
    String execute (Map data, String si) {
        String so = si
        log.debug "in=[$si], out=[$so], " + this.toString()
        so
    }
}
