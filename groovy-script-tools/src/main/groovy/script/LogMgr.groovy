package script

import org.apache.log4j.ConsoleAppender
import org.apache.log4j.RollingFileAppender
import org.apache.log4j.PatternLayout
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.slf4j.bridge.SLF4JBridgeHandler as BridgeHandler

class LogMgr {

    static final String CONSOLE_APPENDER_NAME = 'appender.console'
    static final String FILE_APPENDER_NAME = 'appender.file'
    static final String EMAIL_APPENDER_NAME = 'appender.email'
    static final String LOGFILE_EXT = '.log'
    //static final String CONSOLE_PATTERN_LAYOUT = '%d{HH:mm:ss,SSS} %-5p %c{1} - %m%n'
    static final String CONSOLE_PATTERN_LAYOUT = '%d{HH:mm:ss,SSS} %-5p %c - %m%n'
    static final String FILE_PATTERN_LAYOUT = '%d %-5p %c{1} - %m%n'
    static final String MAX_FILE_SIZE = '5MB'
    static final int MAX_BACKUP_INDEX = 2
    static final Map levels = ['TRACE': Level.TRACE,'DEBUG': Level.DEBUG,'INFO':Level.INFO,'WARN':Level.WARN,'ERROR':Level.ERROR,'FATAL':Level.FATAL]

    static void addDefaultConsoleAppender () {
        // tell log4j not to initialize... we will do it manually
        System.setProperty('log4j.defaultInitOverride', 'true')
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout('%-5p %c{1} - %m%n'))
        appender.setName(CONSOLE_APPENDER_NAME)
        appender.activateOptions()
        Logger logger = Logger.getRootLogger()
        logger.addAppender(appender)
        logger.setLevel(Level.WARN)
    }

    static void removeAllAppenders () {
        Logger.getRootLogger().removeAllAppenders()
    }

    /**
     * Create a JUL to Slf4j bridge (Slf4J uses Log4j)
     */
    static void createJulBridge () {
        try {
            java.util.logging.LogManager.getLogManager().reset()
            BridgeHandler.removeHandlersForRootLogger()
            BridgeHandler.install()
        }
        catch (Exception e) {
            String errm = "jul->slf4j bridge install error [${e.message}]"
            throw new Exception(errm)
        }
    }

    // look for class loggers in properties to override level
    // property pattern to look for = logger.override.{class}={level}
    static void overrideLoggerLevels() {
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
                if (levels.containsKey(level)) {
                    String clazz = key - logger_override_
                    Logger logger = Logger.getLogger(clazz)
                    logger.setAdditivity(true) // necessary ?
                    logger.setLevel(levels[level])
                }
            }
        }
    }

    static void addConsoleAppender () {
        try {
            Logger.getRootLogger().removeAppender(CONSOLE_APPENDER_NAME)
            String layoutString = Props.instance.getProperty('logger.console_pattern_layout', CONSOLE_PATTERN_LAYOUT)
            ConsoleAppender appender = new ConsoleAppender(new PatternLayout(layoutString))
            appender.setName(CONSOLE_APPENDER_NAME)
            appender.activateOptions()
            Logger.getRootLogger().addAppender(appender)
        }
        catch (Exception e) {
            throw new Exception("failed to setup a console appender [${e.message}]")
        }
    }

    static void setVerbose () {
        addConsoleAppender()
    }

    static void addFileAppender () {
        try {
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
            Logger.getRootLogger().removeAppender(FILE_APPENDER_NAME)
            ScriptToolsRollingFileAppender appender = new ScriptToolsRollingFileAppender()
            if (Props.instance.containsKey('logger.email')) {
                appender.setEmailList(Props.instance.getListProp('logger.email'))
            }
            appender.setName(FILE_APPENDER_NAME)
            appender.setFile(file.path)
            appender.setMaxFileSize(props.getProperty('logger.max_file_size', MAX_FILE_SIZE))
            appender.setMaxBackupIndex(props.getIntProp('logger.max_backup_index', MAX_BACKUP_INDEX))
            String layoutString = props.getProperty('logger.file_pattern_layout', FILE_PATTERN_LAYOUT)
            appender.setLayout(new PatternLayout(layoutString))
            appender.activateOptions()
            Logger.getRootLogger().addAppender(appender)
        }
        catch (Exception e) {
            String errm = "failed to setup a file appender [${e.message}]"
            throw new Exception(errm)
        }
    }

    static Boolean appenderExists (String name) {
        for (def appender in Logger.getRootLogger().getAllAppenders()) {
            if (appender.name == name) {
                return  true
            }
        }
        false
    }

    static boolean consoleAppenderExists () {
        appenderExists(CONSOLE_APPENDER_NAME)
    }

    static boolean fileAppenderExists () {
        appenderExists(FILE_APPENDER_NAME)
    }

    static boolean emailAppenderExists () {
        appenderExists(EMAIL_APPENDER_NAME)
    }

    static File getLogFile () {
        File logFile = null
        if (fileAppenderExists()) {
            RollingFileAppender fileAppender = (RollingFileAppender) Logger.getRootLogger().getAppender(FILE_APPENDER_NAME)
            if (fileAppender) {
                String file = fileAppender.getFile()
                if (file) {
                    logFile = new File(file)
                }
            }
        }
        logFile
    }

    static void setLevel (String level) {
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

    static void setTraceLevel () {
        Logger.getRootLogger().setLevel(Level.TRACE)
    }

    static void setDebugLevel () {
        Logger.getRootLogger().setLevel(Level.DEBUG)
    }

    static void setInfoLevel () {
        Logger.getRootLogger().setLevel(Level.INFO)
    }

    static void setWarnLevel () {
        Logger.getRootLogger().setLevel(Level.WARN)
    }

    static void setErrorLevel () {
        Logger.getRootLogger().setLevel(Level.ERROR)
    }

    static void setFatalLevel () {
        Logger.getRootLogger().setLevel(Level.FATAL)
    }

    static Level getLevel () {
        Logger.getRootLogger().getLevel()
    }

    static void removeEmailAppender () {
        if (emailAppenderExists()) {
            Logger.getRootLogger().removeAppender(EMAIL_APPENDER_NAME)
        }
    }

    static void removeConsoleAppender () {
        if (consoleAppenderExists()) {
            Logger.getRootLogger().removeAppender(CONSOLE_APPENDER_NAME)
        }
    }

    static void setQuiet () {
        removeConsoleAppender()
    }

    static void removeFileAppender () {
        if (fileAppenderExists()) {
            Logger.getRootLogger().removeAppender(FILE_APPENDER_NAME)
        }
    }

    static Logger getLogger (Object obj) {
        Logger.getLogger(obj.getClass())
    }

    static Logger getLogger (Class clazz) {
        Logger.getLogger(clazz)
    }

    static Logger getLogger(String name) {
        Logger.getLogger(name)
    }

}