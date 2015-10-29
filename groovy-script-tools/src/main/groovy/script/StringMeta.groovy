package script

class StringMeta {

    private StringMeta () {
        throw new Exception("StringMeta is a static class")
    }

    static void add () {

        String.metaClass.isTrue = { StringUtils.isTrue(delegate) }
        String.metaClass.random = { Integer len -> StringUtils.random(delegate, len) }
        String.metaClass.static.random = { Integer len -> StringUtils.random(len) }
        String.metaClass.shuffle = { StringUtils.shuffle(delegate) }

        String.metaClass.to_BigDecimal { BigDecimal dv = BigDecimal.ZERO ->
            try {
                delegate.toBigDecimal()
            }
            catch (e) {
                dv
            }
        }

        // safe toInteger that will default to 0 or any other integer when conversion fails
        String.metaClass.to_Integer { Integer dv = 0 ->
            try {
                delegate.toInteger()
            }
            catch (e) {
                dv
            }
        }

        String.metaClass.to_Long { Long dv = 0L ->
            try {
                delegate.toLong()
            }
            catch (e) {
                dv
            }
        }

        String.metaClass.to_BigInteger { BigInteger dv = BigInteger.ZERO ->
            try {
                delegate.toBigInteger()
            }
            catch (e) {
                dv
            }
        }
    }
}