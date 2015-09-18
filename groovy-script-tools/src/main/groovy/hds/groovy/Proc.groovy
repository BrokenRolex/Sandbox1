package hds.groovy

import java.util.List;

/**
 * Shell out and perform commands
 */
class Proc {

    static final List SSHOPT = [
        '-q',
        '-oBatchMode=yes',
        '-oServerAliveInterval=60'
    ]
    static final List SSH = ['/usr/bin/ssh', SSHOPT].flatten()
    static final List SCP = ['/usr/bin/scp', SSHOPT].flatten()

    static int scp (String cmd) {
        run([SCP, cmd])
    }
    
    static int scp (List cmd) {
        run([SCP, cmd].flatten())
    }

    static int ssh (String host, String cmd) {
        run([SSH, host, cmd].flatten())
    }

    static int ssh (String host, List cmd) {
        run([SSH, host, cmd].flatten())
    }

    static int ssh (String host, List cmd, List output) {
        run([SSH, host, cmd].flatten(), output)
    }

    static int run (String cmd) {
        Process proc = cmd.execute()
        proc.consumeProcessOutput()
        proc.waitFor()
        proc.exitValue()
    }

    static int run (List cmd) {
        Process proc = cmd.execute()
        proc.consumeProcessOutput()
        proc.waitFor()
        proc.exitValue()
    }

    static int run (String cmd, File output) {
        Process proc = cmd.execute()
        output.withOutputStream { os ->
            proc.consumeProcessOutput(output, output)
        }
        proc.waitFor()
        proc.exitValue()
    }

    static int run (List cmd, File output) {
        Process proc = cmd.execute()
        output.withOutputStream { os ->
            proc.consumeProcessOutput(output, output)
        }
        proc.waitFor()
        proc.exitValue()
    }

    static int run (String cmd, Appendable output) {
        Process proc = cmd.execute()
        proc.consumeProcessOutput(output, output)
        proc.waitFor()
        proc.exitValue()
    }

    static int run (List cmd, Appendable output) {
        Process proc = cmd.execute()
        proc.consumeProcessOutput(output, output)
        proc.waitFor()
        proc.exitValue()
    }

    static int run (String cmd, List output) {
        StringBuilder sb = new StringBuilder(2048)
        int exitValue = run(cmd, sb)
        sb.toString().eachLine { output << it }
        exitValue
    }

    static int run (List cmd, List output) {
        StringBuilder sb = new StringBuilder(2048)
        int exitValue = run(cmd, sb)
        sb.toString().eachLine { output << it }
        exitValue
    }

    static List execute (List cmd) {
        List output = []
        run(cmd, output)
        output
    }

    static List execute (String cmd) {
        List output = []
        run(cmd, output)
        output
    }

}