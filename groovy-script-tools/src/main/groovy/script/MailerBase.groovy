package script

/**
 * Base class for sending email.
 * 
 * @author en032339
 */
@groovy.util.logging.Log4j
abstract class MailerBase {

    String default_from = 'do_not_reply@hdsupply.com'
    String from = Props.instance.getProperty('Mailer.default_from', default_from)
    String subject = ''
    String message = ''
    def to  = []
    def cc = []
    def bcc = []
    def attach = []
    Boolean smartSubject = true // assemble subject

    /**
     * Reset the internal state
     */
    void reset() {
        from = Props.instance.getProperty('Mailer.default_from', default_from)
        subject = ''
        message = ''
        to = []
        cc = []
        bcc = []
        attach = []
    }

    /**
     * Set the from email address
     * @param addr from email address
     */
    void setFrom (String addr) {
        if (addr) {
            from = addr
        }
    }

    void from (String addr) {
        setFrom(addr)
    }

    /**
     * Set a list of email address to the To list.
     * Clears the any email addresses in the list first.
     * @param list of email addresses to add to the To list
     */
    void setTo (arg) {
        if (arg == null) {
            to = []
        }
        else if ([Collection, Object[]].any { it.isAssignableFrom(arg.getClass()) }) {
            to = arg as List
        }
        else {
            to = (arg.toString()).split(/,/) as List
        }
    }

    void to (arg) {
        setTo(arg)
    }

    /**
     * Set a list of email addresses to the Cc list.
     * Clears the Cc list first.
     * @param list of email addresses to add to the Cc list
     */
    void setCc (arg) {
        if (arg == null) {
            cc = []
        }
        else if ([Collection, Object[]].any { it.isAssignableFrom(arg.getClass()) }) {
            cc = arg as List
        }
        else {
            cc = (arg.toString()).split(/,/) as List
        }
    }

    void cc (arg) {
        setCc(arg)
    }

    /**
     * Set a list of email addresses to the Bcc list.
     * Clears the Bcc list first.
     * @param list of email addresses to add to the Bcc list
     */
    void setBcc (arg) {
        if (arg == null) {
            bcc = []
        }
        else if ([Collection, Object[]].any { it.isAssignableFrom(arg.getClass()) }) {
            bcc = arg as List
        }
        else {
            bcc = (arg.toString()).split(/,/) as List
        }
    }

    void bcc (arg) {
        setBcc(arg)
    }

    /**
     * Set the email subject
     * @param subject of the email
     */
    void setSubject (String s) {
        if (s) {
            subject = s
            if (smartSubject) {
                String env = Env.name ?: '?'
                String host = Env.hostname ?: '?'
                String program = Env.scriptFile?.path ?: '?'
                String title = Props.instance.getProperty('program.title','?')
                subject = "$s [$title] [$env] [$host:$program]"
            }
        }
    }

    void subject (String s) {
        setSubject(s)
    }

    /**
     * Set the email message (body)
     * @param message
     */
    void setMessage (String m) {
        if (m) {
            message = m
        }
    }

    void message (String m) {
        setMessage(m)
    }

    void setBody (String m) {
        setMessage(m)
    }

    void body (String m) {
        setMessage(m)
    }

    /**
     * Add a file attachment.
     * <p>
     * @param file to attach
     */
    void setAttach (File file, String name = null) {
        if (file?.isFile()) {
            attach << [file: file, name: (name ?: file.name)]
        }
    }

    void attach (File file, String name = null) {
        attach(file, name)
    }

    @Override
    String toString() {
        [
            Subject: subject,
            From: from,
            To: to,
            Cc: cc,
            Bcc: bcc,
            Message: message,
            Attach: attach,
        ].toString()
    }

    /**
     * Send the email. Must be implemented.
     */
    abstract void send()
}