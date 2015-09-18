package hds.groovy.zip

import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry

/**
 * Simple interface around Java's java.util.zip.* classes to create zip files
 */
@groovy.transform.CompileStatic
class ZipCreator {

    private File zipFile
    private ZipOutputStream zos

    /**
     * Create a new instance of this class that will create the given file.
     * @param file to create (zip archive)
     */
    ZipCreator (File file) {
        zipFile = file
        zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))
    }

    /**
     * Add a file to the zip archive.
     * <br/>The entry added will have the full path of the file.
     * @param file to add to the zip file
     */
    void add (File file) {
        add(file, file.path)
    }

    /**
     * Add a file to the zip archive as a <i>name</i>.
     * @param file to add to the zip file
     * @param name to use (zipEntry)
     */
    void add (File file, String name) {
        ZipEntry ze = new ZipEntry(name)
        zos.putNextEntry(ze)
        zos << new BufferedInputStream(new FileInputStream(file))
    }

    /**
     * Add a properties object to the zip file.
     * @param props object to add
     * @param name zip entry name
     */
    void add (Properties props, String name) {
        ZipEntry ze = new ZipEntry(name)
        zos.putNextEntry(ze)
        props.store(zos, '')
    }

    /**
     * Add a text string to a zip file as an file entry.
     * @param text to add to the zip file
     * @param name zip entry name
     */
    void add (String text, String name) {
        ZipEntry ze = new ZipEntry(name)
        zos.putNextEntry(ze)
        zos << text
    }

    /**
     * Close the zip output stream.<p>
     * You should do this when done adding zip entries otherwise all data might not be flushed.
     * Once this method is called you can no longer add zip entries.
     */
    void close () {
        zos?.close()
    }
}
