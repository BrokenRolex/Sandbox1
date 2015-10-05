package script

@groovy.util.logging.Log4j
class MockMailer extends MailerBase {
    @Override
    public void send() {
        log.info this.toString()
    }
}
