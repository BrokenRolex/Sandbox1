package hds.groovy.zip

import java.util.zip.ZipFile
import java.util.zip.ZipEntry

/**
 * Simple interface around Java's java.util.zip.* classes to extract zip files
 * 
 * @author en032339
 */
class ZipExtractor {

    private File file
    private List zipEntries
    private ZipFile zipFile

    /**
     * Create a new ZipExtractor object that will operate on a file
     * @param zipFile zip file to work with
     */
    ZipExtractor (File file) {
        this.file = file
        zipFile = new ZipFile(file)
        zipEntries = []
        zipFile.entries().each { zipEntries << it }
    }

    /**
     * Extract a zip file.
     * @param outDir directory to extract the zip file to
     * @param junkPaths 'junk' the paths in the zip file (default = true)
     */
    void extract (File outDir, boolean junkPaths = true) {
        checkDir(outDir)
        zipEntries.each { ZipEntry zipEntry ->
            File outFile = new File(outDir, junkPaths ? new File(zipEntry.name).name : zipEntry.name)
            checkDir(outFile.parentFile)

            //FileOutputStream fos = new FileOutputStream(outFile)
            //org.apache.commons.io.IOUtils.copy(zipFile.getInputStream(zipEntry), fos)
            //fos.flush()
            //fos.close()

            outFile.withOutputStream { OutputStream os ->
                def bos = new BufferedOutputStream(os)
                def is = zipFile.getInputStream(zipEntry)
                def bis = new BufferedInputStream(is)
                bis.withStream { bos << it }
                bos.flush()
            }
        }
    }

    private void checkDir (File dir) {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new Exception("output [$dir] isn't a directory")
            }
        }
        else {
            if (!dir.mkdirs()) {
                throw new Exception("cannot create directory [$dir]")
            }
        }
    }

    /**
     * Get a list of entries in a zip file
     * @return a List of entries in the zip file
     */
    List getList () {
        return zipEntries
    }

    /**
     * Look for a properties file in the zip file and return it as a Properties object.
     * @param name zip entry name to look for that will be loaded into a properties object
     * @return properties object
     */
    Properties getPropertiesEntry (String name) {
        ZipEntry zipEntry = (ZipEntry) zipEntries.find { ZipEntry zipEntry -> zipEntry.name == name }
        if (zipEntry == null) {
            throw new Exception("cannot find zip entry [$name] in zip file [$zipFile]")
        }
        Properties props = new Properties()
        props.load(zipFile.getInputStream(zipEntry))
        return props
    }

    /**
     * Look for a properties file in the zip file and return it as a Properties object.
     * <p/>
     * This method looks for the default properties file entry in the zip file.<br/>
     * If the zip file is called iliketoast.zip, calling this method will look for
     * a file called iliketoast.properties in the zip file (no path), load it
     * into a properties object and return it.
     * @return properties object
     */
    Properties getPropertiesEntry () {
        return getPropertiesEntry(new File(zipFile.name).name.replaceAll(/\.zip\z/, '.properties'))
    }

    /**
     * Look for a zip entry by name and load it as a text file into a String and return it.
     * @param name the name of the zip entry to read
     * @return a string of the contents of the zip entry
     */
    String getTextEntry (String name) {
        ZipEntry zipEntry = (ZipEntry) zipEntries.find { ZipEntry zipEntry -> zipEntry.name == name }
        if (zipEntry == null) {
            throw new Exception("cannot find zip entry [$name] in zip file [$zipFile]")
        }
        return zipFile.getInputStream(zipEntry).text
    }

}