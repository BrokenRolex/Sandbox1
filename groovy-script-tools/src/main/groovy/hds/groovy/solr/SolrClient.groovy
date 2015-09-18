package hds.groovy.solr

@groovy.util.logging.Log4j
class SolrClient {

    //String host = 'gfmwss02ldr'
    String host = 'sfmwss01lpr'
    Integer port = 3737
    Integer rows = 9999
    Integer start = 0
    
    SolrResult execute(SolrQuery sq) {
        List solrData = []
        String equery = URLEncoder.encode(sq.query, 'UTF-8')
        String http = "http://${host}:${port}/solr/${sq.store}/select?rows=${sq.rows}&start=${sq.start}&q=${equery}"
        println http
        log.debug http
        String text = new URL(http).text
        log.trace text
        Node response = new XmlParser().parseText(text)
        Node result = response.result[0]
        SolrResult sr = new SolrResult()
        Map solrDataType = [:]
        sr.dataType = solrDataType
        sr.data = solrData
        sr.result = result.attributes()
        result.doc.each { node ->
            Map map = [:]
            node.each {
                String tag = it.name()
                String name = it.@name
                if (tag == 'arr') {
                    List arr = []
                    it.each {
                        solrDataType[name] = "${it.name()}[]" as String
                        arr << it.text()
                    }
                    map[name] = arr
                }
                else {
                    map[name] = it.text()
                    solrDataType[name] = tag
                }
            }
            solrData << map
        }
        sr
    }
    
}
