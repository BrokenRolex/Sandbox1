package hds.groovy.xml

import javax.xml.transform.OutputKeys
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.Transformer as TX
import javax.xml.transform.TransformerFactory as TF
import javax.xml.transform.stream.StreamResult as SR
import javax.xml.transform.stream.StreamSource as SS

/**
 * Transform xml files with xslt
 * 
 * <pre>
 * def tx = Transformer(new File('file.xsl'))
 * tx.transform(new File('input.xml'), new File('output.xml'))
 * </pre>
 * @author en032339
 */
@groovy.transform.CompileStatic
class Transformer {

    private File tFile
    private TX tx

    /**
     * Get a new instance of this transformer given an .xsl file.
     * @param file
     */
    Transformer(File file, String doctypeSystem = null) {
        if (file == null) {
             throw new Exception('transform file is null')
        }
        if (!file.isFile()) {
            throw new Exception("transform file [$file] does not exist")
        }
        tFile = file
        tx = TF.newInstance().newTransformer(new SS(file))
        
        if (doctypeSystem != null) {
            tx.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctypeSystem)
        }
        
    }
    
    /**
     * Transform an input file to produce an output file
     * @param iFile
     * @param oFile
     */
    void transform (File iFile, File oFile) {
        if (iFile == null) {
            throw new Exception("input file is null")
        }
        if (oFile == null) {
            throw new Exception("output file is null")    
        }
        if (!iFile.exists()) {
            throw new Exception("input file [$iFile] does not exist")
        }
        if (oFile.exists()) {
            if (iFile.getCanonicalFile() == oFile.getCanonicalFile()) {
                throw new Exception("input file [$iFile] and output file [$oFile] refer to the same file")
            }
            if (oFile.getCanonicalFile() == tFile.getCanonicalFile()) {
                throw new Exception("output file [$oFile] and transform file [$tFile] refer to the same file")
            }
        }
        if (iFile.getCanonicalFile() == tFile.getCanonicalFile()) {
            throw new Exception("input file [$iFile] and transform file [$tFile] refer to the same file")
        }
        tx.transform(new SS(iFile), new SR(oFile))
    }
    
    /**
     * Transform an input stream to produce an output file
     * @param iStream
     * @param oFile
     */
    void transform (InputStream iStream, File oFile) {
        if (oFile.exists()) {
            if (oFile.getCanonicalFile() == tFile.getCanonicalFile()) {
                throw new Exception("output file [$oFile] and transform file [$tFile] refer to the same file")
            }
        }
        tx.transform(new SS(iStream), new SR(oFile))
    }
    
    void transform (InputStream i, OutputStream o) {
        tx.transform(new SS(i), new SR(o))
    }
    
    void transform (Source ss, Result sr) {
         tx.transform(ss, sr)
    }
    
    void transform (Source ss, File oFile) {
        if (oFile.exists()) {
            if (oFile.getCanonicalFile() == tFile.getCanonicalFile()) {
                throw new Exception("output file [$oFile] and transform file [$tFile] refer to the same file")
            }
        }
        tx.transform(ss, new SR(oFile))
    }
    
}
