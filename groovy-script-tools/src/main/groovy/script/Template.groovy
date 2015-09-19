package script

class Template {
    
    /**
     * Create a new script and properties from a template.<br>
     * @param list list of script names to create.
     *  Will create a script called <i>run</i> if null or empty
     */
    static void write (List list = null) {
        if (list == null || list.size() == 0) {
            list = ['run']
        }
        String groovyExt = '.groovy'
        list.unique().each {
            String scriptName = it
            if (Env.isWindows && !scriptName.endsWith(groovyExt)) {
                scriptName += groovyExt
            }
            File scriptFile = new File('.', scriptName)
            if (scriptFile.exists()) {
                System.err.println "script file [${scriptFile}] already exists"
                return
            }
            File propsFile = new File('.', "${scriptName}.properties")
            if (propsFile.exists()) {
                System.err.println "properties file [${propsFile}] already exists"
                return
            }
            scriptFile.withWriter { it << getScriptText() }
            propsFile.withWriter { it << getPropertiesText() }
            if (Env.isLinux) {
                try {
                    def proc = ['/bin/chmod', '744', scriptFile].execute()
                    proc.waitFor()
                }
                catch (Exception e) {
                    System.err.println e.message
                }
            }
        }
    }
    
    
    static String getScriptText () {
        String today =  new Date().format('dd-MMM-yyyy')
        String shebang = ''
        if (Env.isLinux) {
            String groovyHome = System.getenv('GROOVY_HOME')
            if (groovyHome) {
                shebang = "#!/usr/bin/env ${groovyHome}/bin/groovy"
            }
            else {
                shebang = "#!/usr/bin/env ${Env.homeDir.path}/groovy/bin/groovy"
            }
        }

        String scriptText = $/${shebang}
// TODO A short description of what I do.
//
// Revision History
// date         who       comment
// ${today}  unknown    created -- TODO please edit the 'who' so we know who created me

@Grab('com.hdsupply:hds-groovy:${FrameWork.version}')
@groovy.transform.BaseScript(script.ScriptWrapper)
import script.*

// TODO your code here

/$

        scriptText
    }

    static String getPropertiesText () {
        String propertiesText = '''
# program title used for automated emails
program.title=ima lazy developer and could not be bothered to change this

ScriptTools.runlock=false
ScriptTools.announce=false
ScriptTools.debug=false

# environments
env.gfmwsa01ldp=dev1
env.gfmwsa02ldp=dev2
env.gfmwsp11lds=dev
env.gfmwsa01lqp=qa1
env.gfmwsa02lqp=qa2
env.gfmwsp11lqs=qa
env.gfmwsa01lsp=stg
env.gfmwsp14lqs=stg
env.sfmwsa01lpp=prod
env.sfmwsp11lps=prod
env=${env.${HOST}}

# email

email.support=hds-fmwebspheretechsup-u1@hdsupply.com
email.dev1=${email.support}
email.dev2=${email.support}
email.dev=${email.support}
email.qa1=${email.support}
email.qa2=${email.support}
email.qa=${email.support}
email.stg=${email.support}
email.prod=${email.support}
email=${email.${env}}

logger.email=${email}

# SAP PI transfer directory

xfer.dev=PLD
xfer.qa=PLQ
xfer.stg=PLS
xfer.prod=PLP
xfer=${xfer.${env}}
xfer.dir=/xfer/${xfer}/websphere
#validate.xfer.dir=dir

'''
        propertiesText
    }

}
