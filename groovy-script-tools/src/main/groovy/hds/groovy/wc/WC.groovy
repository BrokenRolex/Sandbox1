package hds.groovy.wc

import java.io.File

@Singleton
class WC {
    static File dir = new File('/opt/apps/WAS/CommerceServer70')
    static File earDir = new File(dir, 'wc.ear')
    static File logsDir = new File(dir, 'logs')
    static File xmlConfigDir = new File(earDir, 'xml/config')
    static File wcServerXmlFile = new File(xmlConfigDir, 'wc-server.xml')

    Map databaseProperties

    Map databaseProperties (File file) {
        if (databaseProperties != null) {
            return databaseProperties
        }
        if (file.isFile()) {
            Node config = new XmlParser().parse(file)
            Map db = config?.InstanceProperties?.Database?.DB[0]?.attributes()
            if (db == null) {
                return [:]
            }
            databaseProperties = db
        }
        return databaseProperties
    }

    Map databaseProperties () {
        return databaseProperties(wcServerXmlFile)
    }
}
