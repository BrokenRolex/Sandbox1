package script.xml

import groovy.xml.*

@groovy.util.logging.Log4j
class Combiner {

    Node root
    NodeList children
    String encoding = 'UTF-8'
    String dtd

    void add (File file) {
        if (file.isFile()) {
            add(new XmlParser().parse(file))
        }
    }

    void add (Node node) {
        if (root == null) {
            root = node
            children = root.children()
        }
        else {
            node.children().each { children.add(children.size(), it) }
        }
    }

    void write (File file) {
        if (root == null) {
            throw new Exception("nothing available to write")
        }
        log.info "writing to [$file]"
        file.withWriter(encoding) { wtr ->
            wtr << "<?xml version=\"1.0\" encoding=\"${encoding}\" ?>\n"
            if (dtd) {
                wtr << "<!DOCTYPE import SYSTEM \"${dtd}\">\n"
            }
            def xnp = new XmlNodePrinter(new PrintWriter(wtr))
            xnp.preserveWhitespace = true
            xnp.print(root)
        }
        root = null
        children = null
    }

}