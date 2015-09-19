package script

class PropertyValidator {
    static final String VALIDATION_PREFIX = 'validate.'

    List validate (Properties properties) {
        //println "validating properties"
        List errors = []
        StringValidator validator = new StringValidator()
        for (String vkey in properties.keySet().findAll({ it.startsWith(VALIDATION_PREFIX) })) {
            String rule = properties[vkey] // validation rule
            if (rule.length() == 0) {
                errors << "validation key [$vkey] does not have a validation rule"
                continue
            }
            String key = vkey.substring(VALIDATION_PREFIX.length()) // key of data to be validated
            if (key.length() == 0 || !properties.containsKey(key)) {
                errors << "validation property [$vkey] refers to a property that does not exist"
                continue
            }
            String data = properties[key] // data to validate with a rule
            if (!validator.validate(data, rule)) {
                errors << "property [$key] value [$data] does not validate as [$rule]"
            }
        }
        errors
    }

}
