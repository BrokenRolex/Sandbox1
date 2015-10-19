
import ci.io.*

//@Grab('brokenrolex:groovy-script-tools:4.0')
@groovy.transform.BaseScript(script.ScriptWrapper)
import script.*

/*
@Grapes([
  @Grab('hdsupply.com:custom-invoices:1.0'),
  @Grab('commons-digester:commons-digester:2.1'),
  @Grab('commons-beanutils:commons-beanutils:1.9.2'),
  @Grab('commons-/lang:commons-lang:2.6'),
  @Grab('commons-net:commons-net:3.3'),
])
*/
import custom_invoices.io.*

String s = 'a,,'
def (a, b) = s.split(/,/,2)
println "a=$a"
println "b=$b"

System.exit 1
def db = new DataBuilder()
db.eachFile { file ->
    db.build(file)
}

def ds = new DataSender()
ds.eachFile { file ->
    ds.send(file)
}
