package hds.groovy.log

import hds.groovy.*
import java.awt.Event
import org.apache.log4j.Level
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent
import java.io.StringWriter
import java.io.PrintWriter

/**
 * Log4j Email Appender.
 * <p>
 * This appender will send e-mail depending on what kind of message is being logged.
 * <p>
 * If the level if the message is ERROR or FATAL then the message gets e-mailed.
 * <p>
 * If the level is INFO or WARN and the message begins with an at-sign (@) then it gets e-mailed.
 * <p>
 * DEBUG or TRACE messages never get e-mailed.
 * <p>
 * If the message is of type {@link java.lang.Throwable Throwable} then it is converted to a stack trace.
 * <p>
 * The subject line will be built from
 *  {@link hds.groovy.MailUtils#buildSubject(String) MailUtils.buildSubject}.
 * @author en032339
 */
public class EmailAppender extends AppenderSkeleton {

    private List emailList

    /**
     * Set the e-mail addresses.
     * @param list
     */
    public void setEmailList (List list) {
        emailList = list
    }

    @Override
    void activateOptions() {
        super.activateOptions()
    }

    @Override
    protected void append (LoggingEvent event) {
        boolean addEmailAppender = false
        try {
            if (LogMgr.emailAppenderExists()) {
                addEmailAppender = true
                LogMgr.removeEmailAppender()
            }
            if (emailList == null || emailList.size() == 0) {
                return // no email addresses configured, skip this appender
            }
            if (event == null) {
                return // no event ???
            }
            Level level = event.getLevel()
            if (level == null) {
                return // no level ???
            }
            int levelInt = level.toInt()

            // DEBUG or TRACE messages do _NOT_ get emailed
            if (levelInt < Level.INFO_INT) {
                return
            }

            String eventMessage = getEventMessage(event)
            Boolean atSign = false
            if (eventMessage != null && eventMessage.length() > 0 && eventMessage[0] == '@') {
                atSign = true
                eventMessage = eventMessage.replaceFirst(/\A@/, '') // remove atSign
            }
            String eventMessage1 = ''
            int line = 0
            eventMessage.eachLine {
                if ((++line) == 1) {
                    eventMessage1 = it
                }
            }

            // INFO and WARN messages that begin with an at-sign (@) get emailed
            // ERROR and FATAL messages always get emailed
            if (levelInt < Level.ERROR_INT && !atSign) {
                return
            }

            String subject = MailUtils.buildSubject(level.toString()) + ' ' + eventMessage1
            sendEmail(emailList, subject, eventMessage)
        }
        finally {
            if (addEmailAppender) {
                LogMgr.addEmailAppender()
            }
        }
    }

    private void sendEmail (List tolist, String subject, String message) {
        try {
            SMTPMailer smtpMailer = new SMTPMailer()
            smtpMailer.setTo(tolist)
            smtpMailer.setSubject(subject)
            smtpMailer.setMessage(message)
            smtpMailer.send()
        }
        catch (e) {
            if (ScriptTools.cli.opt.debug) {
                e.printStackTrace()
            }
        }
    }

    private String getEventMessage (LoggingEvent event) {
        if (event == null) {
            return ''
        }
        Object eventMessage = event.getMessage()
        if (eventMessage == null) {
            return ''
        }
        if (eventMessage instanceof Throwable) {
            // always email full stack trace if Throwable
            StringWriter sw = new StringWriter(2048) // too big?
            ((Throwable) eventMessage).printStackTrace(new PrintWriter(sw))
            return sw.toString()
        }
        String message = eventMessage.toString()
        if (message == null) {
            return ''
        }
        return message;
    }

    @Override public void close() { }

    @Override public boolean requiresLayout() { return false }

}
