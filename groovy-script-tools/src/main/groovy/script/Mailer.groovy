package script

import org.apache.log4j.Logger
import groovy.lang.Closure

class Mailer {

    private static MailerBase mailerImpl

    Mailer () {
        throw new Exception("Mailer is a static class")
    }

    static void send (Closure block) {
        def mailer = (mailerImpl == null) ? new SMTPMailer() : mailerImpl
        mailer.with block
        int recipients = 0
        recipients += mailer.to == null ? 0 : mailer.to.size()
        recipients += mailer.cc == null ? 0 : mailer.cc.size()
        recipients += mailer.bcc == null ? 0 : mailer.bcc.size()
        if (recipients > 0) {
            if (!mailer.subject) {
                mailer.subjectPlus('')
            }
            mailer.send()
        }
    }
}
