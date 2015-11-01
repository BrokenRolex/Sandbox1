package script

import org.apache.log4j.Logger
import groovy.time.TimeCategory
import org.apache.log4j.ConsoleAppender
import org.apache.log4j.RollingFileAppender
import org.apache.log4j.PatternLayout
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.slf4j.bridge.SLF4JBridgeHandler

class Smgr {

    static final String CONSOLE_APPENDER_NAME = 'appender.console'
    static final String FILE_APPENDER_NAME = 'appender.file'
    static final String LOGFILE_EXT = '.log'
    static final String CONSOLE_PATTERN_LAYOUT = '%d{HH:mm:ss,SSS} %-5p %c{1} - %m%n'
    static final String FILE_PATTERN_LAYOUT = '%d %-5p %c{1} - %m%n'
    static final String MAX_FILE_SIZE = '5MB'
    static final Integer MAX_BACKUP_INDEX = 2
    static final Map LEVELS = ['TRACE': Level.TRACE,'DEBUG': Level.DEBUG,'INFO':Level.INFO,'WARN':Level.WARN,'ERROR':Level.ERROR,'FATAL':Level.FATAL]

    ScriptWrapper script
    Logger log
    Lock lock
    Boolean announce
    List errors
    List propFilesLoaded
    Cli cli
    Long scriptStartTime
    Long userStartTime

    void begin (ScriptWrapper s) {
        script = s
        errors = []
        try {
            scriptStartTime = System.currentTimeMillis()
            addDefaultConsoleAppender()
            createJulBridge()
            log = getLogger(Smgr.class)
            script.Log = log
            script.scriptFile = Env.findScriptFile(script)
            //FileMetaClass.add()
            //StringMetaClass.add()
            //DateMetaClass.add()
            // do not change the order of the  next 4 tasks
            _load_property_files()
            _parse_command_line_options() // uses properties
            _setup_logging() // uses cli and properties
            _acquire_runlock()
            if (errors.size() > 0) {
                fatal("ScriptManager startup errors ${errors}")
            }
            log.info 'begin'
            cli.info().with {
                it.each { log.info it }
                if (announce) { _email('begin', it.join("\n")) }
            }
            script.stopWatch = { id, Closure c ->
                Date start = new Date()
                c.call()
                log.info "$id : ${TimeCategory.minus(new Date(), start)}"
            }
            script.title = script.props.getProperty('program.title','?')
            script.env = Env.name ?: '?'
            script.host = Env.hostname ?: '?'
            //"[${script.title}] [${script.env}] [${script.host}:${script.scriptFile.path}]" 
            userStartTime = System.currentTimeMillis()
        }
        catch (e) {
            fatal(e.message)
        }
    }

    private void _acquire_runlock() {
        if (Props.instance.getBooleanProp('ScriptTools.runlock', false)) {
            try {
                lock = new Lock().acquireHiddenForBin()
            }
            catch (Exception e) {
                String errmsg  = "cannot acquire a run lock [${e.message}]"
                errors << errmsg
                log.error errmsg
            }
        }
    }

