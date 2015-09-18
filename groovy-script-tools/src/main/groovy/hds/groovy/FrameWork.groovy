package hds.groovy

class FrameWork {

    static final String name = 'framework.properties'
    static Properties properties

    static {
        properties = new Properties()
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader()
            if (cl) {
                URL resource = cl.getResource(name)
                if (resource) {
                    resource.withInputStream { InputStream is -> properties.load(is) }
                }
                else {
                    properties.setProperty('error', "cannot find [$name] with the thread context class loader")
                }
            }
            else {
                properties.setProperty('error', 'cannot get the thread context class loader')
            }
        }
        catch (e) {
            properties.setProperty('error', e.message)
        }
    }

    static String getVersion () {
        properties.getProperty('version')
    }

    static String getName () {
        properties.getProperty('name')
    }

    static String getGroup () {
        properties.getProperty('group')
    }

    static String getTitle () {
        properties.getProperty('title')
    }

    static String getVendor () {
        properties.getProperty('vendor')
    }

    static String getError () {
        properties.getProperty('error')
    }

}