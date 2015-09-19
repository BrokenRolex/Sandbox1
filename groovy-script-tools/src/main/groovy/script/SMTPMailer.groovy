package script

import javax.mail.Session
import javax.mail.Transport
import javax.mail.Message
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import javax.activation.FileDataSource
import javax.activation.DataHandler

/**
 * Send an email using SMTP.
 * <p>
 * Example:
 * <pre>
 * Mailer mailer = new SMTPMailer()
 * mailer.subject = 'hello'
 * mailer.to = ['someone@somewhere.com']
 * mailer.message = 'mail body text'
 * mailer.send()
 * </pre>
 * <p>
 *  
 * @author en032339
 */
@groovy.transform.Canonical
@groovy.util.logging.Log4j
class SMTPMailer extends MailerBase {

    private Session session

    /**
     * Create a new SMTPMailer instance.<br/>
     * To do this some smtp configuration needs to be loaded.<br/>
     * At a minimum the following two properties are needed...
     * <p>
     * <ul>
     * <li style="list-style: decimal;">mail.smtp.host</li>
     * <li style="list-style: decimal;">mail.smtp.port</li>
     * </ul>
     * <p>
     * If authentication is required then the following properties will be needed...
     * <ul>
     * <li style="list-style: decimal;">mail.smtp.auth = true</li>
     * <li style="list-style: decimal;">mail.smtp.authenticator.username</li>
     * <li style="list-style: decimal;">mail.smtp.authenticator.password</li>
     * </ul>
     * <p>
     * These Properties are looked for in a number of places.
     *  The first file found with smtp properties is used.
     * <ul>
     * <li style="list-style: decimal;">Script Properties via a {@link Props} instance</li>
     * <li style="list-style: decimal;">Script Directory (file .smtp)</li>
     * <li style="list-style: decimal;">User Home Directory (file .smtp)</li>
     * </ul>
     * <p>
     * If smtp properties cannot be found in any of these locations
     * a warning will be logged and the host (mailhost.mwh.com)
     * and port (25) will be used with no authentication.<br/>
     * <b>Please do not use this default behavior in a production environment.</b>
     */
    SMTPMailer() {
        super()
        java.util.Properties props = SMTPProps.instance.getSmtpProps()
        String auth = props.getProperty('mail.smtp.auth')
        String username = props.getProperty('mail.smtp.user')
        String password = props.getProperty('mail.smtp.user.password')
        if (auth == 'true' && username && password) {
            SMTPAuthenticator authenticator = new SMTPAuthenticator(username, password)
            session = Session.getInstance(props, authenticator)
        }
        else {
            session = Session.getInstance(props)
        }
    }

    @Override
    void send () {
        try {
            def recipientList = [
                [id: 'to',  type: Message.RecipientType.TO,  list: to],
                [id: 'cc',  type: Message.RecipientType.CC,  list: cc],
                [id: 'bcc', type: Message.RecipientType.BCC, list: bcc],
            ]
            log.debug "subject [$subject] from [$from] " + recipientList.collect{ "${it.id} [${it.list.join(',')}]" }.join(' ') + " message [$message]"
            MimeMessage mimeMessage = new MimeMessage(session)
            mimeMessage.setSubject(subject)
            def fromAddr = new InternetAddress(from)
            mimeMessage.setFrom(fromAddr)
            mimeMessage.setReplyTo(fromAddr)
            int validEmailAddresses = 0
            recipientList.each { Map map ->
                if (map.list.size()) {
                    map.list.each { String email ->
                        mimeMessage.addRecipient(map.type, new InternetAddress(email))
                        validEmailAddresses++
                    }
                }
            }
            if (validEmailAddresses == 0) {
                log.warn "no email recipients set, cannot send email"
                return
            }
            MimeBodyPart mimeBodyPart = new MimeBodyPart()
            mimeBodyPart.setText(message)
            MimeMultipart multipart = new MimeMultipart()
            multipart.addBodyPart(mimeBodyPart)
            attach.each { List attachThis ->
                File file = attachThis[0]
                String name = attachThis[1]
                log.debug "added file attachment [$file] as [$name]"
                if (!file.exists()) {
                    log.warn "cannot attach file [$file] because it does not exist"
                }
                MimeBodyPart attachment = new MimeBodyPart()
                attachment.setDataHandler(new DataHandler(new FileDataSource(file)))
                attachment.setFileName(name)
                multipart.addBodyPart(attachment)
            }
            mimeMessage.setContent(multipart)
            Transport.send(mimeMessage)
            // javax.mail.Transport t = session.getTransport('smtp')
            // t.send(mimeMessage)
            // t.close()
            log.debug 'send complete'
        }
        catch (e) {
            log.error e
        }
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        private String username
        private String password
        private javax.mail.PasswordAuthentication authentication
        public SMTPAuthenticator (String username, String password) {
            this.username = username
            this.password = password
            authentication = new PasswordAuthentication(username, password)
        }
        protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
            return authentication
        }
    }

}