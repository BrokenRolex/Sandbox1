package script

class StringUtils {

    StringUtils () {
        throw new Exception("StringUtils is a static class")
    }

    /**
     * Evaluate a string as a boolean.
     * <p>
     * True values are...
     * <ul>
     * <li>Yes or Y -- case insensitive</li>
     * <li>True or T -- case insensitive</li>
     * <li>Any string that is all numbers but does not begin with 0</li>
     * </ul>
     * Anything else is false.
     * <p>
     * @param string to evaluate as on boolean
     * @return boolean value
     */
    static Boolean isTrue (String s) {
        if (s == null) {
            false
        }
        else {
            [
                ~/(?i)Y/,
                ~/(?i)T/,
                ~/(?i)YES/,
                ~/(?i)TRUE/,
                ~/[1-9]\d*/
            ].any{s ==~ it}
        }
    }

    static String shuffle (String string) {
        if (string == null || string.length() < 2) {
            string
        }
        else {
            def r = new Random()
            def sb1 = new StringBuilder(string)
            def sb2 = new StringBuilder(string.length())
            while (sb1.length() > 1) {
                def ni = r.nextInt(sb1.length())
                sb2 << sb1[ni]
                sb1.deleteCharAt(ni)
            }
            sb2 << sb1
            sb2.toString()
        }
    }

    static String random (String s, Integer len) {
        if (s == null || len == null || s.length() == 0 || len == 0) {
            ''
        }
        else {
            def slen = s.length()
            def sb = new StringBuilder(len)
            def r = new Random()
            (0..<len).each { sb << s[r.nextInt(slen)] }
            sb.toString()
        }
    }

    static String random (int len) {
        random('0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ', len)
    }
}
