
/*
 @Grapes([
 @Grab('commons-digester:commons-digester:2.1'),
 @Grab('commons-beanutils:commons-beanutils:1.9.2'),
 @Grab('commons-/lang:commons-lang:2.6'),
 @Grab('log4j:log4j:1.2.17'),
 ])
 */
import ci.io.*
//@Grab('com.hdsupply:hds-groovy:4.0')
@groovy.transform.BaseScript(script.ScriptWrapper)
import script.*
@Grab('commons-net:commons-net:3.3')
import org.apache.commons.net.ftp.*
import groovy.io.FileType

def db = new DataBuilder()
db.eachFile { file ->
    db.build(file)
}

def ds = new DataSender()
ds.eachFile { file ->
    ds.send(file)
}
