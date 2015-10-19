package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class SimpleDateformat extends Macro {
    @Override
    String execute (Map data, String s) {
        String so = s // assumed to be a string of 8 digits
        try {
             so = Date.parse('yyyyMMdd',s).format(value)
        }
        catch (e) {
            log.error "date string [$s] cannot be reformatted from [yyyyMMdd] to [$value] " + this
        }
        so
    }
}
