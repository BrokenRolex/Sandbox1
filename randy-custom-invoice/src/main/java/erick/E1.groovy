package erick

import javax.mail.*
import javax.mail.internet.*

Properties p = new Properties()
//p['mail.smtp.host'] = 'mailhost.mwh.com'
p['mail.smtp.host'] = 'mailrelay.hsi.hughessupply.com'
p['mail.smtp.port'] = '25'
Session session = Session.getInstance(p)
MimeMessage mimeMessage = new MimeMessage(session)
mimeMessage.setSubject('test')
InternetAddress fromAddr = new InternetAddress('do_not_reply@hdsupply.com')
mimeMessage.setFrom(fromAddr)
mimeMessage.setReplyTo(fromAddr)
mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress('erick.nelson@hdsupply.com'))
MimeBodyPart mimeBodyPart = new MimeBodyPart()
mimeBodyPart.setText('test')
MimeMultipart multipart = new MimeMultipart()
multipart.addBodyPart(mimeBodyPart)
MimeBodyPart attachment = new MimeBodyPart()
mimeMessage.setContent(multipart)
Transport.send(mimeMessage)
