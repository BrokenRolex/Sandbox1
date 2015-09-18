package ci.map.macro

import ci.map.Macro
import groovy.util.logging.Log4j

@Log4j
class Trim extends Macro {

	@Override
	public String execute(Map data, String si) {
		String so = si?.trim()
		log.debug "in=[$si], out=[$so], " + this
		so
	}

}
