package script.sql

import script.*
import groovy.transform.Memoized

/**
 * A class that encapsulates Oracle Account Credentials (eg. alias, user, password).
 * The point of this class is to store Oracle account
 * information, most notably passwords,
 * in one place, a centralized configuration file,
 * so that passwords will not be hard coded in any Groovy scripts or thier property files.
 * If all Groovy scripts use this class, no scripts will have to change
 * if we decide to change account information. The only thing that
 * will have to change is the Accounts Configuration file.
 * <p>
 * Normally you would never access this class directly. See the OracleSql class. This class
 * uses OracleAccounts in its <i>connect</i> methods.
 * <p>
 * <b>Code Sample:</b>
 * <ul>
 * <br>This class uses the <tt>@Singleton</tt> annotation so you'll need to get
 * an OracleAccounts instance by accessing either the static <i>getInstance()</i>
 * method or the static <i>instance</i> property.
 * <pre>
 * OracleAccounts accts = OracleAccounts.instance
 * // Note: an alias is a tnsname. Although an alias
 * //   is not necessarily the same as a database name
 * //   here at HDSupply we typically make them the same.
 * String alias = 'ecommdev' // typically obtained from a property file in your app
 * String user = accts.getDefaultUser(alias)
 * String password = accts.getPassword(user, alias)
 * </pre>
 * </ul>
 * <p>
 * <b>Accounts configuration file</b>
 * <ul>
 * This properties file details information about alias', users aand passwords.
 * An alias can have a default user. A host can have a default alias. An alias and
 * a user has a password. The defaults are set so that you do not have to supply
 * anything to get a default connection on a host. The host is determined by the
 * Host class.
 * </ul>
 * <ul>
 * format:
 *     <br/><br/>
 *     default_alias={alias}
 *     <br/>
 *     default_user.{alias}={user}
 *     <br/>
 *     password.{alias}.{user}={password}
 * </ul>
 * <ul>
 * example:
 *     <br/><br/>
 *     default_alias=ecommmdev
 *     <br/>
 *     default_user.ecommdev=tauser02
 *     <br/>
 *     password.ecommdev.tauser02=secret
 * </ul>
 * <ul>
 * This file is called <i>.accounts.ora</i>, 
 * and is looked for in your app directory and then the users home directory.
 * These files are load in this order and Properties do not override.
 * If no files are found an exception will be thrown.
 * </ul>
 */
@groovy.util.logging.Log4j
@Singleton
class OracleAccounts {

    static final String _ACCOUNTS_ORA = '.accounts.ora'

    Set filesLoaded = []
    Map data
    
    synchronized void load (File file) {
        if (file.isFile()) {
            data ?: [:]
            Properties2 props = new Properties2()
            props.load(new FileInputStream(file))
            props.each { String k, String v -> data[k] = v }
            filesLoaded << file
        }
    }

    synchronized void load () {
        if (data == null) {
            data = [:]
            // look for a properties file in the app directory
            if (Env.scriptFile != null) {
                load(new File(Env.scriptFile.parentFile, _ACCOUNTS_ORA))
            }
            // look for a properties file in the users home directory
            load(new File(Env.getHomeDir(), _ACCOUNTS_ORA))
        }
    }

    /**
     * Get the default password 
     * @return password of the default user and the default alias
     */
    String getPassword () {
        getPassword(getDefaultUser(), getDefaultAlias())
    }

    @Memoized
    String getPassword (String type, String user, String alias) {
        load()
        String password
        if (type && user && alias) {
            Set keys = [] as LinkedHashSet
            keys << "${type}.${alias}.${user}" as String
            keys << "${type}.${alias.toUpperCase()}.${user.toUpperCase()}" as String
            keys << "${type}.${alias.toUpperCase()}.${user.toLowerCase()}" as String
            keys << "${type}.${alias.toLowerCase()}.${user.toLowerCase()}" as String
            keys << "${type}.${alias.toLowerCase()}.${user.toUpperCase()}" as String
            for (key in keys) {
                password = data[key]
                if (password != null) {
                    break
                }
            }
            if (password == null) {
                log.warn "cannot find [${type}] using alias [${alias}] user [${user}], tried these keys $keys"
            }
        }
        password
    }
    
    String getPassword (Map map) {
        getPassword(map.user, map.alias)
    }

    /**
     * Get a password for alias using default user
     * @param alias for determining password
     * @return password of the input user for the default alias
     */
    String getPassword (String alias) {
        getPassword(getDefaultUser(), alias)
    }

    /**
     * Get a password
     * @param user for determining password
     * @param alias (tnsname) for determining password
     * @return password of the input user and the input alias
     */
    String getPassword (String user, String alias) {
        getPassword('password', user, alias)
    }

    /**
     * Get a wcs encrypted password
     * @param user for determining password
     * @param alias (tnsname) for determining password
     * @return password of the input user and the input alias
     */
    String getEncpwd (String user, String alias) {
        getPassword('encpwd', user, alias)
    }

    String getEncpwd (Map map) {
        getPassword('encpwd', map.user, map.alias)
    }

    String getEncpwd (String alias) {
        getEncpwd(getDefaultUser(), alias)
    }

    String getEncpwd () {
        getEncpwd(getDefaultUser(), getDefaultAlias())
    }

    /**
     * Get the default user of the default alias
     * @return default user for the default alias (tnsname)
     * @throws Exception if the accounts configuration has problems loading
     *  or if the <i>default_alias</i> value is missing
     */
    String getDefaultUser() {
        getDefaultUser(getDefaultAlias())
    }

    /**
     * Get the default user for a given alias
     * @param alias (tnsname) to lookup the default user
     * @return user name or null if the user name cannot be determined
     */
    @Memoized
    String getDefaultUser (String alias) {
        load()
        String user
        if (alias) {
            Set keys = [] as LinkedHashSet
            keys << "default_user.${alias}" as String
            keys << "default_user.${alias.toUpperCase()}" as String
            keys << "default_user.${alias.toLowerCase()}" as String
            for (key in keys) {
                user = data[key]
                if (user != null) {
                    break
                }
            }
            if (user == null) {
                log.warn "cannot find default user using alias ${alias}, tried these keys $keys"
            }
        }
        user
    }

    /**
     * Get the default alias for your current host
     * @return a database alias name or null if the alias cannot be determined
     */
    @Memoized
    String getDefaultAlias () {
        load()
        final String key = 'default_alias'
        String alias = data[key]
        if (alias == null) {
            log.warn "cannot get the default alias, property [$key] does not exist"
        }
        alias
    }

    /**
     * Get a Map of connection properties for a user and an alias (tnsname)
     * @param user
     * @param alias (tnsname)
     * @return Map of account properties. Keys are 'password' and 'encpwd'
     */
    @Memoized
    Map getAccount (String user, String alias) {
        Map account = [password: null, encpwd: null]
        if (user && alias) {
            account['password'] = getPassword(user, alias)
            account['encpwd'] = getEncpwd(user, alias)
        }
        account
    }
    
}