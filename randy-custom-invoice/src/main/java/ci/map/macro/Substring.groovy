package ci.map.macro

import ci.map.Macro

// <macro name="substring" value="offset,length" />

// <macro name="substring" value="offset" />

@groovy.util.logging.Log4j
class Substring extends Macro {
    @Override
    public String execute(Map data, String inputString) {
        // value = 'offset,length' or 'offset'
        // obj1 = offset
        // obj2 = length
        String outputString = inputString
        if (value != null)  {
            value = value.replaceAll(' ','')
            if (obj1 == null) {
                if (value.contains(',')) {
                    def (offset, length) = value.split(/,/,2)
                    obj1 = offset.toInteger()
                    obj2 = length.toInteger()
                    obj2 = (((Integer) obj2) < 0) ? Integer.valueOf(0) : obj2
                }
                else {
                    obj1 = value.toInteger()
                }
                obj1 = (((Integer) obj1) < 0) ? Integer.valueOf(0) : obj1
            }
            if (obj1 != null) {
                Integer offset = (Integer) obj1
                if (offset > 0) {
                   outputString = inputString.drop(offset) 
                }
                if (obj2 != null) {
                    Integer length = (Integer) obj2
                    if (length > 0) {
                         outputString = outputString.take(length)     
                    }
                }
            }
        }
        outputString
    }
}
