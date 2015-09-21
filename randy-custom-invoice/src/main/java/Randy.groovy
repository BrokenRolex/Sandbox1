
/*
 @Grapes([
 @Grab('commons-digester:commons-digester:2.1'),
 @Grab('commons-beanutils:commons-beanutils:1.9.2'),
 @Grab('log4j:log4j:1.2.17'),
 @Grab('commons-/lang:commons-lang:2.6'),
 ])
 */
import groovy.io.FileType
import ci.io.DataReader
import ci.io.DataWriter
//@Grab('com.hdsupply:hds-groovy:4.0')
@groovy.transform.BaseScript(script.ScriptWrapper)
import script.*

props.print()

// ==========================================================================

props.getFileProp('in.dir').eachFileMatch(FileType.FILES, ~/.+\.xml/) { inFile ->
    DataReader dr = new DataReader(inFile)
    DataWriter dw = new DataWriter(dr)
    dw.write()
}

// ==========================================================================

/*
@Log4j
class AbsValue extends Macro {
    String execute (Map m, String sin) {
        data = m
        String sout = sin.to_BigDecimal().abs().toString()
        log.debug "in=[$sin], out=[$sout], " + this
        sout
    }
}

@Log4j
class Add extends Macro {
    String execute (Map data, String sin) {
        String sout = sin.to_BigDecimal().add(getSourceData(sin).to_BigDecimal()).toString()
        log.debug "in=[$sin], out=[$sout], " + this
        sout
    }
}

@Log4j
class Append extends Macro {
    String execute (Map data, String sin) {
        String sout = (sin ?: '') + (getSourceData(sin) ?: '')
        log.debug "in=[$sin], out=[$sout], " + this
        sout
    }
}

@Log4j
class Center extends Macro {
    String execute (Map data, String si) {
        String so = si
        if (!obj1) {
            obj1 = value.to_Integer()
        }
        if (obj1) {
            Integer width = (Integer) obj1
            so = data.center(width).take(width)
        }
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Default extends Macro {
    String execute (Map data, String si) {
        String so = data ? getSourceData(data) : data
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class DeleteWhiteSpace extends Macro {
    String execute (Map data, String si) {
        String so = si?.replaceAll(/\s/,'')
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Divide extends Macro {
    String execute (Map data, String si) {
        String so = BigDecimal.ZERO.toString()
        BigDecimal databd = data.to_BigDecimal()
        BigDecimal sourcebd = getSourceData(data).to_BigDecimal()
        if (databd != BigDecimal.ZERO && sourcebd != BigDecimal.ZERO) {
            so = databd.divide(sourcebd, 2, BigDecimal.ROUND_HALF_UP).toString()
        }
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

@Log4j
class Left extends Macro {
    String execute (Map data, String si) {
        String so = si
        if (si?.length()) {
            if (!obj1) {
                obj1 = value.to_Integer()
            }
            so = data.take((Integer) obj1)
        }
        log.debug "in=[$si], out=[$so], " + this
        so
    }
}

*/