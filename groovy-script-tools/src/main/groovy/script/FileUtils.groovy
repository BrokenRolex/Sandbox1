package script

class FileUtils {
    
    FileUtils () {
        throw new Exception("FileUtils is a static class")
    }

    /**
     * Delete a file or a directory (and its contents).
     * @param file File or Directory to delete.
     */
    static void delete (File file) {
        def method = 'FileUtils.delete:'
        if (file != null && file.exists()) {
            def success = true
            if (file.isDirectory()) {
                if (!file.deleteDir()) {
                    success = false
                }
            }
            else {
                if (!file.delete()) {
                    success = false
                }
            }
            if (!success) {
                throw new Exception("$method delete failed for [$file]")
            }
        }
    }

    /**
     * Copy a file from a Source to a Destination.
     * <p>
     * If the destination is a Directory then the Source will be
     * copied there with the same name.
     * <p>
     * If the Source or Destination do not have a parent file the current
     * directory is set as their parent file.
     * <p> 
     * If the Source and Destination refer to the same file the copy
     * will fail. Equality is determined by canonicalPath.
     * 
     * @param source
     * @param destination
     * @param overWrite
     * @throws Exception if
     *  <ul>
     *  <li>The source or destination is null</li>
     *  <li>The source is not a file that exists.</li>
     *  <li>The destination does not exist after a copy.</li>
     *  </ul>
     */
    static File copy (File source, File destination, Boolean overWrite = true) {
        def method = 'FileUtils.copy:'
        if (source == null) {
            throw new Exception("$method source file is null")
        }
        if (destination == null) {
            throw new Exception("$method destination file is null")
        }
        if (source.parentFile == null) {
            source = new File(new File('.'), source.name)
        }
        if (destination.parentFile == null) {
            destination = new File(new File('.'), destination.name)
        }
        if (!source.isFile()) {
            throw new Exception("$method copy of [$source] to [$destination] failed, source is not a file")
        }
        if (destination.isDirectory()) {
            destination = new File(destination, source.name)
        }
        if (source.canonicalPath == destination.canonicalPath) {
            throw new Exception("$method copy of [$source] to [$destination] failed, source and destination are the same file")
        }
        if (destination.exists()) {
            if (overWrite) {
                delete(destination)
            }
            else {
                throw new Exception("$method copy of [$source] to [$destination] failed, destination already exists")
            }
        }
        source.withInputStream { destination << it } // perform the copy
        if (!destination.isFile()) {
            throw new Exception("$method copy of [$source] to [$destination] failed, destination does not exist")
        }
        destination
    }

    /**
     * Move a file. Same as copy then delete.
     * See {@link #copy} and {@link #delete} for details.
     * @param src Source file
     * @param dest Destination file
     * @param overWrite Optional. The default is <i>true</i> which means that
     *  if the destination already exists it will be deleted first.
     *  If overWrite is <i>false</i> and the destination file exists
     *  then an exception will be thrown.
     */
    static File move (File src, File dest, Boolean overWrite = true) {
        File newFile = copy(src, dest, overWrite)
        delete(src)
        newFile
    }

    /**
     * Rename a file or a directory.
     * @param sourceFile File to rename.
     * @param newName New name of file.
     * @param overWrite Default is true. If the destination file already exists delete it first.
     * @return file object
     */
    static File rename (File sourceFile, String newName, Boolean overWrite = true) {
        def method = 'FileUtils.rename:'
        if (sourceFile == null) {
            throw new Exception("$method source file is null")
        }
        if (newName == null) {
            throw new Exception("$method new name is null")
        }
        if (newName.contains(File.separator)) {
            throw new Exception("$method source [$sourceFile] cannot be renamed to [$newName], invalid character [${File.separator}] in name")
        }
        if (sourceFile.parentFile == null) {
            sourceFile = new File(new File('.'), sourceFile.name).canonicalFile
        }
        if (!sourceFile.exists()) {
            throw new Exception("$method source [$sourceFile] does not exist, rename to [$newName] failed")
        }
        String type = sourceFile.isFile() ? 'file' : 'directory'
        if (newName == sourceFile.name) {
            throw new Exception("$method $type [$sourceFile] cannot be renamed to [$newName] because the name is the same")
        }
        File dest = new File(sourceFile.parentFile, newName)
        if (dest.exists()) {
            if (overWrite) {
                delete(dest)
            }
            else {
                throw new Exception("$method $type [$sourceFile] cannot be renamed to [$newName] because it already exists")
            }
        }
        Boolean success = sourceFile.renameTo(dest)
        if (!success || !dest.exists()) {
            throw new Exception("$method rename of [${sourceFile.canonicalPath}] to [$newName] failed")
        }
        return dest
    }

}
