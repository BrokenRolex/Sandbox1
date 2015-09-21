package ci.map.macro

//import ci.map.Macro
import ci.map.Macro
import java.text.DecimalFormat

@groovy.util.logging.Log4j
class Numberformat extends Macro {
    @Override
    String execute (Map data, String si) {
        String so = si
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
                so = ((DecimalFormat) (obj1)).format(si.to_BigDecimal())
            }
            catch (e) {
                log.error "cannot format [$si] using format [$value] : " + e.message
            }
        }
        log.debug "in=[$si], out=[$so], " + this.toString()
        so
    }
}