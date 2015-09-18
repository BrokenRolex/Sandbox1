package ci.map

import org.apache.commons.digester.Digester
import org.apache.commons.digester.ObjectCreationFactory
import org.xml.sax.Attributes
import ci.map.Macro

@groovy.util.logging.Log4j
class MacroFactory implements ObjectCreationFactory {
	Digester digester

	@Override
	Object createObject (Attributes attrs) {
		Map map = [:]
		(0 ..< attrs.getLength()).each { i -> map[attrs.getLocalName(i)] = attrs.getValue(i) }
		if (!map.name) {
			throw new Exception("cannot create macro from $map, macro name is missing")
		}
		String className = 'ci.map.macro.' + map.name.split('_').collect { it.capitalize() }.join('')

		GroovyClassLoader loader = new GroovyClassLoader()
		Class c
		try {
			c = loader.loadClass(className)
		}
		catch (e) {
			throw new Exception("cannot create object from $map, class [$className] does not exist")
		}
		Object o = c.newInstance()
		if (!(o instanceof Macro)) {
			throw new Exception("cannot create object from $map, class [${c.getCanonicalName()}] does not extend from [${Macro.getCanonicalName()}]")
		}
		loader.close()

		Macro macro = (Macro) o
		macro.name = className
		macro.value = map.value
		macro.source = map.source ?: 'value'

		macro
	}

}