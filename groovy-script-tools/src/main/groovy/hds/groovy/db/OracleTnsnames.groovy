package hds.groovy.db

import hds.groovy.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import groovy.transform.Memoized

/**
 * Load Oracle's tnsnames.ora so that Oracle database connections can be made.
 * This class only provides database info, not account credentials.
 * 
 * If you need to get users and passwords see the OracleAccounts class.
 * <p/>
 * Tnsnames files are looked for in several locations.
 * The following steps are executed the first time an alias is requested with the getAlias method.
 * <ul style="list-style: decimal;">
 *   <li>Groovy script directory 
 *       <p>
 *       The Env class is used to determine the scipt's directory.
 *       The hidden file <i>.tnsnames.ora</i> is looked for in this directory.
 *       If it exists, it is loaded.
 *   </li>
 *   <li>Users home directory
 *       <p>
 *       The Java system property <i>user.home</i> is used to determine the users
 *       home directory. If this property exists and it contains a directory that
 *       exists, and if the hidden file <i>.tnsnames.ora</i> exists in this
 *       directory, it is loaded.
 *   </li>
 *   <li>TNS_ADMIN/tnsnames.ora
 *       <p>
 *       If the environment variable TNS_ADMIN exists and contains a directory
 *       and the file <i>tnsnames.ora</i> exists in this directory then it is
 *       loaded.
 *   </li>
 *   <li>ORACLE_HOME/network/admin/tnsnames.ora
 *       <p>
 *       If the environment variable ORACLE_HOME exists and contains a
 *       directory and the file <i>network/admin/tnsnames.ora</i> exists,
 *       it is loaded.
 *   </li>
 * </ul>
 * <p/>
 * If an alias is found in multiple configuration files no overriding
 * occurs. The first alias found is used. If no tnsnames files can
 * be found resulting in no database aliases being loaded then a
 * fatal exception will be thrown.
 * <p>
 * If you have a host that does not have an Oracle client installed,
 * it is very unlikely that any tnsnames.ora file will exist at all.
 * So, steps 3 and 4 will definately not find one. When this happens,
 * you will need get one so that at least steps 1 or 2 find a
 * tnsnames.ora file.
 * <p>
 * Tnsnames Structure
 * <ul>Here is a typical TNS entry.  See Oracle documentation for details.</ul>
 * <ul><ul>
 * banana=(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(<b>HOST</b> = ecdev2.hdsupply.net)(<b>PORT</b> = 1521))
 *  (CONNECT_DATA = (SERVER = DEDICATED) (<b>SERVICE_NAME</b> = ecdev2)))
 * </ul></ul>
 * <ul>
 * The important pieces of information here are the alias name (banana), service name (ecdev2), host (ecdev2.hdsupply.net), and port (1521).
 * <br/>This getAlias method will return an alias as a map with service, host, and port as keys.
 * </ul>
 * <p>
 * Singleton
 * <ul>
 * Normally you would never use this class directly. It is used in the OracleSql class to get database connections.
 * <br/>
 * However, if you do need to use it, this class is a singleton.
 * <br/>
 * It uses the <tt>@Singleton</tt> annotation so you'll need to get
 * an OracleTnsnames instance by accessing either the static <i>getInstance()</i> method or the static <i>instance</i> property.
 * </ul>
 * <p>
 * Sample Code:
 * <pre>
 * def alias = OracleTnsnames.instance.getAlias('ecommdev')
 * </pre>
 * 
 * @author en032339
 */
