package script

import groovy.xml.*

/**
 * Static class to get current environment settings.
 */
class Env {

    Env () {
        throw new Exception("Env is a static class")
    }

    // vars to cache results
    static File homeDir // from system property user.home
    static File userDir // from system property user.dir
    static File tmpDir // from system property java.io.tmpdir
    static File scriptFile // derived from Script object, see findScriptFile(Script)
    static Boolean isWindows // from system property os.name
    static Boolean isLinux // from system property os.name
    static Boolean isOSX // from system property os.name
    static String pid // jvm process id
    static String name // environment name
    
    static String getName () {
        if (name) {
            name   
        }
        else {
            name = Props.instance.getProperty('env')
        }
    }

    static String getHostname () {
        Host.getName()
    }

    /**
     * Find the File of the script<br/>
     * Useful for finding resources relative to the script file.
     * <p/>
     * Example:
     * <pre>
     * File scriptFile = Env.findScriptFile(this)
     * File scriptDir = scriptFile.parentFile
     * </pre>
     * @param script object (this)
     * @return File Object for the script
     */
    static File findScriptFile(Script script) {
        try {
            String path = script.getClass().protectionDomain.codeSource.location.path
            File file = new File(path)
            if (!file.isFile()) {
                throw new Exception("script file [$file] does not exist")
            }
            scriptFile = file
        }
        catch (e) {
            throw new Exception("cannot find the script file : " + e.message)
        }
        scriptFile
    }

    /**
     * Return the script name.<br/>
     * The .groovy extension, if it exists, will be removed.
     * @return script name
     */
    static String getScriptName () {
        String nm = scriptFile?.name
        nm ? (nm - '.groovy') : '?'
    }

    /**
     * Get the Java version 
     * @return version string
     */
    static String getJavaVersion () {
        System.properties['java.version']
    }

    /**
     * Get the Groovy version
     * @return version string
     */
    static String getGroovyVersion () {
        GroovySystem.getVersion()
    }

    /**
     * Get the users home directory from the System property 'user.home'
     * @return current user's home directory as a File Object
     */
    static File getHomeDir () {
        if (homeDir) {
            homeDir // return cached value
        }
        else {
            String propval = System.getProperty('user.home')
            if (propval) {
                homeDir = new File(propval)
            }
            else {
                null
            }
        }
    }

    /**
     * Get the users current directory from the System property 'user.dir'
     * @return the current directory as a File Object
     */
    static File getUserDir () {
        if (userDir) {
            userDir // return cached value
        }
        else {
            String propval = System.getProperty('user.dir')
            if (propval) {
                userDir = new File(propval)
            }
            else {
                null
            }
        }
    }

    /**
     * Get the temporary directory from the System property 'java.io.tmpdir'
     * @return the system temporary directory as a File Object
     */
    static File getTmpDir () {
        if (tmpDir != null) {
            tmpDir // return cached value
        }
        else {
            String propval = System.getProperty('java.io.tmpdir')
            if (propval) {
                tmpDir = new File(propval)
            }
            else {
                null
            }
        }
    }

    /**
     * Determine if we are on a Windows computer by evaluating the os.name system property.
     * @return true if os.name (converted to lower case) contains 'windows'
     */
    static Boolean getIsWindows () {
        if (isWindows == null) {
            isWindows = false
            final String propval = System.getProperty('os.name')
            if (propval != null && propval.length() > 0) {
                if (propval.toLowerCase().contains('windows')) {
                    isWindows = true
                    isLinux = false
                    isOSX = false
                }
            }
        }
        isWindows
    }

    /**
     * Determine if we are on a Linux computer by evaluating the os.name system property.
     * @return true if os.name (converted to lower case) contains 'linux'
     */
    static Boolean getIsLinux () {
        if (isLinux == null) {
            isLinux = false
            final String propval = System.getProperty('os.name')
            if (propval != null && propval.length() > 0) {
                if (propval.toLowerCase().contains('linux')) {
                    isWindows = false
                    isLinux = true
                    isOSX = false
                }
            }
        }
        isLinux
    }

    /**
     * Determine if we are on an OS X computer by evaluating the os.name system property.
     * @return true if os.name (converted to lower case) contains 'os x'
     */
    static Boolean getIsOSX () {
        if (isOSX == null) {
            isOSX = false
            final String propval = System.getProperty('os.name')
            if (propval != null && propval.length() > 0) {
                if (propval.toLowerCase().contains('os x')) {
                    isWindows = false
                    isLinux = false
                    isOSX = true
                }
            }
        }
        isOSX
    }

    /**
     * Currently there is no support in Java to get the jvm process id.
     * However, there is a 'hack' that seems to work.
     * <p/>
     * From ManagementFactory javadoc:
     * <p/>
     * Returns the name representing the running Java virtual machine. 
     * The returned name string can be any arbitrary string and a Java
     * virtual machine implementation can choose to embed platform-specific
     * useful information in the returned name string. Each running
     * virtual machine could have a different name.
     * <p/>
     * For Windows 7 and Linux the getName method returns a string of the process
     * id and the hostname in a formatted string like this...
     * <p/>
     * <ul>pid@host</ul>
     * This is easily parsed. <p/>
     *  @return process id of the jvm
     *  @see java.lang.management.ManagementFactory
     */
    static String getPid () {
        if (pid != null) {
            return pid
        }
        String pidHost = java.lang.management.ManagementFactory.getRuntimeMXBean().getName()
        int atsign = pidHost.indexOf('@')
        pid = (atsign > 1) ? pidHost.substring(0, atsign) : pidHost
    }

}