package ci.map.macro

import ci.map.Macro

// <macro name="take" value="length" />

@groovy.util.logging.Log4j
class Take extends Macro {
    @Override
    public String execute (Map data, String inputString) {
        String outputString = inputString
        if (value != null)  {
            if (!obj1) {
                obj1 = value.toInteger()
            }
            if (obj1) {
                Integer length = (Integer) obj1
                if (length > 0) {
                    outputString = inputString.take(length)
                }
            }
        }
        outputString
    }
}
