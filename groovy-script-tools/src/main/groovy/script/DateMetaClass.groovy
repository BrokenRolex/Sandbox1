package script

class DateMetaClass {

    private DateMetaClass() {
        throw new Exception("DateMeta is a static class")
    }

    static void add () {
        Date.metaClass.ymd = { DateString.ymd() }
    }

}