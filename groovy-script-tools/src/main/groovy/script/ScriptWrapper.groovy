package script

import groovy.time.TimeCategory
import org.apache.log4j.Logger

abstract class ScriptWrapper extends Script {
    
    File scriptFile 
    Props props
    Logger log
    Cli cli // command line options and arguments
    Closure stopWatch
    ScriptManager sm

    @Override
    def run () {
        ScriptManager sm = new ScriptManager()
        try {
            //ScriptTools.begin(this)           
            sm.begin(this)           
            scriptFile = sm.scriptFile
            cli = sm.cli
            log = LogMgr.getLogger(this)
            props = Props.instance
            stopWatch = { id, Closure c ->
                Date start = new Date()
                c.call()
                log.info "$id : ${TimeCategory.minus(new Date(), start)}"
            }
            runUserScript()
            //ScriptTools.end()           
            sm.end()           
        }
        catch (e) {
            sm.fatal(e)           
        }
    }
    
    abstract def runUserScript()
    
}