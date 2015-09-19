package script

/**
 * This class all mailers in this scripting frame work should extend.
 * <br>
 * A class that extends Mailer need only implement the send method. If you
 * have special setup code create a default constructor but don't forget to
 * call the super (this class) default constructor.<br/>
 * See classes {@link Mutt} and {@link SMTPMailer} for examples.
 * 
 * @author en032339
 */
abstract class MailerBase {

    static final String DEFAULT_FROM_ADDR = 'do_not_reply@hdsupply.com'
    static final String EMPTY_STRING = ''

    String from
    String subject
    String message
    List to
    List cc
    List bcc
    List attach
    //Map attach

    /**
     * Constructor
     * <p>
     * Initialize object by calling {@link #reset()}
     */
    MailerBase() {
        reset()
    }

    Map getMap () {
        Map map = [
            Email: [
                Subject: subject,
                From: from,
                To: to,
                Cc: cc,
                Bcc: bcc,
                Body: message,
                Attach: attach,
            ]
        ]
        map
    }

    /**
     * Reset the internal state
     * <p>
     * <ul>
     * <li>Set From: back to default</li>
     * <li>Clear Subject and Message</li>
     * <li>Clear all To:, Cc: and Bcc: addresses</li>
     * <li>Clear all attachments</li>
     * </ul>
     */
    void reset() {
        from = Props.instance.getProperty('Mailer.default_from', DEFAULT_FROM_ADDR)
        subject = EMPTY_STRING
        message = EMPTY_STRING
        to = []
        cc = []
        bcc = []
        attach = []
        //attach = [:]
    }

    /**
     * Set the from email address
     * @param addr from email address
     */
    void setFrom (String addr) {
        if (!(addr == null || addr.length() == 0)) {
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
    void setTo (List list) {
        if (list == null) list = []
        to = list
    }
    void to (List list) {
        setTo(list)
    }

    void addTo (List list) {
        list.each { to << it }
    }

    /**
     * Set a email To address.
     * Clears the any email addresses in the list first.
     * @param addr
     */
    void setTo (String addr) {
        addString2List(to, addr)
    }

    void to (String addr) {
        setTo(addr)
    }

    void addTo (String addr) {
        to << addr
    }

    private final void addString2List (List list, String string) {
        if (string == null || string.length() == 0) return
        // the input string might be a csv...
        string.split(',').each {
            String s = it.trim()
            if (s.length() > 0) {
                list << it.trim()
            }
        }
    }


    /**
     * Set a list of email addresses to the Cc list.
     * Clears the Cc list first.
     * @param list of email addresses to add to the Cc list
     */
    void setCc (List list) {
        if (list == null) list = []
        cc = list
    }

    void cc (List list) {
        setCc(list)
    }

    /**
     * Set a Cc email address.
     * Clears the Cc list first.
     * @param addr email address
     */
    void setCc (String addr) {
        addString2List(cc, addr)
    }

    void cc (String addr) {
        setCc(addr)
    }

    /**
     * Set a list of email addresses to the Bcc list.
     * Clears the Bcc list first.
     * @param list of email addresses to add to the Bcc list
     */
    void setBcc (List list) {
        if (list == null) list = []
        bcc = list
    }

    void bcc (List list) {
        setBcc(list)
    }

    /**
     * Set a BCC email address.
     * Clears the bcc list first.
     * @param addr email address
     */
    void setBcc (String addr) {
        addString2List(bcc, addr)
    }

    void bcc (String addr) {
        setBcc(addr)
    }

    /**
     * Set the email subject
     * @param subject of the email
     */
    void setSubject (String s) {
        subject = s
    }

    void subject (String s) {
        setSubject(s)
    }

    void setSubjectPlus (String s) {
        setSubject(MailUtils.buildSubject(s))
    }

    void subjectPlus (String s) {
        setSubject(MailUtils.buildSubject(s))
    }

    /**
     * Set the email message (body)
     * @param message
     */
    void setMessage (String m) {
        message = m
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
    void attach (File file, String name = null) {
        if (file != null && file.isFile()) {
            //attach[file] = name == null ? file.name : name
            attach << [
                file,
                name == null ? file.name : name
            ]
        }
    }

    void attach (String file, String name = null) {
        attach(new File(file), name)
    }

    void attachFile (File file, String name = null) {
        attach(file, name)
    }

    void attachFile (String file, String name = null) {
        attach(file, name)
    }

    void setAttach (File file, String name = null) {
        attach(file, name)
    }

    void setAttach (String file, String name = null) {
        attach(file, name)
    }

    /**
     * 
     * Clear all attachments.
     */
    void clearAttachments () {
        attach.clear()
    }

    ///**
    // * Send an email
    // * @param json JsonBuilder
    // */
    //void send (groovy.json.JsonBuilder json) {
    //    send(json.toString())
    //}

    ///**
    // * Send an email
    // * @param json string
    // */
    //void send (String json) {
    //    send(new groovy.json.JsonSlurper().parseText(json) as Map)
    //}

    ///**
    // * Send an email
    // * @param map produced by JsonSlurper.parseText
    // */
    //void send (Map map) {
    //    reset()
    //    setSubject(map.Email.Subject)
    //    setMessage(map.Email.Body)
    //    setFrom(map.Email.From)
    //    map.Email.To.each { addTo(it) }
    //    map.Email.Cc.each { addCc(it) }
    //    map.Email.Attach.each { String filepath, String name ->
    //        attachFile(new File(filepath), name)
    //    }
    //    send()
    //}

    /**
     * For debugging
     */
    void dump () {
        println getMap()
    }
    
    @Override
    public String toString() {
        return getMap().toString()
    }

    /**
     * Send the email.
     * <p>
     * Classes that extend this class must implement this method.
     */
    abstract void send()
}