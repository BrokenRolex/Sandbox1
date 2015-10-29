package script

import groovy.time.TimeCategory
import org.apache.log4j.Logger

abstract class ScriptWrapper extends Script {
    
    File scriptFile 
    Props props
    Logger log
    Cli cli // command line options and arguments
    Closure stopWatch
    ScriptManager scriptMgr
    LogManager logMgr

    @Override
    def run () {
        scriptMgr = new ScriptManager()
        try {
            scriptMgr.begin(this)           
            scriptFile = Env.scriptFile
            logMgr = scriptMgr.logMgr
            log = scriptMgr.log
            cli = scriptMgr.cli
            props = Props.instance
            stopWatch = { id, Closure c ->
                Date start = new Date()
                c.call()
                log.info "$id : ${TimeCategory.minus(new Date(), start)}"
            }
            runScript()
            scriptMgr.end()           
        }
        catch (e) {
            scriptMgr.fatal(e)           
        }
    }
    
    abstract def runScript()
    
}