package ci.map.macro

import ci.map.Macro

// <macro name="substring" value="offset,length" />

// <macro name="substring" value="offset" />

@groovy.util.logging.Log4j
class Substring extends Macro {
    @Override
    public String execute(Map data, String inputString) {
        // value = offset,length
        //    or = offset
        // obj1 = offset
        // obj2 = length
        String outputString = inputString
        if (value != null)  {
            if (!obj1) {
                if (value.contains(',')) {
                    def (offset, length) = value.replaceAll(' ','').split(/,/,2)
                    obj1 = offset.toInteger()
                    obj2 = length.toInteger()
                }
                else {
                    obj1 = value.replaceAll(' ','').toInteger()
                }
            }
            if (obj1) {
                println obj1
                println obj2
                Integer offset = (Integer) obj1
                if (offset > 0) {
                   outputString = inputString.drop(offset) 
                }
                if (obj2) {
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
