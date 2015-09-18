package hds.groovy

import groovy.time.TimeCategory
import org.apache.log4j.Logger

abstract class ScriptWrapper extends Script {
    
    File scriptFile 
    Props props
    Logger log
    Cli cli // command line options and arguments
    Closure stopWatch

    @Override
    def run () {
        try {
            ScriptTools.begin(this)           
            scriptFile = Env.scriptFile
            cli = ScriptTools.cli
            log = LogMgr.getLogger(this)
            props = Props.instance
            stopWatch = { id, Closure c ->
                Date start = new Date()
                c.call()
                log.info "$id : ${TimeCategory.minus(new Date(), start)}"
            }
            runUserScript()
            ScriptTools.end()           
        }
        catch (e) {
            ScriptTools.fatal(e)           
        }
    }
    
    abstract def runUserScript()
    
}
