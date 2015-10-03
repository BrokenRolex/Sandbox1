package script

class Meta {

    private Meta () {
        throw new Exception("Meta is a static class")
    }

    /**
     * Add more methods to the File class<p>
     * Methods added...
     * <ul>
     * <li>remove()
     *     <ul>Deletes a file or directory.
     *     Throws an exception if the delete was not successful.</ul>
     * </li>
     * <li>copy(File)
     *     <ul>Copies a file to another File.
     *     The source file cannot be a directory.
     *     If the destination file is a directory then the source name will be used.
     *     Any errors cause an exception to be thrown.
     *     </ul>
     * </li>
     * <li>move(File)
     *     <ul>Performs a copy(File) then a remove()</ul>
     * </li>
     * <li>rename(String)
     *     <ul>Renames a file or directory.
     *     Does not allow a file or directory to be moved.
     *     Any errors cause an exception to be thrown.</ul>
     * </li>
     * </ul>
     */
    static void addFileMethods () {

        File.metaClass.remove = { FileUtils.delete(delegate) }

        File.metaClass.copyTo = { dest ->
            if (dest == null) {
                throw new Exception("cannot copy [$delegate] to [$dest]")
            }
            if (dest instanceof String) {
                return FileUtils.copy(delegate, new File(dest))
            }
            if (dest instanceof File) {
                return FileUtils.copy(delegate, dest)
            }
            throw new Exception("cannot copy [$delegate] to [$dest]")
        }

        File.metaClass.moveTo = { dest ->
            if (dest == null) {
                throw new Exception("cannot move [$delegate] to [$dest]")
            }
            if (dest instanceof String) {
                return FileUtils.move(delegate, new File(dest))
            }
            if (dest instanceof File) {
                return FileUtils.move(delegate, dest)
            }
            throw new Exception("cannot move [$delegate] to [$dest]")
        }

        File.metaClass.renameAs = { String dest ->
            if (dest == null) {
                throw new Exception("cannot rename [$delegate] as [$dest]")
            }
            FileUtils.rename(delegate, dest)
        }

        File.metaClass.scpTo = { String host, String dest ->
            Proc.scp([delegate.path, "$host:$dest"])
        }
    }

    static void addStringMethods () {

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