package ci.map.macro

import ci.map.Macro

@groovy.util.logging.Log4j
class Dateformat extends Macro {
    private static final Map Mon = ['01':'Jan','02':'Feb','03':'Mar','04':'Apr','05':'May','06':'Jun','07':'Jul','08':'Aug','09':'Sep','10':'Oct','11':'Nov','12':'Dec']
    private static final Map MON = ['01':'JAN','02':'FEB','03':'MAR','04':'APR','05':'MAY','06':'JUN','07':'JUL','08':'AUG','09':'SEP','10':'OCT','11':'NOV','12':'DEC']
    private static final Map mon = ['01':'jan','02':'feb','03':'mar','04':'apr','05':'may','06':'jun','07':'jul','08':'aug','09':'sep','10':'oct','11':'nov','12':'dec']
    @Override
    String execute (Map data, String dateString) {
        String formattedDateString = dateString
        String c = dateString.take(2) // century
        dateString = dateString.drop(2)
        String y = dateString.take(2) // year
        dateString = dateString.drop(2)
        String m = dateString.take(2) // month
        dateString = dateString.drop(2)
        String d = dateString.take(2) // day
        String dateFormat = value
        switch (dateFormat) {
            case ~/\A[Yy]{4}[Mm]{2}[Dd]{2}\z/:
                formattedDateString =  dateString
                break
            case ~/\A[Mm]{2}[Dd]{2}[Yy]{4}\z/:
                formattedDateString =  "$m$d$c$y"
                break
            case ~/\A[Mm]{2}[Dd]{2}[Yy]{2}\z/:
                formattedDateString =  "$m$d$y"
                break
            case ~/\A[Dd]{2}[Mm]{2}[Yy]{4}\z/:
                formattedDateString =  "$d$m$c$y"
                break
            case ~/\A[Dd]{2}[Mm]{2}[Yy]{2}\z/:
                formattedDateString =  "$d$m$y"
                break
            case ~/\A[Yy]{4}[Dd]{2}[Mm]{2}\z/:
                formattedDateString =  "$c$y$d$m"
                break
            case ~/\A[Yy]{2}[Dd]{2}[Mm]{2}\z/:
                formattedDateString =  "$y$d$m"
                break
            case ~/\A[Yy]{2}[Mm]{2}[Dd]{2}\z/:
                formattedDateString =  "$y$m$d"
                break
            case ~/\A[Dd]{2}-MON-[Yy]{4}\z/:
            case ~/\A[Dd]{2}-MMM-[Yy]{4}\z/:
                formattedDateString =  "${d}-${MON[m]}-$c$y"
                break
            case ~/\A[Dd]{2}-MON-[Yy]{2}\z/:
            case ~/\A[Dd]{2}-MMM-[Yy]{2}\z/:
                formattedDateString =  "$d-${MON[m]}-$y"
                break
            case ~/\A[Dd]{2}\/MON\/[Yy]{4}\z}/:
            case ~/\A[Dd]{2}\/MMM\/[Yy]{4}\z}/:
                formattedDateString =  "$d/${MON[m]}/$c$y"
                break
            case ~/\A[Dd]{2}\/MON\/[Yy]{2}\z}/:
            case ~/\A[Dd]{2}\/MMM\/[Yy]{2}\z}/:
                formattedDateString =  "$d/${MON[m]}/$y"
                break
            case ~/\A[Dd]{2}-Mon-[Yy]{4}\z/:
            case ~/\A[Dd]{2}-Mmm-[Yy]{4}\z/:
                formattedDateString =  "$d-${Mon[m]}-$c$y"
                break
            case ~/\A[Dd]{2}-Mon-[Yy]{2}\z/:
            case ~/\A[Dd]{2}-Mmm-[Yy]{2}\z/:
                formattedDateString =  "$d-${Mon[m]}-$y"
                break
            case ~/\A[Dd]{2}\/Mon\/[Yy]{4}\z/:
            case ~/\A[Dd]{2}\/Mmm\/[Yy]{4}\z/:
                formattedDateString =  "$d/$Mon[$m]/$c$y"
                break
            case ~/\A[Dd]{2}\/Mon\/[Yy]{2}\z/:
            case ~/\A[Dd]{2}\/Mmm\/[Yy]{2}\z/:
                formattedDateString =  "$d/${Mon[m]}/$y"
                break
            case ~/\A[Dd]{2}-mon-[Yy]{4}\z/:
            case ~/\A[Dd]{2}-mmm-[Yy]{4}\z/:
                formattedDateString =  "$d-${mon[m]}-$c$y"
                break
            case ~/\A[Dd]{2}-mon-[Yy]{2}\z/:
            case ~/\A[Dd]{2}-mmm-[Yy]{2}\z/:
                formattedDateString =  "$d-$Mon[$m]-$y"
                break
            case ~/\A[Dd]{2}\/mon\/[Yy]{4}\z/:
            case ~/\A[Dd]{2}\/mmm\/[Yy]{4}\z/:
                formattedDateString =  "$d/$mon[$m]/$c$y"
                break
            case ~/\A[Dd]{2}\/mon\/[Yy]{2}\z/:
            case ~/\A[Dd]{2}\/mmm\/[Yy]{2}\z/:
                formattedDateString =  "$d/${mon[m]}/$y"
                break
            case ~/\A[Dd]{2}-[Mm]{2}-[Yy]{4}\z/:
                formattedDateString =  "$d-$m-$c$y"
                break
            case ~/\A[Dd]{2}-[Mm]{2}-[Yy]{2}\z/:
                formattedDateString =  "$d-$m-$y"
                break
            case ~/\A[Dd]{2}\/[Mm]{2}\/[Yy]{4}\z/:
                formattedDateString =  "$d/$m/$c$y"
                break
            case ~/\A[Dd]{2}\/[Mm]{2}\/[Yy]{2}\z/:
                formattedDateString =  "$d/$m/$y"
                break
            case ~/\A[Mm]{2}-[Dd]{2}-[Yy]{4}\z/:
                formattedDateString =  "$m-$d-$c$y"
                break
            case ~/\A[Mm]{2}-[Dd]{2}-[Yy]{2}\z/:
                formattedDateString =  "$m-$d-$y"
                break
            case ~/\A[Mm]{2}\/[Dd]{2}\/[Yy]{4}\z/:
                formattedDateString =  "$m/$d/$c$y"
                break
            case ~/\A[Mm]{2}\/[Dd]{2}\/[Yy]{2}\z/:
                formattedDateString =  "$m/$d/$y"
                break
            case ~/\A[Yy]{4}-[Mm]{2}-[Dd]{2}\z/:
                formattedDateString =  "$c$y-$m-$d"
                break
            case ~/\A[Yy]{2}-[Mm]{2}-[Dd]{2}\z/:
                formattedDateString =  "$y-$m-$d"
                break
            case ~/\A[Yy]{4}\/[Mm]{2}\/[Dd]{2}\z/:
                formattedDateString =  "$c$y/$m/$d"
                break
            case ~/\A[Yy]{2}\/[Mm]{2}\/[Dd]{2}\z/:
                formattedDateString =  "$y/$m/$d"
                break
            default:
                log.warn "date format [$value] format is unknown, cannot convert [$dateString]"
        }
        formattedDateString
    }
}
