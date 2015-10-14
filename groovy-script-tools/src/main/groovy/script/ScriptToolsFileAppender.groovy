package script

import script.*
import org.apache.log4j.Level
import org.apache.log4j.RollingFileAppender
import org.apache.log4j.spi.LoggingEvent

/**
 * Write any message that extends Throwable as a full stack trace.
 * @author en032339
 */
@groovy.transform.CompileStatic
class ScriptToolsFileAppender extends RollingFileAppender {

    List emailList

    void setEmailList (List list) {
        emailList = list
    }

    @Override
    void subAppend (LoggingEvent event) {
        if (event == null) {
            return
        }

        Object eventMessage = event.getMessage()
        String message = ''
        if (eventMessage == null) {
        }
        else if (eventMessage instanceof Throwable) {
            // always email full stack trace if Throwable
            StringWriter sw = new StringWriter(2048) // too big?
            ((Throwable) eventMessage).printStackTrace(new PrintWriter(sw))
            message = sw.toString()
        }
        else {
            message = eventMessage.toString() ?: ''
        }
        Boolean atSign = false
        if (message.startsWith('@')) {
            atSign = true
            message = message.drop(1) // remove @ sign
        }

        if (emailList) {
            Level level = event.getLevel()
            Integer levelInt = level.toInt()
            if ((levelInt > Level.DEBUG_INT && atSign) || (levelInt > Level.ERROR_INT)) {
                // get first 30 chars of line 1
                String message1 = message.take(30)
                Integer line = 0
                message1.eachLine {
                    if ((++line) == 1) {
                        message1 = it
                    }
                }
                try {
                    SMTPMailer m = new SMTPMailer()
                    m.setFrom('do_not_reply@hdsupply.com')
                    m.setTo(emailList)
                    m.setSubject(level.toString(), message1)
                    m.setMessage(message)
                    m.send()
                }
                catch (e) {
                    // ignore this ?
                }
            }
        }

        // build a new logging event that contains the stack trace as the message
        LoggingEvent newEvent = new LoggingEvent(
                event.getFQNOfLoggerClass(),
                event.getLogger(),
                event.getTimeStamp(),
                event.getLevel(),
                message,
                event.getThreadName(),
                event.getThrowableInformation(),
                event.getNDC(),
                event.getLocationInformation(),
                event.getProperties())

        super.subAppend(newEvent)
    }
}