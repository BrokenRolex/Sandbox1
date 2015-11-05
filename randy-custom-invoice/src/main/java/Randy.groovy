
import ci.io.*

//@Grab('brokenrolex:groovy-script-tools:4.0')
import org.xml.sax.Attributes
@groovy.transform.BaseScript(script.SCRIPT)
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

import ci.map.*

def mf = new MacroFactory()

def a = new TestAttributes()
a.setValue('name', 'substring')
a.setValue('value', '1,2')
a.setValue('source', 'data')
def macro = (Macro) mf.createObject(a)
println macro

println macro.execute([banana:'toast'], 'erick')

System.exit 1
def db = new DataBuilder()
db.eachFile { file ->
    db.build(file)
}

def ds = new DataSender()
ds.eachFile { file ->
    ds.send(file)
}


class TestAttributes implements Attributes {

    List names = []
    List values = []

    void setValue (String name, String value) {
        if (name) {
            names << name
            values << (value ?: '')
        }
    }

    @Override
    int getLength() {
        names.size()
    }

    @Override
    String getURI(int index) {
        null
    }

    @Override
    String getLocalName(int index) {
        (index >= 0 && index < getLength()) ? names[index] : null
    }

    @Override
    String getQName(int index) {
        (index >= 0 && index < getLength()) ? names[index] : null
    }

    @Override
    String getType(int index) {
        null
    }

    @Override
    String getValue(int index) {
        (index >= 0 && index < getLength()) ? values[index] : null
    }

    @Override
    int getIndex(String uri, String localName) {
        names.findIndexOf { it == localName}
    }

    @Override
    int getIndex(String qName) {
        names.findIndexOf { it == qName}
    }

    @Override
    String getType(String uri, String localName) {
        null
    }

    @Override
    String getType(String qName) {
        null
    }

    @Override
    String getValue(String uri, String localName) {
        getValue(getIndex(localName))
    }

    @Override
    String getValue(String qName) {
        getValue(getIndex(qName))
    }

}