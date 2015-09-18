package hds.groovy.wc

import hds.groovy.Proc
import hds.groovy.Props
import hds.groovy.db.OracleAccounts
import hds.groovy.db.OracleSql

/**
 * Run IBM WCS stagprop
 * 
 * @author en032339
 */
@groovy.transform.Canonical
@groovy.util.logging.Log4j
class Stagprop {

    String script
    String dbtype = 'Oracle'
    String scope  = '_all_'
    String transaction = 'one' // table
    String batchsize
    String sourcedb
    String sourcedbUser
    String sourcedbPasswd
    String destdb
    String destdbUser
    String destdbPasswd
    boolean ok2run = false

    /**
     * Initialize a Stagprop object with properties loaded in a hds.groovy.Props instance
     * <p/>
     * <b>Required Properties</b><br/>
     * <ul>
     * <li>stagprop.script = <i>full path to IBM WCS script that runs stagprop</i></li>
     * <li>stagprop.sourcedb = <i>source database alias (from Oracle tnsnames.ora)</i></li>
     * <li>stagprop.sourcedb_user = <i>source database user</i></li>
     * <li>stagprop.destdb = <i>destination database alias (from Oracle tnsnames.ora)</i></li>
     * <li>stagprop.destdb_user = <i>destination database user</i></li>
     * </ul>
     * <p/>
     * <b>Optional Properties</b><br/>
     * <ul>
     * <li>stagprop.dbtype = default is 'Oracle'</li>
     * <li>stagprop.scope = default is '_all_'</li>
     * <li>stagprop.batchsize = no default</li>
     * <li>stagprop.transaction = default is 'one'</li>
     * </ul>
     * <p/>
     * The OracleAccounts class is used to determine sourcedbPasswd and destdbPasswd
     * 
     * @return this
     * @see Props
     * @see OracleAccounts
     */
    Stagprop initWithProps () {
        Props props = Props.instance
        ok2run = props.getBooleanProp('stagprop.ok2run')
        script = props.getProp('stagprop.script')
        sourcedb = props.getProp('stagprop.sourcedb')
        sourcedbUser = props.getProp('stagprop.sourcedb_user')
        if (sourcedb != null && sourcedb.length() > 0 && sourcedbUser != null && sourcedbUser.length() > 0) {
            sourcedbPasswd = OracleAccounts.instance.getPassword(sourcedbUser, sourcedb)
        }
        destdb = props.getProp('stagprop.destdb')
        destdbUser = props.getProp('stagprop.destdb_user')
        if (destdb != null && destdb.length() > 0 && destdbUser != null && destdbUser.length() > 0) {
            destdbPasswd = OracleAccounts.instance.getPassword(destdbUser, destdb)
        }
        // optional props
        dbtype = props.getProperty('stagprop.dbtype', dbtype)
        scope = props.getProperty('stagprop.scope', scope)
        batchsize = props.getProperty('stagprop.batchsize')
        transaction = props.getProperty('stagprop.transaction', transaction)
        return this
    }

    void execute () {
        if (!ok2run) {
            log.warn 'stagprop not configured to run'
            return
        }

        List cmd = [
            script,
            '-dbtype', dbtype,
            '-scope', scope,
            '-transaction', transaction,
            '-sourcedb', sourcedb,
            '-sourcedb_user', sourcedbUser,
            '-sourcedb_passwd', sourcedbPasswd,
            '-destdb', destdb,
            '-destdb_user', destdbUser,
            '-destdb_passwd', destdbPasswd,
        ]
        if (batchsize != null) {
            cmd << '-batchsize'
            cmd << batchsize
        }

        validateCommandArguments(cmd)

        log.info "running [${cmd.join(' ')}]"
        Process proc = cmd.execute()
        proc.consumeProcessOutput()
        proc.waitFor()
        int exitValue = proc.exitValue()
        if (exitValue != 0) {
            String errmsg = "execution of $cmd failed with exit value = ${exitValue}"
            if (exitValue == 121) {
                log.error "${errmsg}, this is not considered a fatal error"
            }
            else {
                throw new Exception(errmsg)
            }
        }

    }

    private void validateCommandArguments (List cmd) {
        boolean validcmd = true
        cmd.eachWithIndex { String arg, int idx ->
            if (idx == 0) { // first arg should be the script being executed
                if (!new File(arg).isFile()) {
                    log.error "script file [${arg}] does not exist"
                    validcmd = false
                }
                return // next argument
            }
            if ((idx & 1) == 0) return // next arg, skip even args 2,4,6...
            String argval = cmd[idx + 1]
            if (arg.startsWith('-')) {
                if (argval == null || argval.length() == 0) {
                    log.error "argument [$idx] key [$arg] value is missing"
                    validcmd = false
                }
                else if (arg == '-batchsize' && argval =~ /\D/) {
                    log.error "argument [$idx] key [$arg] value [$argval] must be a number"
                    validcmd = false
                }
            }
            else {
                log.error "argument [$idx] key [$arg] value [$argval]"
                validcmd = false
            }
        }
        if (sourcedb == destdb) {
            log.error "stagprop source [$sourcedb] and destination [$destdb] databases must be different $cmd"
            validcmd = false
        }
        if (!validcmd) {
            throw new Exception("invalid command $cmd")
        }
    }

}