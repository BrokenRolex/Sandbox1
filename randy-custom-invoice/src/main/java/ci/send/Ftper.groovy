package ci.send

import groovy.util.logging.Log4j
import org.apache.commons.net.ftp.*

@Log4j
class Ftper {
    
    def send (File file, String s) {
        def errors = 0
        try {
            def url = new URL(s)
            def (user, pass) = url.userInfo.split(/:/, 2)
            def path = url.path.drop(1)
            def ftp = new FTPClient()
            ftp.connect(url.host, url.port == -1 ? url.defaultPort : url.port)
            ftp.enterLocalPassiveMode()
            //ftp.setFileType(FTP.BINARY_FILE_TYPE)
            //ftp.setFileType(FTP.ASCII_FILE_TYPE)
            if (ftp.login(user, pass)) {
                def ok2send = true
                if (path) {
                    if (!ftp.changeWorkingDirectory(path)) {
                        log.error "ftp cwd failed: path [$path] data file [$file] not delivered"
                        errors++
                        ok2send = false
                    }
                }
                if (ok2send) {
                    file.withInputStream { is -> ftp.storeFile(cfn, is) }
                    ftp.disconnect()
                }
            }
            else {
                log.error "ftp login failed: data file [$file] not delivered"
                errors++
            }
        }
        catch (e) {
            log.error "ftp connect failed: $e.message"
            errors++
        }
        errors
    }

}
