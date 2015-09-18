package hds.groovy.log

import org.apache.log4j.RollingFileAppender
import org.apache.log4j.spi.LoggingEvent

/**
 * Write any message that extends Throwable as a full stack trace.
 * @author en032339
 */
@groovy.transform.CompileStatic
public class RollingFileAutoStackTraceAppender extends RollingFileAppender {

    @Override
    void subAppend (LoggingEvent event) {
        if (event.getMessage() instanceof Throwable) {
            
            // get the stack trace
            java.io.StringWriter sw = new java.io.StringWriter()
            ((Throwable) event.getMessage()).printStackTrace(new java.io.PrintWriter(sw))
            
            // build a new logging event that contains the stack trace as the message
            LoggingEvent newEvent = new LoggingEvent(
                event.getFQNOfLoggerClass(),
                event.getLogger(),
                event.getTimeStamp(),
                event.getLevel(),
                sw.toString(), // stack trace
                event.getThreadName(),
                event.getThrowableInformation(),
                event.getNDC(),
                event.getLocationInformation(),
                event.getProperties())
            
            super.subAppend(newEvent)
            
            return
        }
        
        super.subAppend(event)
    }
    
}