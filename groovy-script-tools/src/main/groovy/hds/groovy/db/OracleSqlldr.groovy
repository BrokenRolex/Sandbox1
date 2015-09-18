package hds.groovy.db

import hds.groovy.Env
import hds.groovy.Proc

@groovy.util.logging.Log4j
class OracleSqlldr {

    File dataFile
    File logFile
    File badFile
    File controlFile
    File discardFile
    File archiveDir
    Integer discardmax
    Integer skip
    Integer load
    Integer errors = 9999999
    Integer rows
    Integer bindsize
    Boolean silent = true
    String dbuser
    String dbname // tnsname
    String userid // user/pass@tnsname
    Integer rc // sqlldr exit status

    void load (File dataFile) {
        log.info "loading [$dataFile]"
        rc = null
        File dir = Env.scriptFile.parentFile
        logFile = new File(dir, dataFile.name + '.log')
        logFile.delete()
        badFile = new File(dir, dataFile.name + '.bad')
        badFile.delete()
        discardFile = new File(dir, dataFile.name + '.disc')
        discardFile.delete()
        //
        if (controlFile == null || !controlFile.isFile()) {
            throw new Exception("cannot find sqlldr control file [$controlFile]")
        }
        Map env = System.getenv()
        if (!env.containsKey('ORACLE_HOME')) {
            throw new Exception('Environment var [ORACLE_HOME] does not exist')
        }
        String oracle_home = env['ORACLE_HOME']
        File sqlldrFile = new File(new File(oracle_home, 'bin'), 'sqlldr')
        if (sqlldrFile.isFile() == false) {
            throw new Exception("cannot find sqlldr executable [$sqlldrFile]")
        }
        //
        if (userid == null) {
             if (dbuser && dbname) {
                 def oacct = OracleAccounts.instance.getAccount(dbuser, dbname)
                 userid = "${dbuser}/${oacct['password']}@${dbname}"
             }
        }
        //
        def cmd = [
            sqlldrFile.path,
            'USERID=' + userid,
            'CONTROL=' + controlFile.path,
            'DATA=' + dataFile.path,
            'LOG=' + logFile.path,
            'BAD=' + badFile.path,
            'DISCARD=' + discardFile.path,
        ]
        if (silent) {
            cmd << 'SILENT=HEADER'
            cmd << 'SILENT=FEEDBACK'
            cmd << 'SILENT=ERRORS'
            cmd << 'SILENT=DISCARDS'
            cmd << 'SILENT=PARTITIONS'
        }
        if (errors != null) {
            cmd << ('ERRORS=' + errors)
        }
        if (skip != null && skip > 0) {
            cmd << ('SKIP=' + skip)
        }
        if (rows != null) {
            cmd << ('ROWS=' + rows)
        }
        if (discardmax != null) {
            cmd << ('DISCARDMAX=' + discardmax)
        }
        if (bindsize != null) {
            cmd << ('BINDSIZE=' + bindsize)
        }
        log.debug "running [${cmd.join(' ')}]"
        rc = Proc.run(cmd)
        log.debug "sqlldr rc [$rc]"
        if (archiveDir != null && archiveDir.isDirectory()) {
            log.info "moving data file [$dataFile] to [$archiveDir]"
            dataFile = dataFile.moveTo(archiveDir)
            if (logFile.isFile()) {
                log.info "found log file [$logFile] moving to [$archiveDir]"
                logFile = logFile.moveTo(archiveDir)
            }
            if (badFile.isFile()) {
                log.info "found bad file [$badFile] moving to [$archiveDir]"
                badFile = badFile.moveTo(archiveDir)
            }
            if (discardFile.isFile()) {
                log.info "found discard file [$discardFile] moving to [$archiveDir]"
                discardFile = discardFile.moveTo(archiveDir)
            }
        }
        if (rc != 0) {
            String msg = "load of [$dataFile] failed with rc [$rc]"
            msg += "\nwhen running [${cmd.join(' ')}]"
            if (logFile.isFile()) msg += "\nlog file [$logFile]"
            if (badFile.isFile()) msg += "\nbad file [$badFile]"
            if (discardFile.isFile()) msg += "\ndiscard file [$discardFile]"
            msg += "\nPossible problems include..."
            if (rc == 1 || rc > 2) { // EX_FAIL or EX_FTL
                msg += "\n1. Command-line syntax errors"
                msg += "\n2. Oracle errors nonrecoverable for SQL*Loader"
                msg += "\n3. Operating system errors"
            }
            else if (rc == 2) { // EX_WARN
                msg += "\n1. All or some rows rejected"
                msg += "\n2. All or some rows discarded"
                msg += "\n3. Discontinued load"
            }
            log.error msg
        }
    }
}
