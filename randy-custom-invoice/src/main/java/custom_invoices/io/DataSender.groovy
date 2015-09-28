package custom_invoices.io

import script.*
import groovy.io.FileType

import javax.mail.Session
import javax.mail.Transport
import javax.mail.Message
import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

import custom_invoices.send.Emailer;
import custom_invoices.send.Ftper;

import javax.activation.FileDataSource
import javax.activation.DataHandler

@groovy.util.logging.Log4j
class DataSender {
    File datFile
    File sumFile
    Map summary
    Map sendRules
    String cfn // customer file name
    Emailer emailer = new Emailer()
    Ftper ftper = new Ftper()

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
        def errors = 0
        sendRules.each { k, v ->
            switch (k) {
                case 'email' :
                    errors += emailer.send(f, cfn, summary, v)
                    break
                case 'ftp' :
                    errors += ftper.send(f, v)
                    break
                case 'call' :
                    errors += callIt(v)
                    break
                case 'copy' :
                    errors += copyIt(v)
            }
        }
        if (errors == 0) {
            log.info "archive"
        }
    }

    def callIt (s) {
        def errors = 0
        try {
            File script = new File(s)
            if (script.isFile()) {
                String cmd = "${Env.isWindows ? 'cmd /c ' : ''}$script $datFile $cfn"
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
        catch (e) {
            log.error e.message
            errors++
        }
        errors
    }

    def copyIt (s) {
        def errors = 0
        try {
            File dir = new File(s)
            if (dir.isFile()) {
                log.error "cannot copy [$datFile] as [$cfn] to [$dir] destination dir is a file"
                errors++
            }
            if (!dir.isDirectory()) {
                if (!dir.mkdirs()) {
                    log.error "cannot copy [$datFile] as [$cfn] to [$dir] destination dir does not exist"
                    errors++
                }
            }
            File toFile = new File(dir, cfn)
            datFile.withInputStream { toFile << it } // perform the copy
        }
        catch (e) {
            log.error e.message
            errors++
        }
        errors
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
        cfn = name.join('')
        log.info "customer file name [$cfn]"
    }

}
