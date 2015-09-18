package hds.groovy

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

    private DateString() {
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
    static String datetimems () { datetimems(new Date()) }
    static String datetimems (Date date) { date?.format(DATE_TIME_MS) }
    static String date_yyyyMMddHHmmssSSS () { datetimems() }
    static String yyyymmddhhmmsssss () { datetimems() }

    /**
     * Get a date string
     * @return date in yyyyMMddHHmmss format
     */
    static String datetime () { datetime(new Date()) }
    static String datetime (Date date) { date?.format(DATE_TIME) }
    static String date_yyyyMMddHHmmss() { datetime() }
    static String yyyymmddhhmmss() { datetime() }

    /**
     * Get a date string
     * @return date in yyyyMMdd format
     */
    static String date () { date(new Date()) }
    static String date (Date date) { date?.format(DATE) }
    static String date_yyyyMMdd() { date() }
    static String yyyymmdd() { date() }

    /**
     * Get time string
     * @return time in HHmmss format
     */
    static String time () { time(new Date()) }
    static String time (Date date) { date?.format(TIME) }
    static String time_HHmmss() { time() }
    static String hhmmss() { time() }
    
    /**
     * Get time string
     * @return time in HHmmssSSS format
     */
    static String timems () { timems(new Date()) }
    static String timems (Date date) { date.format(TIME_MS) }
    static String time_HHmmssSSS() { timems() }
    static String hhmmsssss() { timems() }

}