package ci.map

@groovy.util.logging.Log4j
abstract class Macro {
	Position position // the position that this macro belongs to
	String name // macro name
	String value
	String source // 'data' or 'value'
	Object obj1
	Object obj2
	Map data
	Boolean valid
	Macro () {
		(name, value, source, valid) = ['', '', '', true]
	}
	abstract String execute (Map data, String s)
	String getSourceData (Map data) {
		String s
		if (source.equals('data')) { s = data[value] }
		else if (source.equals('value')) { s = value }
		else { log.error "invalid source [$source] data will be the empty string" }
		s ?: ''
	}
	String toString () {
		"macro[${name}, value=\"${value}\", source=\"${source}\"]"
	}
}