    /**
     load properties.
     If you had a script called /a/dir/run and ran it, these properties 
     would be loaded, in this order, if they exist.
     1. load /a/dir/run.properties
     2. load /a/dir/dir.properites
     2. load /a/dir/application.properites
     3. load /homedir/application.properties
     */
    private void _load_property_files () {
        Props props = Props.instance
        String ext = '.properties'
        Set files = [] as LinkedHashSet
        files << new File(Env.scriptFile.parentFile, (Env.scriptFile.name - '.groovy') + ext)
        files << new File(Env.scriptFile.parentFile, Env.scriptFile.parentFile.name + ext)
        files << new File(Env.scriptFile.parentFile, 'application'+ ext)
        files << new File(Env.homeDir, 'application' + ext)
        propFilesLoaded = []
        files.each { File file ->
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
        script.props = props
    }

    private void _parse_command_line_options () {
        cli = new Cli(script)
        script.cli = cli
        if (cli.opt == null || cli.opt.help) {
            cli.usage()
            System.exit 1
        }
    }

    private void _setup_logging () {
        addFileAppender()
        if (cli.opt.verbose || cli.opt.debug) {
            addConsoleAppender()
        }
        overrideLoggerLevels()
        setInfoLevel()
        if (cli.opt.debug) {
            setDebugLevel()
        }
        log = getLogger(Smgr.class)
        script.log = log
        announce = Props.instance.getBooleanProp('ScriptTools.announce', false)
        if (announce && (cli.opt.help || cli.opt.test)) {
            announce = false // turn off announce for help or test
        }
    }

    private void _email (String subject, String message) {
        String emailto = Props.instance.getProperty('logger.email')
        if (emailto) {
            Mailer.send {
                delegate.to(emailto)
                delegate.subject(subject)
                delegate.message(message)
            }
        }
    }

    void end () {
        if (lock) { try { lock.release() } finally { lock = null } } // release run lock
        def runTime = TimeCategory.minus(new Date(), new Date(scriptStartTime))
        try {
            Boolean debug = Props.instance.getBooleanProp('ScriptTools.debug', false)
            if (log && (debug || log.isDebugEnabled())) {
                long userEndTime = System.currentTimeMillis()
                setDebugLevel()
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
                String jvmStartDuration  = TimeCategory.minus(d1, d2)
                d2.setTime(userStartTime)
                String frameworkStartDuration = TimeCategory.minus(d2, d1)
                log.debug 'groovy startup [' + jvmStartDuration + '] jvm start -> groovy compile'
                log.debug 'framework startup [' + frameworkStartDuration+ '] load properties + setup logging + parse options'
                d1.setTime(userEndTime)
                d2.setTime(jvmStartTime)
                String totalRunDuration = TimeCategory.minus(d1, d2)
                d2.setTime(userStartTime)
                String userRunDuration = TimeCategory.minus(d1, d2)
                log.debug 'user [' + userRunDuration + '] user start time -> user end time'
                log.debug 'total [' + totalRunDuration + '] jvm start time -> user end time'
            }
        }
        catch (e) {
            log.error e.message
        }
        if (log) { log.info "end [$runTime]" }
        if (announce) { _email('end', runTime) }
    }
    
    void fatal (error) {
        if (log) { log.fatal error }
        end()
        System.exit 1
    }

    /**
     * Create a JUL to Slf4j bridge (Slf4J uses Log4j)
     */
    void createJulBridge () {
        try {
            java.util.logging.LogManager.getLogManager().reset()
            SLF4JBridgeHandler.removeHandlersForRootLogger()
            SLF4JBridgeHandler.install()
        }
        catch (Exception e) {
            String errm = "jul->slf4j bridge install error [${e.message}]"
            throw new Exception(errm)
        }
    }

    // look for class loggers in properties to override level
    // property pattern to look for = logger.override.{class}={level}
    void overrideLoggerLevels() {
        // don't let email class logging be finer than info
        Logger.getLogger('com.sun.mail').setLevel(Level.INFO)
        Logger.getLogger('com.sun.activation').setLevel(Level.INFO)
        Logger.getLogger('javax.mail').setLevel(Level.INFO)
        Logger.getLogger('javax.activation').setLevel(Level.INFO)
        //Logger.getLogger('groovy.sql').setLevel(Level.INFO)
        String logger_override_ = 'logger.override.'
        Props.instance.each { String key, String val ->
            if (key.startsWith(logger_override_)) {
                String level = val.toUpperCase()
                if (LEVELS.containsKey(level)) {
                    String clazz = key - logger_override_
                    Logger logger = Logger.getLogger(clazz)
                    logger.setAdditivity(true) // necessary ?
                    logger.setLevel(LEVELS[level])
                }
            }
        }
    }

    void addDefaultConsoleAppender () {
        System.setProperty('log4j.defaultInitOverride', 'true')
        Logger logger = Logger.getRootLogger()
        logger.removeAppender(CONSOLE_APPENDER_NAME)
        PatternLayout layout = new PatternLayout('%-5p %c{1} - %m%n')
        ConsoleAppender appender = new ConsoleAppender(layout)
        appender.setName(CONSOLE_APPENDER_NAME)
        appender.activateOptions()
        logger.addAppender(appender)
        logger.setLevel(Level.WARN)
    }

    void addConsoleAppender () {
        Logger logger = Logger.getRootLogger()
        logger.removeAppender(CONSOLE_APPENDER_NAME)
        String layoutString = Props.instance.getProperty('logger.console_pattern_layout', CONSOLE_PATTERN_LAYOUT)
        PatternLayout layout = new PatternLayout(layoutString)
        ConsoleAppender appender = new ConsoleAppender(layout)
        appender.setName(CONSOLE_APPENDER_NAME)
        appender.activateOptions()
        logger.addAppender(appender)
    }

    void addFileAppender () {
        Logger logger = Logger.getRootLogger()
        logger.removeAppender(FILE_APPENDER_NAME)
        Props props = Props.instance
        String name = Env.scriptName + LOGFILE_EXT
        File logDir = Env.scriptFile.parentFile
        File file = new File(logDir, name)
        String logger_file_key = 'logger.file'
        if (props.containsKey(logger_file_key)) {
            String logger_file = props.getProperty(logger_file_key)
            if (logger_file.endsWith(LOGFILE_EXT)) {
                file = new File(logger_file)
            }
            else {
                File dir = new File(logger_file)
                if (dir.isDirectory()) {
                    file = new File(dir, name)
                }
            }
        }
        ScriptFileAppender appender = new ScriptFileAppender()
        appender.setEmailTo(Props.instance.getProperty('logger.email'))
        appender.setName(FILE_APPENDER_NAME)
        appender.setFile(file.path)
        appender.setMaxFileSize(props.getProperty('logger.max_file_size', MAX_FILE_SIZE))
        appender.setMaxBackupIndex(props.getIntProp('logger.max_backup_index', MAX_BACKUP_INDEX))
        String layoutString = props.getProperty('logger.file_pattern_layout', FILE_PATTERN_LAYOUT)
        appender.setLayout(new PatternLayout(layoutString))
        appender.activateOptions()
        logger.addAppender(appender)
    }

    File getLogFile () {
        File logFile = null
        RollingFileAppender fileAppender = (RollingFileAppender) Logger.getRootLogger().getAppender(FILE_APPENDER_NAME)
        if (fileAppender) {
            String file = fileAppender.getFile()
            if (file) {
                logFile = new File(file)
            }
        }
        logFile
    }
    
    void setLevel (String level) {
        switch (level) {
            case 'TRACE':
            case 'FINEST':
                setTraceLevel()
                break
            case 'DEBUG':
            case 'FINER':
            case 'FINE':
                setDebugLevel()
                break
            case 'INFO':
                setInfoLevel()
                break
            case 'WARN':
            case 'WARNING':
                setWarnLevel()
                break
            case 'ERROR':
            case 'SEVERE':
                setErrorLevel()
                break
            case 'FATAL':
                setFatalLevel()
        }
    }

    void setTraceLevel () {
        Logger.getRootLogger().setLevel(Level.TRACE)
    }

    void setDebugLevel () {
        Logger.getRootLogger().setLevel(Level.DEBUG)
    }

    void setInfoLevel () {
        Logger.getRootLogger().setLevel(Level.INFO)
    }

    void setWarnLevel () {
        Logger.getRootLogger().setLevel(Level.WARN)
    }

    void setErrorLevel () {
        Logger.getRootLogger().setLevel(Level.ERROR)
    }

    void setFatalLevel () {
        Logger.getRootLogger().setLevel(Level.FATAL)
    }

    Logger getLogger (Object obj) {
        Logger.getLogger(obj.getClass())
    }

    Logger getLogger (Class clazz) {
        Logger.getLogger(clazz)
    }

    Logger getLogger(String name) {
        Logger.getLogger(name)
    }
    
}