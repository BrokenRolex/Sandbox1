package script

import org.apache.log4j.Logger
import groovy.lang.Closure

class Mailer {
    
    private static final Logger LOG = Logger.getLogger(Mailer.class)
    private static MailerBase mailerImpl
    
    Mailer () {
        throw new Exception("Mailer is a static class")
    }

    static void send (Closure block) {
        try {
            def mailer = (mailerImpl == null) ? new SMTPMailer() : mailerImpl
            mailer.with block
            int recipients = 0
            recipients += mailer.to == null ? 0 : mailer.to.size()
            recipients += mailer.cc == null ? 0 : mailer.cc.size()
            recipients += mailer.bcc == null ? 0 : mailer.bcc.size()
            if (recipients == 0) {
                LOG.debug "can't send email, no recipients : ${mailer.map}"
                return
            }
            if (mailer.subject == null || mailer.subject.length() == 0) {
                LOG.debug "no subject line provided, using default"
                mailer.subjectPlus('')
            }
            mailer.send()
        }
        catch (e) {
            LOG.error e.message
        }
    }

}
