package script.ftp

import org.apache.commons.net.ftp.*

@groovy.transform.Canonical
class Ftpclient {
    
    private FTPClient ftp
    
    String host
    String user
    String password
    String path
    
    void url (String urlstring) {
        def ftpurl = new URL(urlstring)
        def userInfo = ftpurl.userInfo.split(/:/, 2)
        host = ftpurl.host
        user = userInfo[0]
        password = userInfo[1]
        path = ftpurl.path
    }
    
    void connect () {
        ftp = new FTPClient()
        try {
            ftp.connect(host)
        }
        catch (e) {
            throw new Exception("connect failed: $this: $e")
        }
        ftp.enterLocalPassiveMode()
        if (!ftp.login(user, password)) {
            throw new Exception("login failed; $this")
        }
        if (path) {
           cwd(path)
        }
    }
    
    void binary () {
        ftp.setFileType(FTP.BINARY_FILE_TYPE)
    }
    
    void ascii () {
        ftp.setFileType(FTP.ASCII_FILE_TYPE)
    }
    
    void cwd (String dir) {
        if (!ftp.changeWorkingDirectory(dir)) {
            throw new Exception("cwd failed to [$dir] $this")
        }
    }
    
    void getFile (String name, File f = new File('.', name)) {
        f.withOutputStream { os -> ftp.retrieveFile(name, os) }
    }
    
    void putFile (File f, String name = f.name) {
        f.withInputStream { is -> ftp.storeFile(name, is) }
    }
    
    void deleteFile (String name) {
        ftp.deleteFile(name)
    }
    
    List list () {
        List files = []
        ftp.listFiles().each {
            if (it.isFile()) {
                 files << it.name
            }
        }
        return files
    }
    
    void disconnect () {
        ftp?.disconnect()
    }

}