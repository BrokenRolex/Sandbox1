package script

import java.text.SimpleDateFormat

/**
 * Simple static class to return date strings
 * using {@link SimpleDateFormat}.
 */
@groovy.transform.CompileStatic
class DateString {

    static final String DATE_TIME_MS = 'yyyyMMddHHmmssSSS'
    static final String DATE_TIME = 'yyyyMMddHHmmss'
    static final String DATE = 'yyyyMMdd'
    static final String TIME_MS = 'HHmmssSSS'
    static final String TIME = 'HHmmss'
    static final String ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    static final String ISOZ = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    DateString() {
        throw new Exception("DateString is a static class")
    }

    /**
     * Get a date string
     * @return date in iso format
     */
    static String iso () { iso(new Date()) }
    static String iso (Date date) { date?.format(ISO) }
    
    /**
     * Get a date string
     * @return date in isoz format
     */
    static String isoz () { isoz(new Date()) }
    static String isoz (Date date) { date?.format(ISOZ, TimeZone.getTimeZone("UTC")) }
    
    /**
     * Get a date string
     * @return date in yyyyMMddHHmmssSSS format
     */
    static String ymdtms () { ymdtms(new Date()) }
    static String ymdtms (Date date) { date?.format(DATE_TIME_MS) }

    /**
     * Get a date string
     * @return date in yyyyMMddHHmmss format
     */
    static String ymdt () { ymdt(new Date()) }
    static String ymdt (Date date) { date?.format(DATE_TIME) }

    /**
     * Get a date string
     * @return date in yyyyMMdd format
     */
    static String ymd () { ymd(new Date()) }
    static String ymd (Date date) { date?.format(DATE) }

    /**
     * Get time string
     * @return time in HHmmss format
     */
    static String t () { t(new Date()) }
    static String t (Date date) { date?.format(TIME) }
    
    /**
     * Get time string
     * @return time in HHmmssSSS format
     */
    static String tms () { tms(new Date()) }
    static String tms (Date date) { date.format(TIME_MS) }

}