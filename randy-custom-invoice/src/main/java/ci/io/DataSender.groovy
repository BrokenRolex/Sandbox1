package ci.io

import org.apache.commons.net.ftp.*
import groovy.io.FileType
import groovy.util.logging.Log4j
import script.*

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
class DataSender {
    File datFile
    File sumFile
    Map summary
    Map sendRules
    String custFileName
    Integer errors = 0
    
    void eachFile (Closure c) {
        Props.instance.getFileProp('out.dir').eachFileMatch(FileType.FILES, ~/.+\.dat/, c)
    }

    void send (File f) {
        datFile = f
        log.info "sending [$datFile]"
        sumFile = new File(datFile.parentFile, datFile.name - '.dat' + '.sum')
        if (!sumFile.isFile()) {
            log.warn "cannot deliver [$datFile] because [$sumFile] does not exist"
            return
        }
        summary = load_properties_file(sumFile)
        load_send_rules_file()
        build_customer_filename()
        sendRules.each { k, v ->
            switch (k) {
                case 'email' :
                    emailIt(v)
                    break
                case 'ftp' :
                    ftpIt(v)
                    break
                case 'call' :
                    callIt(v)
                    break
                case 'copy' :
                    copyIt(v)
            }
        }
    }

    void ftpIt (s) {
        def url = new URL(s)
        def (user, pass) = url.userInfo.split(/:/, 2)
        def path = url.path.drop(1)
        def ftp = new FTPClient()
        try {
            ftp.connect(url.host, url.port == -1 ? url.defaultPort : url.port)
            ftp.enterLocalPassiveMode()
            //ftp.setFileType(FTP.BINARY_FILE_TYPE)
            //ftp.setFileType(FTP.ASCII_FILE_TYPE)
            if (ftp.login(user, pass)) {
                def ok2send = true
                if (path) {
                    if (!ftp.changeWorkingDirectory(path)) {
                        log.error "ftp cwd failed: path [$path] data file [$datFile] not delivered"
                        ok2send = false
                        errors++
                    }
                }
                if (ok2send) {
                    datFile.withInputStream { is -> ftp.storeFile(custFileName, is) }
                    ftp.disconnect()
                }
            }
            else {
                log.error "ftp login failed: data file [$datFile] not delivered"
                errors++
            }
        }
        catch (e) {
            log.error "ftp connect failed: $e.message"
            errors++
        }
    }

    void emailIt (s) {
        try {
            Properties p = new Properties()
            //p['mail.smtp.host'] = 'mailhost.mwh.com'
            p['mail.smtp.host'] = 'localhost'
            p['mail.smtp.port'] = '25'
            Session session = Session.getInstance(p)
            MimeMessage mimeMessage = new MimeMessage(session)
            mimeMessage.setSubject(email_subject())
            def fromAddr = new InternetAddress('do_not_reply@hdsupply.com')
            mimeMessage.setFrom(fromAddr)
            mimeMessage.setReplyTo(fromAddr)
            s.split(/,/).each {
                def (type, addr) = it.trim().split(/:/, 2)
                def ia = new InternetAddress(addr.trim())
                type = type.trim().toUpperCase()
                if (type == 'TO') {
                    mimeMessage.addRecipient(Message.RecipientType.TO, ia)
                }
                else if (type == 'CC') {
                    mimeMessage.addRecipient(Message.RecipientType.CC, ia)
                }
                else if (type == 'BCC') {
                    mimeMessage.addRecipient(Message.RecipientType.BCC, ia)
                }
            }
            MimeBodyPart mimeBodyPart = new MimeBodyPart()
            mimeBodyPart.setText(email_body())
            MimeMultipart multipart = new MimeMultipart()
            multipart.addBodyPart(mimeBodyPart)
            MimeBodyPart attachment = new MimeBodyPart()
            attachment.setDataHandler(new DataHandler(new FileDataSource(datFile)))
            attachment.setFileName(custFileName)
            multipart.addBodyPart(attachment)
            mimeMessage.setContent(multipart)
            Transport.send(mimeMessage)
        }
        catch (e) {
            log.error "email failed [${e.message}] data file [$datFile] not delivered as [$custFileName] to [$s]"   
            errors++
        }
    }

