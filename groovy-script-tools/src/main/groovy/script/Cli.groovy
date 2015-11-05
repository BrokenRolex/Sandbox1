package script

import org.apache.commons.cli.Option

@groovy.util.logging.Log4j
class Cli {

    CliBuilder builder
    OptionAccessor opt
    Map implied = [:].withDefault{[] as Set}
    Script script

    Cli (Script s) {
        script = s
        Props props = Props.instance
        builder = new CliBuilder(
                usage: "${Env.scriptName} [options] args" as String,
                header: (props.getProperty('cli.header') ?: 'options:'),
                footer: (props.getProperty('cli.footer') ?: ''),
                )
    }

    void parseArgs () {
        opt = builder.parse(script.args)
    }

    void addImpliedOptions () {
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
        Map map = [opt: '', longOpt: '', argName: '', desc: '', implied: '']
        s.split(/\|/).each {
            def (key, val) = it.split(/=/, 2)
            val = val.trim()
            if (val && map.containsKey(key)) {
                map[key] = val
            }
        }
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
        Map builderOption = [:]
        if (llen > 0) {
            builderOption.longOpt = map.longOpt
        }
        if (slen > 0) {
            builderOption.opt = map.opt
        }
        if (map.argName) {
            builderOption.args = 1
            builderOption.argName = map.argName
        }
        builder."${map.opt ?: '_'}"(builderOption, map.desc)
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

    // cli.opt.name=
    void createCustomOptions () {
        Properties p = Props.instance
        def pattern = ~/cli\.opt\.\p{Alnum}+/
        p.each { String key, String val ->
            if (key ==~ pattern) {
                createOption(val)
            }
        }
    }

    void usage () {
        builder.usage()
    }

    List info () {
        List lines = []
        if (script.args) {
            lines << "script.args ${script.args}" as String
        }
        if (opt.arguments()) {
            lines << "opt.arguments ${opt.arguments()}" as String
        }

        List optVals = []
        builder.options.getOptions().each { option ->
            String nm = "${option.longOpt ?: option.opt}"
            optVals << ("$nm = ${opt.getProperty(nm)}" as String)
        }

        if (optVals) {
            lines << "cli options $optVals" as String
        }
        lines
    }

}
