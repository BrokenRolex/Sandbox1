package script

/**
 * Send an email using the Linux Mutt utility. TODO get rid if this
 * <p>
 * Example:
 * <pre>
 * def mailer = new Mutt()
 * mailer.subject = 'hello'
 * mailer.to = ['someone@somewhere.com']
 * mailer.message = 'mail body text'
 * mailer.send()
 * </pre> 
 * 
 */
@groovy.transform.Canonical
@groovy.util.logging.Log4j
class Mutt extends MailerBase {
    
    @Override
    void send () {
        try {
            String mutt = '/usr/bin/mutt'

            List cmd =  [
                mutt,
                '-e',  """my_hdr Reply-to:${from}""",
                '-e',  """my_hdr From:${from}""",
                '-s',  """${subject}""",
            ]

            if (!new File(mutt).exists()) {
                log.error "mail utility [${mutt}] cannot be found, email not sent"
                return
            }

            cc.each { String it ->
                cmd << '-c'
                cmd << it
            }

            attach.each { String it ->
                cmd << '-a'
                cmd << it.file.path
            }

            to.each { String it ->
                cmd << it
            }
        
            log.debug cmd

            def proc = cmd.execute()

            proc.withWriter { Writer wtr ->
                wtr << message
                wtr << "\n"
            }

            proc.waitFor()

            def rc = proc.exitValue()
            if (rc != 0) {
                log.error "email failed rc=$rc [$cmd] [${proc.err.getText()}]"
            }
        }
        catch (e) {
            log.error e   
        }
        reset()
    }

}