@groovy.util.logging.Log4j
@Singleton
class OracleTnsnames {
    static final String TNSNAMES_ORA = 'tnsnames.ora'
    static final String _TNSNAMES_ORA = '.tnsnames.ora'
    static final Pattern HOST_PAT = ~/\(\s*[Hh][Oo][Ss][Tt]\s*=\s*([\p{Alnum}._]+\s*)/
    static final Pattern PORT_PAT = ~/\(\s*[Pp][Oo][Rr][Tt]\s*=\s*(\d+)/
    static final Pattern SERVICE_PAT = ~/\(\s*[Ss][Ee][Rr][Vv][Ii][Cc][Ee]_[Nn][Aa][Mm][Ee]\s*=\s*([\p{Alnum}_]+)\s*\)/
    static final Pattern ALIAS_PAT= ~/[\p{Alnum}._]+/
    static final Map PATS = [
        host     : HOST_PAT,
        port     : PORT_PAT,
        service  : SERVICE_PAT,
    ]

    Map data // key = alias, value = map of connection properties

    synchronized void load () {
        //println "OracleTnsnames.instance.load()"
        if (data == null) {
            // look for tnsnames.ora in the usual oracle places
            System.getenv().each { envName, envValue ->
                if (envName == 'ORACLE_HOME') {
                    load(new File(new File(new File(envValue), 'network/admin'), TNSNAMES_ORA))
                }
                if (envName == 'TNS_ADMIN') {
                    load(new File(new File(envValue), TNSNAMES_ORA))
                }
            }
            // user home dir override
            load(new File(Env.homeDir, _TNSNAMES_ORA))
            load(new File(Env.homeDir, TNSNAMES_ORA))
            // script dir override
            load(new File(Env.scriptFile.parentFile, _TNSNAMES_ORA))
            load(new File(Env.scriptFile.parentFile, TNSNAMES_ORA))
        }
        //println data.keySet()
    }

    synchronized void load (File file) {
        //println "OracleTnsnames loading [$file]"
        if (file == null || !file.isFile()) { return }
        data = (data == null) ? [:] : data
        String aliasName = null
        String aliasStr = null
        file.eachLine { String line ->
            line = line.trim()
            // ignore blank lines and comments
            if (line.size() == 0 || line.startsWith("#")) {
                return // next line
            }
            // process a line
            if (aliasName == null) {
                String[] lineSplit2 = line.split('=', 2)
                if (lineSplit2.length < 2) {
                    log.warn "bad tnsnames.ora line [$line]"
                    return // next line
                }
                String key = lineSplit2[0].trim()
                Matcher aliasMatcher = key =~ ALIAS_PAT
                if (aliasMatcher.matches()) {
                    aliasName = key
                    aliasStr = lineSplit2[1].trim()
                }
                else {
                    log.warn "can't find alias name in [$line]"
                    return // next line
                }
            }
            else {
                aliasStr += ' ' + line
            }
            // determine if a definition is complete
            int opc = aliasStr.count('(') // open paren count
            int cpc = aliasStr.count(')') // close paren count
            boolean aliasComplete = (opc > 0 && cpc > 0) && (opc == cpc)
            if (aliasComplete == false) {
                return // next line
            }
            // parse definition and store key/values in a map
            Map aliasProps = [:]
            PATS.each { key, pat ->
                Matcher matcher = aliasStr =~ pat
                if (matcher.find()) {
                    aliasProps[key] = matcher.group(1)
                    return // next pattern
                }
                log.warn "alias [$aliasName] cannot find property [$key]"
            }
            aliasProps['name'] = aliasName
            data[aliasName] = aliasProps
            aliasProps['def'] = aliasStr.trim()
            // reset... get ready for the next alias
            aliasName = aliasStr = null
        }

    }

    /**
     * Get a Map of Tnsname properties for an alias
     * <p/>
     * The map retured by this method will have 3 key value pairs. They are.
     * <ul>
     * <li>service = Database name (from tns entry SERVICE_NAME)</li>
     * <li>host = Database server name (from tns entry HOST)</li>
     * <li>port = Database connection port number (from tns entry PORT)</li>
     * </ul
     * <p/>
     * @param alias database connection alias in a tnsnames file
     * @return Map of key value pairs of alias attributes
     * @throws Exception if no tnsnames entries could be found in any of the usual locations
     */
    @Memoized
    Map getAlias (String alias) {
        //println "OracleTnsnames.instance.getAlias($alias)"
        load()
        if (data != null && alias != null) {
            Set aliasSet = new LinkedHashSet()
            aliasSet << alias
            aliasSet << alias.toUpperCase()
            aliasSet << alias.toLowerCase()
            for (String key in aliasSet) {
                if (data.containsKey(key)) return data[key]
            }
        }
        throw new Exception("tnsname alias [$alias] does not exist")
    }

}