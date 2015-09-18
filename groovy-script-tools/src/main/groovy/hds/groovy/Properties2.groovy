package hds.groovy

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
    List<String> getListProp (String key) {
        getPropList(key, ',')
    }

    // get property value as a list
    List<String> getListProp (String key, String delim) {
        List<String> list = []
        String val = checkKey(key)
        if (val != null && val.length() > 0) {
            delim = delim == null ? ',' : delim
            for (String str in val.split(delim)) {
                String tstr = str.trim()
                if (tstr.length() > 0) {
                    list << tstr
                }
            }
        }
        list
    }

    // get property value as a list (csv)
    List<String> getPropList (String key) {
        getListProp(key, ',')
    }

    // get property value as a list
    List<String> getPropList (String key, String delim) {
        getListProp(key, delim)
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

    List<File> getFilePropList (String key) {
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

    List<Integer> getIntPropList (String key) {
        String value = checkKey(key)
        List<Integer> list = []
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

    List<Long> getLongPropList (String key) {
        String value = checkKey(key)
        List<Long> list = []
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
        List<String> keys = this.keySet().sort()
        for (String key : keys) {
            String val = this.getProperty(key)
            println "    ${key}=${val}"
        }
        println '}'
    }

    List<String> validate () {
        PropertyValidator pv = new PropertyValidator()
        pv.validate(Props.instance)
    }

    List<String> interpolate () {
        PropertyInterpolator pi = new PropertyInterpolator()
        pi.interpolate(Props.instance)
    }

    void addDefaultProps () {
        setProperty('DTTM', new Date().format('yyyyMMddHHmmss'))
        setProperty('HOSTNAME', Host.getName())
        setProperty('SCRIPT_DIR_PATH', Env.scriptFile.parentFile.path)
        setProperty('SCRIPT_DIR_NAME', Env.scriptFile.parentFile.name)
        setProperty('SCRIPT_PATH', Env.scriptFile.path)
        setProperty('SCRIPT_NAME', Env.scriptName)
    }

}
