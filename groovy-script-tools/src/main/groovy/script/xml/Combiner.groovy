package script.xml

import org.dom4j.*
import org.dom4j.io.*

@groovy.util.logging.Log4j
class Combiner {
    private static final String ENCODING = 'UTF-8'

    String dtd
    String encoding
    List files = []
    int size = 0
    int added = 0 // number of documents added
    boolean overwrite
    private Document document
    private Element root

    void add (String xml) {
        if (xml == null) {
            throw new Exception("xml string is null")
        }
        addDoc(DocumentHelper.parseText(xml))
        size += xml.length()
    }

    void add (File file) {
        if (file == null) {
            throw new Exception("cannot add a null file object")
        }
        if (!file.isFile()) {
            throw new Exception("cannot add [$file] because it does not exist")
        }
        log.debug "adding [$file] size [${file.size()}]"
        //addDoc(new SAXReader().read(new BufferedInputStream(new FileInputStream(file))))
        addDoc(new SAXReader().read(file))
        size += file.size()
        files << file
    }

    private void addDoc (Document doc) {
        if (doc == null) {
            throw new Exception("cannot add null document")
        }
        if (document == null) { // first document ?
            document = doc
            if (encoding == null) {
                encoding = document.getXMLEncoding()
            }
            root = document.getRootElement()
            if (dtd != null) {
                document.addDocType(root.name, null, dtd)
            }
            return
        }

        // subsequent documents...

        Document subdoc = doc
        Element subroot = subdoc.getRootElement()

        // add content
        if (root.name != subroot.name) {
            log.warn "document root [${root.name}] not the same as added document root [${subroot.name}]"
        }
        subroot.elements().each { root.add(it.detach()) }

        added++
    }

    /**
     * Write combined xml document to a file
     * @param file to write xml to
     * @param filter optional Visitor object to apply before writing
     */
    void write (File file, Visitor visitor = null) {
        if (file == null) {
            throw new Exception("cannot write to a null file object")
        }
        if (file.exists()) {
            if (!overwrite) {
                throw new Exception("cannot overwrite [$file] if you really want to write to this file delete it first")
            }
            file.delete()
        }
        if (document == null) {
            throw new Exception("cannot write [$file] no documents have been combined")
        }
        log.info "writing [$file] size [$size]"
        if (visitor != null) {
            document.accept(visitor)   
            if (visitor.metaClass.respondsTo(visitor, 'postProcessing', Document)) {
                visitor.postProcessing(document)
            }
        }
        OutputFormat format = OutputFormat.createPrettyPrint()
        format.setEncoding(encoding == null ? ENCODING : encoding)
        format.setIndentSize(0)
        file.withOutputStream { bos ->
            XMLWriter xw = new XMLWriter(bos, format)
            xw.write(document)
            xw.close()
        }
    }
    
    String toString () {
        if (document == null) {
            throw new Exception("document is null")
        }
        OutputFormat format = OutputFormat.createPrettyPrint()
        // StringWriter uses StringBuffer. String Buffer default size is 16 which is a bit small.
        // Try to guestimate a good value for the initial string size.
        StringWriter sw = new StringWriter(255 + size)
        XMLWriter xw = new XMLWriter(sw, format)
        xw.write(document)
        return sw.toString()
    }

    void reset () {
        document = null
        encoding = null
        root = null
        files = []
        size = 0
        added = 0
    }

    void deleteAddedFiles () {
        files.each { it.delete() }
    }

}