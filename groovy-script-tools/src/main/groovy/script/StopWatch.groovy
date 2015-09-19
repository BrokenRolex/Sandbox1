package script

import groovy.time.TimeCategory

/**
 * StopWatch - time blocks of code
 * <p/>
 * Example:
 * <pre>
 *  def sw = new StopWatch()
 *  sw.logger = LogMgr.getLogger(this)
 *  sw.watch('name') {
 *      // your code here
 *  }
 * </pre>
 */
class StopWatch {

    def elapsed = [:]
    def logger

    StopWatch () {
    }

    StopWatch (def log) {
        logger = log
    }

    def log (String keyword, Closure closure) {
        def start = new Date()
        try {
            if (logger == null) {
                logger = LogMgr.getLogger(this)
            }
            logger.info("begin [$keyword]")
            return closure.call()
        }
        finally {
            String msg = "end [$keyword] duration [${TimeCategory.minus(new Date(), start)}]"
            elapsed[keyword] = msg
            logger.info(msg)
        }
    }

    def print (String keyword, Closure closure) {
        def start = new Date()
        try {
            println "begin [$keyword]"
            return closure.call()
        }
        finally {
            String msg = "end [$keyword] duration [${TimeCategory.minus(new Date(), start)}]"
            elapsed[keyword] = msg
            println msg
        }
    }
}
