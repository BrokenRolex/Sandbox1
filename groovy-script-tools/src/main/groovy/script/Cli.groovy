package script

import groovy.json.JsonSlurper
import org.apache.commons.cli.Option

@groovy.util.logging.Log4j
class Cli {

    CliBuilder builder
    OptionAccessor opt
    Map implied = [:].withDefault{[] as Set}
    Script script 

    Cli (Script s) {
        builder = new CliBuilder(
                usage: "${Env.scriptName} [options] args" as String,
                header: (Props.instance.getProperty('cli.header') ?: 'options:'),
                footer: (Props.instance.getProperty('cli.footer') ?: ''),
                )
        script = s
    }

    void parse () {
        opt = builder.parse(script.args)
        addImpliedOptions(script)
    }

    void addImpliedOptions (Script script) {
        Set add = []
        for (Option o in builder.options.getOptions()) {
            String ok = (o.opt ?: o.longOpt) // option key
            def ov = opt.getProperty(ok) // option value
            if (ov) { // option is on the cmd line
                if (implied.containsKey(ok)) { // option has implied option(s)
                    implied[ok].each { iok ->
                        // implied option key
                        Option io = builder.options.getOption(iok)
                        if (io) { // implied option exists
                            if (io.args < 1) { // implied option has no args
                                if (!opt.getProperty(iok)) { // implied option not currently on cmd line
                                    add << iok // add this option to the command line
                                }
                            }
                        }
                    }
                }
            }
        }
        if (add.size() > 0) {
            def newargs = script.args as LinkedList
            add.each { newargs.addFirst((it.length() == 1 ? '-' : '--') + it) }
            opt = builder.parse(newargs)
        }
    }

    void createDefaultOptions () {
        createOption('opt=h|longOpt=help|desc=help')
        createOption('opt=v|longOpt=verbose|desc=verbose')
        createOption('opt=d|longOpt=debug|desc=debug|implied=v')
        createOption('opt=t|longOpt=test|desc=test|implied=v')
    }

    void createOption (String s) {
        Map map = pipedString2Map(s)
        
        // validate the option
        Integer slen = map.opt.length() // short opt length
        Integer llen = map.longOpt.length() // long opt length
        if (slen > 1 || llen == 1 || (slen == 0 && llen == 0)) {
            log.warn "invalid cli opt definition [$s]"
            return
        }
        if (slen) {
            if (builder.options.getOption(map.opt)) {
                log.warn "opt already used [$s]"
                return 
            }
        }
        if (llen) {
            if (builder.options.getOption(map.longOpt)) {
                log.warn "longOpt already used [$s]"
                return
            }
        }

        // add the option
        Map m = [:]
        if (llen > 0) {
            m.longOpt = map.longOpt
        }
        if (slen > 0) {
            m.opt = map.opt
        }
        if (map.args > 0) {
            m.args = 1
            m.argName = map.name ?: 'arg'
        }
        builder."${map.opt ?: '_'}"(m, map.desc ?: '')

        // collect implied options
        map.implied.split(/,/).each {
            String i = it.trim()
            if (i) {
                if (map.opt) {
                    implied[map.opt] << i
                }
                if (map.longOpt) {
                    implied[map.longOpt] << i
                }
            }
        }

    }

    void createCustomOptions () {
        Properties p = Props.instance
        def pattern = ~/(?:cli(?:Builder)?)\.opt(?:ion)?\.\p{Alnum}+/
        p.each { String key, String val ->
            if (key ==~ pattern) {
                createOption(val)
            }
        }
    }

    Map pipedString2Map (String s) {
        Map map = [opt: '', longOpt: '', name: '', arg: '', args: 0, desc: '', implied: '']
        s.split(/\|/).each {
            def (key, val) = it.split(/=/, 2)
            val = val.trim()
            if (val && map.containsKey(key)) {
                if (key in ['name','arg']) { // arg name
                    map.args = 1
                    map.name = val
                }
                else if (key == 'args') {
                    // skip this, will be set if here is an arg name
                }
                else {
                    map[key] = val
                }
            }
        }
        map
    }

    void usage () {
        builder.usage()
    }
    
    List optVals () {
        List optVals = []
        builder.options.getOptions().each { option ->
            String nm = "${option.longOpt ?: option.opt}"
            optVals << ("$nm = ${opt.getProperty(nm)}" as String)
        }
        optVals
    }
    
    List info () {
        List lines = []
        if (script.args) {
            lines << "script.args ${script.args}" as String
        }
        if (opt.arguments()) {
            lines << "opt.arguments ${opt.arguments()}" as String
        }
        List optVals = optVals()
        if (optVals) {
            lines << "cli options $optVals" as String
        }
        lines
    }
    
}
