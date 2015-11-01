package script

//import groovy.time.TimeCategory
import org.apache.log4j.Logger

abstract class ScriptWrapper extends Script {
    
    File scriptFile 
    String title
    String env
    String host
    Props props
    Logger log
    Cli cli
    Closure stopWatch
    Smgr smgr

    @Override
    def run () {
        smgr = new Smgr()
        try {
            smgr.begin(this)           
            runScript()
            smgr.end()           
        }
        catch (e) {
            println e
            smgr.fatal(e)           
        }
        println 'done'
    }
    
    abstract def runScript()
    
}