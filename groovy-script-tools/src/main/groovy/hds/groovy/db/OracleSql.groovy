package hds.groovy.db

import groovy.sql.Sql
import groovy.transform.Memoized
import groovy.transform.CompileStatic
import java.sql.Connection
import java.sql.DatabaseMetaData

/**
 * Get configured Oracle database connections
 * Example :
 * <p>
 * <pre>
 * // logon to the default database with the default user
 * // (see the OracleAccounts and OracleTnsnames classes)
 * // selecting the first 10 users printing out their ids
 * OracleSql.connect().eachRow('select * from users', 1, 10) { row ->
 *     println row.users_id
 * }
 * </pre>
 * <p/>
 * <b>Linux Issues:</b><p/>
 * The <b>java.security.egd</b> system property needs to be set to handle the issues
 * with jdbc thin client connections to 11g databases on a linux 64 bit kernel.
 * By default the Oracle jdbc driver uses the <b>/dev/random</b> device to get a
 * random number. This property can be used to override this default.
 * <p/>
 * The problem is that the <b>/dev/random</b> device relies on an <i>Entropy Pool</i> to
 * generate a random number. If I understand what I've read about this, an entropy pool
 * is a buffer of random bits that Linux collects. For some reason, servers
 * that don't have WCS installed don't get enough entropy generated. At least that is
 * what I've experienced. I'm not sure why. When this pool is empty
 * or not full enough, a random number cannot be generated and the driver will
 * not return a connection. If this happens a really long pause occurs,
 * then an error. Not pretty.<p/>
 * Here are 2 possible solutions:<br/>
 *  <ul>
 *  <li>Somehow force linux to generate more entropy. Talk to your friendly
 *  Unix SA for that, and good luck.</li>
 *  <li>Use a different random number generator. Using <b>/dev/urandom</b>
 *  (note the 'u'), even though it is less random, solves the issue.
 *  This is the solution that this class uses.</li>
 *  </ul>
 * I still have not been able to figure out why the Oracle JDBC Driver even needs a
 * random number to be able to get a new connection. Google search gives this
 * solution as an answer to the problem and it says that it does not
 * cause any problems.<p/>
 * If you are interested, read these two articles for more details on this issue:
 * <br/>
 * https://forums.oracle.com/forums/thread.jspa?messageID=3793101
 * <br/> 
 * <br/> 
 * http://www.usn-it.de/index.php/2009/02/20/oracle-11g-jdbc-driver-hangs-blocked-by-devrandom-entropy-pool-empty
 * <br/>
 * <br/>
 * For a good laugh, after reading the second link's article, read response 10.
 * Actually, no, the response is so funny I've decided to paste it here....
 * <p>
 * 
 * Per your recommendation, I installed an older fan right above the server next to a microphone.
 * This is opposed to new Dyson Air Multipliers. We all know that only the wind buffeting caused
 * by the blades will truly engage a random pattern that will allow the entropy daemon to capture
 * the wave patterns properly. By having a bad bearing the noise pattern although repeatable was
 * just random enough to get the entropy daemon garbage cleanup done properly – but occasionally
 * I still lost connections; especially to system 10G. So I then installed a parrot on top of the
 * fan and had it fed by my a data center troll (we call him Ed) at random times throughout
 * the day. In effect when the parrot took a poop the random noise of the sh*t hitting the fan
 * resolved all my connection issues. Including 10G!! You can imagine how happy my manager was to
 * have the end of entropy issues!! However, every now and then I walk into the data center and
 * he has the parrot on his shoulder pretending to be Captain Morgan. He’s always striking that
 * silly pose with his foot on the barrel. To resolve this I put a honey badger in a parrot suit
 * on top the fan – sure enough the random feedings stopped the random lost connections again.
 * However, one day my manager came in with cuts all over his face. I asked him what the heck
 * happened? He said he was making sure the sh*t hit the fan, but when it (the sh*t) looked
 * different he was concerned for Polly (the afore mentioned parrot). When I told him it was really
 * a honey badger we all had a good laugh, and to date we wonder at how when the sh*t hits the
 * fan things work properly. Frankly when things hang we still ask “Is the sh*t hitting the fan?”
 * Around here – most of the time it is. Thanks again for the help. Damn it Ed – what the hell
 * are you doing, go feed Polly. Geeze. I need to outsource him – slacker.
 * 
 * @author en032339
 */
class OracleSql {

    private static final String DRIVER = 'oracle.jdbc.OracleDriver'

    OracleSql() {
        throw new Exception('OracleSql is a static class')
    }

    static Sql connect () {
        OracleAccounts oa = OracleAccounts.instance
        connect(oa.defaultUser, oa.defaultAlias)
    }

    static Sql connect (String alias) {
        connect(OracleAccounts.instance.defaultUser, alias)
    }

    static Sql connect (String user, String alias) {
        Map tns = OracleTnsnames.instance.getAlias(alias)
        String password =  OracleAccounts.instance.getPassword(user, alias)
        connect(user, password, tns.host, tns.port, tns.service)
    }

    static Sql connect (String user, String password, String alias) {
        Map tns = OracleTnsnames.instance.getAlias(alias)
        connect(user, password, tns.host, tns.port, tns.service)
    }

    static Sql connect (String user, String password, String host, String port, String service) {
        Map map = [
            driver : DRIVER,
            user : user,
            password : password,
            url : url(host, port, service)
        ]
        connect(map)
    }

    static Sql connect (Map map) {
        autoCommitOff(Sql.newInstance(map))
    }

    @Memoized
    static String url (String alias) {
        Map tns = OracleTnsnames.instance.getAlias(alias)
        url(tns?.host, tns?.port, tns?.service)
    }

    static String url (String host, String port, String service) {
        "jdbc:oracle:thin:@//${host}:${port}/${service}"
    }

    static String url (String user, String password, String host, String port, String service) {
        "jdbc:oracle:thin:${user}/${password}@//${host}:${port}/${service}"
    }

    static String url (Map data) {
        "jdbc:oracle:thin:${data.user}/${data.password}@//${data.host}:${data.port}/${data.service}"
    }

    static String url (String user, String alias) {
        Map tnsEntry = OracleTnsnames.instance.getAlias(alias)
        String host = tnsEntry.host
        String port = tnsEntry.port
        String service = tnsEntry.service
        String password = OracleAccounts.instance.getPassword(user, service)
        url(user, password, host, port, service)
    }

    // Groovy >= 2.2.0 has issues with calling methods on a java.sql.Connection object.
    // Putting calls into a CompileStatic method seems to get around the problem.

    @CompileStatic
    static Sql autoCommitOff (Sql sql) {
        sql.connection.setAutoCommit(false)
        sql
    }

    @CompileStatic
    static Sql autoCommitOn (Sql sql) {
        sql.connection.setAutoCommit(true)
        sql
    }

    @CompileStatic
    static DatabaseMetaData databaseMetaData (Sql sql) {
        sql.connection.metaData
    }

    static void enable_dbms_output (Sql sql, Integer size = 100000) {
        sql.call('{call dbms_output.enable(?)}', [size])
    }
    
    static List get_dbms_output (Sql sql) {
        List lines = []
        def line_status = 0
        while (line_status == 0) {
            sql.call('{call dbms_output.get_line(?,?)}', [Sql.VARCHAR, Sql.INTEGER]) { line, status ->
                if (status == 0) lines << line
                line_status = status
            }
        }
        lines
    }

}