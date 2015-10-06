import script.*

Mailer.mailer = new MockMailer()

Mailer.send {
    to(['ericksn@gmail.com'])
    bcc = 'banana@org.com,fred@flintstone.net'
    subject = 'hi'
    message = 'toast'
}

