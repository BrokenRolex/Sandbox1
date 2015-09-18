package hds.groovy


/**
 * Get the current host name.<br/>
 * Uses java.net.InetAddress.getLocalHost().getCanonicalHostName()<br/>
 * which typically returns the full host name<br/>
 * (eg. sfmwsp04lpl.hsi.hughessupply.com).
 * 
 * @author en032339
 */
@groovy.transform.CompileStatic
class Host {

    private static String canonicalName
    private static String name

    private Host () {
        throw new Exception("Host is a static class")
    }

    /**
     * Get the full host name.
     * @return cannonical host name
     */
    static String getCanonicalName () {
        if (canonicalName == null) {
            getName()
        }
        return canonicalName
    }

    /**
     * Get the short host name. No domain, canonical name up until the first period.
     * @return host name
     */
    static String getName () {
        if (name == null) {
            canonicalName = InetAddress.getLocalHost().getCanonicalHostName()
            int dot = canonicalName.indexOf('.')
            name = (dot == -1) ? canonicalName : canonicalName.substring(0, dot)
        }
        return name
    }
    
    /**
     * Determine if a host is reachable.
     * @param host name of host to check
     * @param timeout milliseconds to wait 
     * @return boolean value indicating if the host is reachable
     */
    static Boolean isReachable (String host, int timeout) {
        Boolean result = false
        try {
            result = java.net.InetAddress.getByName(host).isReachable(timeout)
        }
        catch (e) {
            // This exception is not handled because it will cause
            // this method to return false (the host is unreachable)
            // which is what we want.
        }
        return result
    }
    
    /**
     * Determine if a host is reachable. Wait a maximum of 5 seconds.
     * @param host name of host to check
     * @return boolean value indicating if the host is reachable
     */
    static Boolean isReachable (String host) {
        return isReachable(host, 5000) // 5 seconds
    }
    
    static Boolean isLocal (String host) {
        if (host == 'localhost') {
            return true
        }
        return host == Host.getName() ? true : false
    }
    
}