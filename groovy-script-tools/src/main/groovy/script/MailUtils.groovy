package script

/**
 * Mail utilities.
 * 
 * @author en032339
 */
class MailUtils {
    
    private static final String QM = '?'

    private MailUtils () {
        throw new Exception("MailUtils is a static class")
    }
    
    /**
     * Build a default subject line.
     * <p/>
     * The subject is formatted like...
     * <ul>
     * "message [title] [env] [host:script]"
     * </ul>
     * Where ...
     * <ul>
     * <li><b>message</b> = input message text to be placed in subject (see param)
     *  If not provided (null) then a '?' is used.
     * <li><b>title</b> = property program.title. If this does not exists
     *  then the script file name is used by Env.scriptFile.name. If Env.scriptFile has
     *  not been initialized then a '?' is used.</li>
     * <li><b>env</b> = property <i>env</i>. If this does not exist then a '?' is used.
     *  This value is always returned upper case.</li>
     * <li><b>host</b> = current host name from {@link Host#getName()}</li>
     * <li><b>script</b> = script file full path from {@link Env#findScriptFile(Script)} or '?'
     * if Env.scriptFile  has not been initialized </li>
     * </ul>
     * @param msg to be placed in subject line
     * @return subject line formated like...<br>
     * <b>
     * "message [title] [env] [host:script]"
     * </b>
     * 
     */
    static String buildSubject (String msg = null) {
        Props props = Props.instance
        msg = msg == null ? QM : msg
        final String env = (props.getProperty('env', QM)).toUpperCase()
        String title = props.getProperty('program.title', QM)
        String program = QM
        if (Env.scriptFile != null) {
            program = Env.scriptFile.path
        }
        return "${msg} [$title] [$env] [${Host.name}:$program]"
    }
    
}
