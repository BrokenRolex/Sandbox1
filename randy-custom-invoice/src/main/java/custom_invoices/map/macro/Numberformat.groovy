package custom_invoices.map.macro

import custom_invoices.map.Macro
import java.text.DecimalFormat

@groovy.util.logging.Log4j
class Numberformat extends Macro {
    @Override
    String execute (Map data, String s) {
        String so = s
        if (!obj1) {
            try {
                obj1 = new DecimalFormat(value)
            }
            catch (e) {
                log.error "Decimal format pattern [$value] is not valid : " + e.message
            }
        }
        if (obj1) {
            try {
                so = ((DecimalFormat) (obj1)).format(s.to_BigDecimal())
            }
            catch (e) {
                log.error "cannot format [$s] using format [$value] : " + e.message
            }
        }
        so
    }
}