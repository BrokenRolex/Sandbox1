package script

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

    static int run (cmd) {
        Process proc = cmd.execute()
        proc.consumeProcessOutput()
        proc.waitFor()
        proc.exitValue()
    }

    static int run (cmd, File output) {
        Process proc = cmd.execute()
        output.withOutputStream { os ->
            proc.consumeProcessOutput(output, output)
        }
        proc.waitFor()
        proc.exitValue()
    }

    static int run (cmd, Appendable output) {
        Process proc = cmd.execute()
        proc.consumeProcessOutput(output, output)
        proc.waitFor()
        proc.exitValue()
    }

    static int run (cmd, List output) {
        StringBuilder sb = new StringBuilder(2048)
        int exitValue = run(cmd, sb)
        sb.toString().eachLine { output << it }
        exitValue
    }

    static List execute (cmd) {
        List output = []
        run(cmd, output)
        output
    }

    static int scp (cmd) {
        run([SCP, cmd].flatten())
    }

    static int ssh (String host, cmd) {
        run([SSH, host, cmd].flatten())
    }

    static int ssh (String host, cmd, List output) {
        run([SSH, host, cmd].flatten(), output)
    }

}