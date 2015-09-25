package ci.send

import groovy.util.logging.Log4j

import javax.mail.Session
import javax.mail.Transport
import javax.mail.Message
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import javax.activation.FileDataSource
import javax.activation.DataHandler

@Log4j
class Emailer {

    Map recipientTypes = [ //
        TO: Message.RecipientType.TO,
        CC: Message.RecipientType.CC,
        BCC: Message.RecipientType.BCC]

    def send (File file, String custFileName, Map summary, String s) {
        def errors = 0
        try {
            Properties p = new Properties()
            //p['mail.smtp.host'] = 'mailhost.mwh.com'
            p['mail.smtp.host'] = 'localhost'
            p['mail.smtp.port'] = '25'
            Session session = Session.getInstance(p)
            MimeMessage mimeMessage = new MimeMessage(session)
            mimeMessage.setSubject(email_subject(summary))
            def fromAddr = new InternetAddress('do_not_reply@hdsupply.com')
            mimeMessage.setFrom(fromAddr)
            mimeMessage.setReplyTo(fromAddr)
            s.split(/,/).each {
                def (type, addr) = it.trim().split(/:/, 2)
                type = type.trim().toUpperCase()
                if (recipientTypes.containsKey(type)) {
                    mimeMessage.addRecipient(recipientTypes[type],
                            new InternetAddress(addr.trim()))
                }
            }
            MimeBodyPart mimeBodyPart = new MimeBodyPart()
            mimeBodyPart.setText(email_body(summary))
            MimeMultipart multipart = new MimeMultipart()
            multipart.addBodyPart(mimeBodyPart)
            MimeBodyPart attachment = new MimeBodyPart()
            attachment.setDataHandler(new DataHandler(new FileDataSource(file)))
            attachment.setFileName(custFileName)
            multipart.addBodyPart(attachment)
            mimeMessage.setContent(multipart)
            Transport.send(mimeMessage)
        }
        catch (e) {
            log.error "email failed [${e.message}] data file [$file] not delivered as [$custFileName] to [$s]"
            errors++
        }
        errors
    }

    String email_subject (Map s) {
        'subject'
    }

    String email_body (Map s) {
        'body'
    }

}
