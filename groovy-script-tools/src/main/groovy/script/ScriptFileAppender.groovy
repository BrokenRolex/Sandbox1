package script

import org.apache.log4j.Level
import org.apache.log4j.RollingFileAppender
import org.apache.log4j.spi.LoggingEvent

/**
 * Write any message that extends Throwable as a full stack trace.
 * 
 * If an email address is set, send message as an email.
 * 1. Always email Error or Fatal.
 * 2. Never email Trace or Debug.
 * 3. Info and Warn can be emailed, but only if the first character is an @.
 * 4. In all cases, if the first character is an @, it is removed.
 */
@groovy.transform.CompileStatic
class ScriptFileAppender extends RollingFileAppender {

    String emailto

    void setEmailTo (String emailto) {
        this.emailto = emailto
    }

    @Override
    void subAppend (LoggingEvent event) {

        Object eventMessage = event.getMessage()
        String message
        if (eventMessage instanceof Throwable) {
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

        if (emailto) {
            Level level = event.getLevel()
            Integer li = level.toInt()
            if ((li > Level.DEBUG_INT && atSign) || (li > Level.WARN_INT)) {
                try {
                    SMTPMailer m = new SMTPMailer()
                    m.setTo(emailto)
                    String message1 = message.take(30).readLines()[0] ?: ''
                    m.setSubject(level.toString(), message1)
                    m.setMessage(message)
                    m.send()
                }
                catch (e) {
                    // ignore this ?
                    //System.err.println e.message
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