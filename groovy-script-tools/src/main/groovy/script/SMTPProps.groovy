package script

/**
 * Singleton class used to cache SMTP properties found.<br/>
 * Used by SMTPMailer.
 * @author en032339
 * @see SMTPMailer
 */
@Singleton
@groovy.util.logging.Log4j
class SMTPProps {

    static final String SMTP_PROPERTY_FILE_NAME = '.smtp'
    // default smtp config properties and values
    static final String MAIL_SMTP_HOST = 'mail.smtp.host'
    static final String MAIL_SMTP_PORT = 'mail.smtp.port'
    static final String DEFAULT_MAIL_SMTP_HOST = 'mailhost.mwh.com'
    static final String DEFAULT_MAIL_SMTP_PORT = '25'

    Properties props
    
    static void setHostPort (String host = 'localhost', String port = '25') {
        SMTPProps.instance.props = [(MAIL_SMTP_HOST): 'localhost', (MAIL_SMTP_PORT): '25'] 
    }
    /**
     * Get cached SMTP properties.
     * @return Properties object of SMTP properties
     */
    Properties getSmtpProps () {
        return props == null ? load() : props
    }

    /**
     * Load SMTP properties in a Properties object.
     * @param props
     */
    void loadWith (Properties props) {
        if (props == null) {
            throw new Exception("properties argument is null")
        }
        this.props = props
        validate()
    }

    /**
     * Load SMTP properties from a properties File.
     * @param file
     */
    void loadWith (File file) {
        if (file == null || !file.isFile()) {
            throw new Exception("properties file [$file] does not exist")
        }
        props = new Properties()
        props.load(new FileInputStream(file))
    }

    /**
     * Load SMTP properties.
     * <p>
     * First, look in the application properties.<br/>
     * Second, look for a file called ".smtp" on your script directory.<br/>
     * Third, look for the same file in the users home directory.<br/>
     * <p>
     * At a minimum, the following two properties are needed.
     * <ul><li>mail.smtp.host</li><li>mail.smtp.port</li></ul>
     * @return 
     */
    Properties load () {
        props = new Properties()
        log.debug "looking for smtp properties in script properties"

        Props.instance.each { String k, String v ->
            if (k.startsWith('mail.smtp.')) {
                props.setProperty(k, v)
                log.debug "$k = $v"
            }
        }
        
        if (props.size() == 0) {
            if (Env.scriptFile != null && Env.scriptFile.isFile()) {
                log.debug "looking for smtp properties in [${Env.scriptFile.parentFile}]"
                File file = new File(Env.scriptFile.parentFile, SMTP_PROPERTY_FILE_NAME)
                if (file.isFile()) {
                    log.debug "loading smtp properties from [$file]"
                    loadWith(file)
                }
            }
        }

        if (props.size() == 0) {
            log.debug "looking for smtp properties in user home"
            Properties sysprops = System.getProperties()
            if (sysprops.containsKey('user.home')) {
                String home = sysprops['user.home']
                if (home.length() > 0) {
                    File homeDir = new File(home)
                    log.debug "user home directory [$homeDir]"
                    if (homeDir.isDirectory()) {
                        File file = new File(homeDir, SMTP_PROPERTY_FILE_NAME)
                        if (file.isFile()) {
                            log.debug "loading smtp properties from [$file]"
                            loadWith(file)
                        }
                    }
                }
            }
        }

        if (props.size() == 0) {
            props.setProperty(MAIL_SMTP_HOST, DEFAULT_MAIL_SMTP_HOST)
            props.setProperty(MAIL_SMTP_PORT, DEFAULT_MAIL_SMTP_PORT)
            log.warn "Using default SMTP configuration."
            log.warn "Please configure a SMTP connection. Using the default is not recommended."
            log.warn "Default smtp properties: host ($DEFAULT_MAIL_SMTP_HOST) port ($DEFAULT_MAIL_SMTP_PORT)"
            log.info "@WARNING: Using default smtp config."
        }

        validate()

        return props
    }

    /*
     * Validate all smtp properties.
     */
    void validate () {
        if (!props.containsKey(MAIL_SMTP_HOST)) {
            throw new Exception("smtp properties key [${MAIL_SMTP_HOST}] is missing")
        }
        String value = props.getProperty(MAIL_SMTP_HOST)
        if (value.length() == 0) {
            throw new Exception("smtp properties key [${MAIL_SMTP_HOST}] value [$value] is not valid")
        }
        if (!props.containsKey(MAIL_SMTP_PORT)) {
            throw new Exception("smtp properties key [${MAIL_SMTP_PORT}] is missing")
        }
        value = props.getProperty(MAIL_SMTP_PORT)
        if (value.length() > 0 && value =~ /\D/) {
            throw new Exception("smtp properties key [${MAIL_SMTP_PORT}] value [$value] is not valid")
        }
    }
    
    String getHost () {
        if (props.containsKey(MAIL_SMTP_HOST)) {
            return props[MAIL_SMTP_HOST]
        }
        return null
    }

    Properties getProps () {
        return props
    }
    
}