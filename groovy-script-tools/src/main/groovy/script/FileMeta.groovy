package script

class FileMeta {

    private Meta () {
        throw new Exception("FileMeta is a static class")
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
    static void add () {

        File.metaClass.remove = {
            File f = delegate
            if (f.exists()) {
                if (f.isFile()) {
                    if (!f.delete()) {
                        throw new Exception("cannot delete file [$f] check permissions")
                    }
                }
                else if (f.isDirectory()) {
                    if (!f.delete()) {
                        throw new Exception("cannot delete directory [$f] check permissions")
                    }
                }
                else {
                    throw new Exception("cannot delete [$f] not a file or a directory")
                }
            }
        }

        File.metaClass.copyTo = { dest ->
            File f = delegate
            f = !f.parentFile ? new File('.', f.name) : f
            if (!f.isFile()) {
                throw new Exception("cannot copy [$f] to [$dest] not a file")
            }
            if (dest == null) {
                throw new Exception("cannot copy [$f] to [$dest]")
            }
            File destination
            if (dest instanceof String) {
                destination = new File(dest)
            }
            else if (dest instanceof File) {
                destination = dest
            }
            else {
                throw new Exception("cannot copy [$f] to [$dest]")
            }
            if (destination.parentFile == null) {
                destination = new File('.', destination.name)               
            }
            if (destination.exists()) {
                if (destination.isDirectory()) {
                    destination = new File(destination, f.name)
                }
                if (destination.isFile()) {
                    destination.delete()
                }
                else {
                    throw new Exception("cannot copy [$f] to [$dest]")
                }
            }
            f.withInputStream { destination << it } // perform the copy
        }

        File.metaClass.moveTo = { dest ->
            File f = delegate
            f = !f.parentFile ? new File('.', f.name) : f
            f.copyTo(dest)
            f.remove()
        }


        // how will this be different form File.renameTo ?

        File.metaClass.renameAs = { String name ->
            if (!name || name.contains(File.separator)) {
                throw new Exception("cannot rename [$delegate] as [$name] bad name")
            }
            File f = delegate
            f = !f.parentFile ? new File('.', f.name) : f
            if (f.exists()) {
                if (f.isFile() || f.isDirectory()) {
                    f.renameTo(new File(f.parentFile, name))
                }
                else {
                    throw new Exception("cannot rename [$f] as [name] ?")
                }
            }
            else {
                throw new Exception("cannot rename [$f] as [name] missing")
            }
        }

        // unless I use jsch here, these will only work in *nix

        /** 
         * copy a file to another host
         */
        File.metaClass.scpTo = { String host, String path ->
            File file = delegate
            def proc = "scp -q -oBatchMode=yes ${file.path} ${host}:${path}".execute()
            proc.waitForProcessOutput()
            proc.exitValue()
        }

        /** 
         * copy a file from another host
         */
        File.metaClass.scpFrom = { String host, String path ->
            File file = delegate
            def proc = "scp -q -oBatchMode=yes ${host}:${path} ${file.path}".execute()
            proc.waitForProcessOutput()
            proc.exitValue()
        }
    }

}