    String email_subject () {
        'subject'
    }

    String email_body () {
        'body'
    }

    void callIt (s) {
        File script = new File(s)
        if (script.isFile()) {
            String cmd = "${Env.isWindows ? 'cmd /c ' : ''}$script $datFile $custFileName"
            log.info "running [$cmd]"
            def proc = cmd.execute()
            proc.waitFor()
            def exitValue = proc.exitValue()
            if (exitValue != 0) {
                log.error "call to [$cmd] exit value [$exitValue]"
                errors++
            }
        }
        else {
            log.error "call file [$s] does not exist, data file [$datFile] not delivered"
            errors++
        }
    }

    void copyIt (s) {
        File dir = new File(s)
        if (dir.isFile()) {
            log.error "cannot copy [$datFile] as [custFileName] to [$dir] destination dir is a file"
            errors++
        }
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                log.error "cannot copy [$datFile] as [custFileName] to [$dir] destination dir does not exist"
                errors++
            }
        }
        File toFile = new File(dir, custFileName)
        datFile.withInputStream { toFile << it } // perform the copy
    }

    Map load_properties_file (File f) {
        if (!f.isFile()) {
            throw new Exception("properties file [$f] is missing")
        }
        Properties p = new Properties()
        f.withInputStream { p.load(it) }
        p as Map
    }

    void load_send_rules_file () {
        if (!summary.containsKey('buyer.custid')) {
            throw new Exception("customer number [buyer.custid] missing in summary data")
        }
        String custid = summary['buyer.custid']
        File file = new File(Props.instance.getFileProp('send.dir'), "${custid}.txt")
        if (!file.isFile()) {
            throw new Exception("send rules file [$file] does not exist")
        }
        sendRules = load_properties_file(file)
    }

    void build_customer_filename () {
        String custid = summary.containsKey('buyer.custid') ? summary['buyer.custid'] : ''
        //
        Map dates = [:]
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat('YYYYMMdd')
        ['rundt', 'begdt', 'enddt'].each { dt ->
            String dtkey = "run.$dt"
            String dtstr = summary.containsKey(dtkey) ? summary[dtkey] : ''
            dates[dt] = sdf.parse(dtstr)
            if (dates[dt] == null) {
                log.error "invalid date [$dtstr] in summary field [$dtkey] data does not match pattern [${sdf.toPattern()}]"
            }
        }
        dates['nowdt'] = new Date()
        //
        String template = '$custid/_/$begdt/_/$enddt/.csv' // default
        if (sendRules.containsKey('customer_filename_template')) {
            template = sendRules['customer_filename_template']
        }
        List name = []
        template.split(/\//).each { str ->
            if (str.take(1) == '$') {
                String s = str.drop(1)
                if (s.take(5) in ['begdt', 'enddt', 'rundt', 'nowdt']) {
                    String date = s.take(5)
                    String fmt = s.drop(5) ?: 'YYYYMMdd'
                    // fmt should be valid for Java's SimpleDateFormat.
                    // Java does not support an exact implementation of Perl's strftime.
                    // Next 3 lines are a hack to support some strftime formats currently in use.
                    fmt = fmt.contains('%d') ? fmt.replaceAll('%d','dd') : fmt
                    fmt = fmt.contains('%H') ? fmt.replaceAll('%H','HH') : fmt
                    fmt = fmt.contains('%M') ? fmt.replaceAll('%M','mm') : fmt
                    try {
                        name << dates[date].format(fmt)
                    }
                    catch (e) {
                        log.error "token [$str] has an invalid date format [$fmt] in template [$template], token ignored"
                    }
                }
                else if (s.take(6) == 'custid') {
                    name << custid
                }
                else {
                    log.error "token [$str] is an unknown function in template [$template], token ignored"
                }
            }
            else {
                name << str
            }
        }
        custFileName = name.join('')
        log.info "customer file name [$custFileName]"
    }
    
}
