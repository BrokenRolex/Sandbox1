package ci.map.macro

import ci.map.Macro

// <macro name="take" value="length" />

@groovy.util.logging.Log4j
class Take extends Macro {
    @Override
    public String execute (Map data, String inputString) {
        inputString = inputString == null ? '' : inputString
        String outputString = inputString
        if (!obj1) {
            obj1 = (value == null) ? Integer.valueOf(0) : value.toInteger()
        }
        if (obj1) {
            Integer length = (Integer) obj1
            outputString = (length > 0) ? inputString.take(length) : ''
        }
        outputString
    }
}
