package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Dateformat extends Macro {
    @Override
    String execute (Map data, String si) {
        String so = si // assumed to be a string of 8 digits
        try { so = Date.parse('yyyyMMdd',si).format(value) }
        catch (e) {
            log.error "date string [$si] cannot be reformatted from [yyyyMMdd] to [$value] " + this
        }
        log.debug "in=[$si], out=[$so], " + this.toString()
        so
    }
}
