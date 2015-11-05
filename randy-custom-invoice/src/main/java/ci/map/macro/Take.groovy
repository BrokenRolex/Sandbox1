package ci.map.macro

import ci.map.Macro

// <macro name="take" value="length" />

@groovy.util.logging.Log4j
class Take extends Macro {
    @Override
    public String execute (Map data, String inputString) {
        inputString = inputString == null ? '' : inputString
        String outputString = inputString
        if (obj1 == null) {
            try {
                obj1 = (value == null) ? Integer.valueOf(0) : value.toInteger()
            }
            catch (e) {
                obj1 = Integer.valueOf(0)
            }
        }
        if (obj1 != null) {
            Integer length = (Integer) obj1
            outputString = inputString.take(length < 1 ? 0 : length)
        }
        outputString
    }
}
