package script

@groovy.util.logging.Log4j
@groovy.transform.CompileStatic
class Properties2 extends Properties {

    void load (File file) {
        load(file.newInputStream())
    }

    void load (String string) {
        load(new StringReader(string))
    }

    String getProp (String key) {
        checkKey(key)
    }

    // get property value as a list (csv)
    List getListProp (String key) {
        getListProp(key, ',')
    }

    // get property value as a list
    List getListProp (String key, String delim) {
        String value = checkKey(key)
        value.split(delim ?: ',',-1) as List
    }

    List getListProp (String key, String delim, List defaultValue) {
        String value = this.getProperty(key)
        value == null ? defaultValue : (value.split(delim ?: ',',-1) as List)
    }

    Boolean getBooleanProp (String key) {
        if (key == null || !this.containsKey(key)) return false
        StringUtils.isTrue(this.getProperty(key))
    }

    Boolean getBooleanProp (String key, Boolean defaultValue) {
        if (defaultValue == null) defaultValue = false
        if (key == null || !this.containsKey(key)) return defaultValue
        StringUtils.isTrue(this.getProperty(key))
    }

    File getFileProp (String key) {
        new File(checkKey(key))
    }

    File getFileProp (String key, File defaultFile) {
        if (key != null) {
            if (!this.containsKey(key)) {
                return defaultFile
            }
            return new File(this.getProperty(key))
        }
        defaultFile
    }

    List getFilePropList (String key) {
        String val = checkKey(key)
        List files = []
        for (String s in val.split(',')) {
            files << new File(s)
        }
        files
    }

    Integer getIntProp (String key) {
        Integer.valueOf(checkKey(key))
    }

    Integer getIntProp (String key, Integer defaultValue) {
        if (defaultValue == null) {
            defaultValue = Integer.valueOf(0)
        }
        if (key == null || !this.containsKey(key)) {
            return defaultValue
        }
        Integer.valueOf(this.getProperty(key))
    }

    List getIntPropList (String key) {
        String value = checkKey(key)
        List list = []
        for (String i in value.split(',')) {
            list << Integer.valueOf(i)
        }
        list
    }

    Long getLongProp (String key) {
        Long.valueOf(checkKey(key))
    }

    Long getLongProp (String key, Long defaultValue) {
        if (defaultValue == null) {
            defaultValue = Long.valueOf(0)
        }
        if (key == null || !this.containsKey(key)) {
            return defaultValue
        }
        Long.valueOf(this.getProperty(key))
    }

    List getLongPropList (String key) {
        String value = checkKey(key)
        List list = []
        for (String i in value.split(',')) {
            list << Long.valueOf(i)
        }
        list
    }

    private String checkKey (String key) {
        if (key == null || !this.containsKey(key)) {
            throw new Exception("property key [$key] does not exist")
        }
        this.getProperty(key)
    }

    void print () {
        println 'properties {'
        List keys = this.keySet().sort()
        for (String key : keys) {
            String val = this.getProperty(key)
            println "    ${key}=${val}"
        }
        println '}'
    }

    List validate () {
        String validation_prefix = 'validate.'
        Boolean good = true
        List errors = []
        StringValidator validator = new StringValidator()
        for (String vkey in this.keySet()) {
            String rule = this.getProperty(vkey)
            if (vkey.startsWith(validation_prefix)) {
                if (rule && validator.isaRule(rule)) {
                    String key = vkey - validation_prefix
                    if (key && this.containsKey(key)) {
                        String data = this.getProperty(key)
                        if (!validator.validate(data, rule)) {
                            String errmsg = "property [$key] value [$data] does not validate as [$rule]"
                            log.error errmsg
                            errors << errmsg
                            good = false
                        }
                    }
                }
            }

        }
        errors
    }

    List interpolate () {
        List errors = []
        def interpolate_pattern = ~/(\$\{([^\$\{\}]+)\})/
        for (String key in this.keySet()) {
            String val = this.getProperty(key) // property value to interpolate
            if (val.contains('${')) {
                try {
                    Integer matchAttempts = 0
                    String interpolatedValue = val
                    while (true) {
                        matchAttempts++ 
                        def matcher = interpolatedValue =~ interpolate_pattern
                        if (matcher.find()) {
                            String key1 = matcher.group(1) // full replacement var (e.g. ${var})
                            String key2 = matcher.group(2) // replacement var name (e.g. var)
                            String val2 = this.getProperty(key2) // replacement value
                            if (val2 == null) {
                                break // stop if a key is not found
                            }
                            interpolatedValue = interpolatedValue.replace(key1, val2)
                            this.setProperty(key, interpolatedValue)
                        }
                        else {
                            break // stop, no match
                        }
                        if ((matchAttempts) > 99) {
                            String errmsg = "interpolation: possible circular reference detected in property [$key] value [$val]"
                            log.error errmsg
                            errors << errmsg
                            break // stop
                        }
                    }
                }
                catch (Exception e) {
                    String errmsg = "cannot interpolate property [$key] value [$val] error [${e.message}]"
                    log.error errmsg
                    errors << errmsg
                }
            }
        }
    }

    void addDefaultProps () {
        setProperty('HOSTNAME', Host.getName())
        setProperty('SCRIPT_DIR_NAME', Env.scriptFile.parentFile.name)
        setProperty('SCRIPT_DIR_PATH', Env.scriptFile.parentFile.path)
        setProperty('SCRIPT_PATH', Env.scriptFile.path)
        setProperty('SCRIPT_NAME', Env.scriptFile.name)
    }

    @Override
    String getProperty(String key) {
        super.getProperty(key)
    }

    @Override
    String getProperty(String key, String defaultValue) {
        super.getProperty(key, defaultValue)
    }

}