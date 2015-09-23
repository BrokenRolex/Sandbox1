This directory contains 'delivery' files.
A delivery file contains the rules of how to deliver a customer's data.
Each file is named for a customer number with a .txt extension.
(eg. 165748.txt)
The contents of these files are key=value pairs.
Comments can be placed in the files as lines beginning with a #.
Blank likes are ignored.

There are two different kinds of keys, configuration keys, and delivery keys.

Configuration keys are:

    customer_filename_template
    gzip

Delivery keys are:

    ftp
    email
    copy
    call

gzip:

    Compress the customer's datafile using gzip.
    This is a boolean value.

    Examples of truth (yes, compress the file):

        gzip=1
        gzip=yes
        gzip=yep
        gzip=affirmative
        gzip=verdad
        
        (eg. any defined value that does not begin with 'n' and is not '0')

    Examples of false (no, do not compress the file):

        gzip=
        gzip=0
        gzip=no
        gzip=negative
        gzip=nope

        (eg. '0' (zero), empty string, or any string beginning with 'n')

customer_filename_template:

    This key stores the rules on how to build the customer's filename.

    When data gets extracted the filename can potentially be anything.
    The only sure this is that it is 'somename.xml'. When this gets mapped
    a file named 'somename.dat'. However, customers usually want their
    data delivered with a filename that they like.
    This template allows for a custom name.

    This  key takes on the following form:

        customer_filename_template=template data

    The 'template data' is a '/' delimited string.
    Each 'piece' or token is concatenated together to build the final file name.
    There are 4 'special' tokens:
        $custid = This is the customer number of the extract
        $begdt  = begin date of the extract
        $enddt  = end date of the extract
        $rundt  = run date of the extract
    Anything else is a literal.
    The date pieces are by default formated as a YYYYMMDD string.
    However, they can be 'transformed' to any date string using strftime
    formatting codes. For example, say you wanted a ddMonYY begin date
    as a piece of the file name.  To accomplish this you would use
    '$begdt%d%b%y' as a token.  See the strftime man pages for a complete
    list of these formatting codes.

    Here are some examples:
        for customer number 123456, begdt 20080101,
        enddt 20080231, rundt 20070207 :

        $custid/_/$begdt/_/$enddt/.csv   ->  123456_20080101_20080231.csv
        data/-/$rundt%d-%b-%Y/.txt       ->  data-07-Feb-2008.txt
        mw_/$enddt%m%d%Y/.dat            ->  mw_02312008.dat
    
email:

    This is a delivery key that allows the specification of email addresses
    for delivering the data.

    The format is:

        email=addresses

    The addresses string is a csv list of email address to send an email to.
    Each address must be prefixed by a To:, Cc: or Bcc:.

    Examples:

        email=To:someone@somewhere.com,To:bill@whitehouse.gov
        email=To:someone@somewhere.com,Cc:bill@whitehouse.gov
        email=To:someone@somewhere.com,To:god@pearlygates.net,Bcc:bill@whitehouse.gov
          
ftp:

    This is a delivery key that allows the for the delivery of a file to an ftp site.

    The format is:

        ftp=ftp url

        The ftp url must be something like this...

        ftp://username:password@hostname/path

copy:

    This is a delivery key that allows for the delivery of a file by copying it to a directory.

    copy=directory


    Example:

        copy=/home/transfer

call:

   This is a delivery key that allows for the delivery of a file by calling an executeable.

   call=/path/executeable

   This will cause the following to execute...

       /path/executeable /path/outputdatafile customer_filename

   Example:

       Lets say we have a script called  dothewatusi.ksh in /usr/local/scripts
       and we are delivering 123456_20080101_20080131.dat in /home/oramwh/data/invexp/
       using a template of hdsdata/enddt/.txt
       This would cause the following to be executed...

       /usr/local/scripts/dothewatusi.ksh /home/oramwh/data/invexp/123456_20080101_20080131.dat hdsdata20080131.txt