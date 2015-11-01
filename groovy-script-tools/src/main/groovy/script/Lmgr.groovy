package script

import org.apache.log4j.ConsoleAppender
import org.apache.log4j.RollingFileAppender
import org.apache.log4j.PatternLayout
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.slf4j.bridge.SLF4JBridgeHandler as BridgeHandler

class Lmgr {

    static final String CONSOLE_APPENDER_NAME = 'appender.console'
    static final String FILE_APPENDER_NAME = 'appender.file'
    static final String LOGFILE_EXT = '.log'
    static final String CONSOLE_PATTERN_LAYOUT = '%d{HH:mm:ss,SSS} %-5p %c{1} - %m%n'
    static final String FILE_PATTERN_LAYOUT = '%d %-5p %c{1} - %m%n'
    static final String MAX_FILE_SIZE = '5MB'
    static final Integer MAX_BACKUP_INDEX = 2
    static final Map LEVELS = ['TRACE': Level.TRACE,'DEBUG': Level.DEBUG,'INFO':Level.INFO,'WARN':Level.WARN,'ERROR':Level.ERROR,'FATAL':Level.FATAL]

    void removeAllAppenders () {
        Logger.getRootLogger().removeAllAppenders()
    }

    /**
     * Create a JUL to Slf4j bridge (Slf4J uses Log4j)
     */
    void createJulBridge () {
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

    /*
    Boolean appenderExists (String name) {
        for (def appender in Logger.getRootLogger().getAllAppenders()) {
            if (appender.name == name) {
                return  true
            }
        }
        false
    }
    */
    
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