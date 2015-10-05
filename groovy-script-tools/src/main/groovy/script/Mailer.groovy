package script

/**
 * A simple static closure to send email through a mailer.<p/>
 * The default delivery method is through SMTPMailer.<p/>
 * To change this set mailer to an instance of a class that extends MailerBase.<p/>
 * Example...<p/>
 * <pre>
 * Mailer.mailer = new MyMailer() // MyMailer extends MailerBase
 * 
 * Mailer.send {
 *     to = 'someone@somewhere.com'
 *     subject = 'test'
 *     message = 'hello'
 * }
 * </pre>
 * Mailer properties include:
 * <ul>
 * <li>subject : String</li>
 * <li>message : String</li>
 * <li>from : String </li>
 * <li>to : String (csv), List, Array, Collection of String</li>
 * <li>cc : String (csv), List, Array, Collection of String</li>
 * <li>bcc : String (csv), List, Array, Collection of String</li>
 * <li>attach : File or List, Array, Collection of File</li>
 * </ul>
 * <p/>
 * See MailerBase for more details.<p/>
 * @author en032339
 *
 */
class Mailer {
    static MailerBase mailer = new SMTPMailer()
    static void send (Closure block) {
        mailer.with block
        mailer.send()
    }
}
