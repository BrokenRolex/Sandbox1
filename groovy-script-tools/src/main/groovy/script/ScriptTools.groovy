package script

import org.apache.log4j.Logger
import groovy.time.TimeCategory as TC

/**
 * Tools for running scripts using this framework.
 * <p>
 * 
 */
class ScriptTools {

    static Script script
    static Logger log
    static Lock lock
    static Boolean announce
    static List errors
    static List propFilesLoaded
    static Cli cli
    static Long scriptStartTime
    static Long userStartTime
    static String email

    ScriptTools() {
        throw new Exception('ScriptTools is a static class')
    }

    static void begin (Script s) {
        script = s
        errors = []
        try {
            scriptStartTime = System.currentTimeMillis()
            //
            Meta.addFileMethods()
            Meta.addStringMethods()
            //
            // Create a console appender for the RootLogger
            // so we can at least see warn or error messages
            // before proper logging gets setup below.
            LogMgr.addDefaultConsoleAppender() //
            log = LogMgr.getLogger(ScriptTools.class)
            LogMgr.createJulBridge()
            //
            Env.findScriptFile(script) // can throw an exception
            //
            // Next tasks must be done in this order...
            loadProps() // 1. load properties
            initCli() // 2. initialize cli (uses properties)
            initLogger() // 3. initialize the logger (uses properties and cli)
            if (errors.size() > 0) {
                fatal("framework startup errors = ${errors.join(', ')}")
            }

            email = Props.instance.getProperty('logger.email')
            List cliInfo = cli.info()
            log.info 'begin'
            if (announce) {
                email('begin', cliInfo.join("\n"))
            }
            cliInfo.each { log.info it }

            acquireRunLock() // (uses properties)
            userStartTime = System.currentTimeMillis()
        }
        catch (e) {
            println e
            errors << e.message
            fatal("framework startup errors [${errors.join(', ')}]")
        }
    }

    /**
     load properties.
     If you had a script called /a/dir/run and ran it, these properties 
     would be loaded, in this order, if they exist.
     1. load /a/dir/run.properties
     2. load /a/dir/dir.properites
     3. load /homedir/application.properties
     */
    static void loadProps () {
        Props props = Props.instance
        String ext = '.properties'
        Set files = [] as LinkedHashSet
        files << new File(Env.scriptFile.parentFile, (Env.scriptFile.name - '.groovy') + ext)
        files << new File(Env.scriptFile.parentFile, Env.scriptFile.parentFile.name + ext)
        files << new File(Env.homeDir, 'application' + ext)
        propFilesLoaded = []
        files.each { file ->
            if (file.isFile()) {
                try {
                    props.load(file)
                    propFilesLoaded << file
                }
                catch (e) {
                    String errm = "error loading properties from [$file] reason [${e.message}]"
                    log.error errm
                    errors << errm
                }
            }
        }
        props.addDefaultProps()
        props.interpolate().each {
            log.error it
            errors << it
        }
        props.validate().each {
            log.error it
            errors << it
        }
    }

    static void initCli () {
        cli = new Cli(script)
        cli.createDefaultOptions()
        cli.createCustomOptions()
        cli.parse()
        if (cli.opt == null || cli.opt.help) {
            cli.usage()
            System.exit 1
        }
    }

    static void initLogger () {
        LogMgr.addConsoleAppender()
        LogMgr.addFileAppender()
        LogMgr.overrideLoggerLevels()
        LogMgr.setInfoLevel()
        if (cli.opt.debug) {
            LogMgr.setDebugLevel()
        }
        if (!cli.opt.verbose && !cli.opt.debug && LogMgr.fileAppenderExists()) {
            LogMgr.removeConsoleAppender()
        }
        log = LogMgr.getLogger(ScriptTools.class)
        announce = Props.instance.getBooleanProp('ScriptTools.announce', false)
        // turn off announce for help or test
        if (announce && (cli.opt.help || cli.opt.test)) {
            announce = false
        }
    }

    static void releaseRunLock () {
        if (lock) {
            try { lock.release() } finally { lock = null }
        }
    }

    static void acquireRunLock () {
        if (Props.instance.getBooleanProp('ScriptTools.runlock', false)) {
            try {
                lock = new Lock().acquireHiddenForBin()
            }
            catch (Exception e) {
                throw new Exception("cannot acquire a run lock [${e.message}]")
            }
        }
    }

    static void end () {
        releaseRunLock()
        def runTime = TC.minus(new Date(), new Date(scriptStartTime))
        debug()
        log?.info "end [$runTime]"
        if (announce) {
            email('end', runTime)
        }
    }

    static void debug () {
        try {
            Boolean debug = Props.instance.getBooleanProp('ScriptTools.debug', false)
            if (log && (debug || log.isDebugEnabled())) {
                long userEndTime = System.currentTimeMillis()
                LogMgr.setDebugLevel()
                Long jvmStartTime = java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime()
                Date d1 = new Date(jvmStartTime)
                String fmt = 'yyyy-MM-dd HH:mm:ss.SSS'
                log.debug 'jvm start time [' + d1.format(fmt) + ']'
                d1.setTime(scriptStartTime)
                log.debug 'framework start time [' + d1.format(fmt) + ']'
                d1.setTime(userStartTime)
                log.debug 'user start time [' + d1.format(fmt) + ']'
                d1.setTime(userEndTime)
                log.debug 'user end time [' + d1.format(fmt) + ']'
                d1.setTime(scriptStartTime)
                Date d2 = new Date(jvmStartTime)
                String jvmStartDuration  = TC.minus(d1, d2)
                d2.setTime(userStartTime)
                String frameworkStartDuration = TC.minus(d2, d1)
                log.debug 'groovy startup [' + jvmStartDuration + '] jvm start -> groovy compile'
                log.debug 'framework startup [' + frameworkStartDuration+ '] load properties + setup logging + parse options'
                d1.setTime(userEndTime)
                d2.setTime(jvmStartTime)
                String totalRunDuration = TC.minus(d1, d2)
                d2.setTime(userStartTime)
                String userRunDuration = TC.minus(d1, d2)
                log.debug 'user [' + userRunDuration + '] user start time -> user end time'
                log.debug 'total [' + totalRunDuration + '] jvm start time -> user end time'
            }
        }
        catch (e) {
            log.error e.message
        }
    }

    static void email (String subject, String message) {
        if (email) {
            Mailer.send {
                delegate.to(email)
                delegate.subject(subject)
                delegate.message(message)
            }
        }
    }

    static void fatal (error) {
        if (log) {
            log.fatal error
        }
        end()
        System.exit 1
    }

}