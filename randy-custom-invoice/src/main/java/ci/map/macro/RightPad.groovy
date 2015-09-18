package ci.map.macro

import ci.map.Macro
import groovy.util.logging.Log4j

@Log4j
class RightPad extends Macro {

	@Override
	public String execute(Map data, String si) {
		String so = si
		if (valid && (!obj1 || !obj2)) {
			def pat = ~/\A(\d+)(?:\D)?(.*)\z/ // pattern
			def mat = value =~ pat // matcher
			if (mat.matches()) {
				obj1 = mat.group(1).to_Integer()
				obj2 = mat.group(2) + ' '
			}
			else {
				log.error "macro value [$value] does not match pattern [$pat]"
				valid = false
			}
		}
		if (valid && si && obj1 && obj2) {
			so = si.padRight(obj1, obj2[0]).take(obj1)
		}
		log.debug "in=[$so], out=[$si], " + this
		so
	}

}
