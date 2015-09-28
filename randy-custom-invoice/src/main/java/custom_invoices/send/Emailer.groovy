package custom_invoices.send

import script.Props

import javax.mail.Session
import javax.mail.Transport
import javax.mail.Message
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import javax.activation.FileDataSource
import javax.activation.DataHandler

@groovy.util.logging.Log4j
class Emailer {

    Map recipientTypes = [ //
        TO: Message.RecipientType.TO,
        CC: Message.RecipientType.CC,
        BCC: Message.RecipientType.BCC]

    def send (File file, String custFileName, Map summary, String s) {
        def errors = 0
        try {
            Properties p = new Properties()
            p['mail.smtp.host'] = Props.instance.getProp('mail.smtp.host')
            p['mail.smtp.port'] = Props.instance.getProp('mail.smtp.port')
            Session session = Session.getInstance(p)
            MimeMessage mimeMessage = new MimeMessage(session)
            mimeMessage.setSubject(email_subject(summary))
            def fromAddr = new InternetAddress(Props.instance.getProp('email.from.default'))
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
