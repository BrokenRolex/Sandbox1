package hds.groovy.solr

import java.net.URLEncoder

@groovy.util.logging.Log4j
class SolrCatentry {

    Integer storeId = 10051
    String lang = 'en_US'
    Boolean unstructured
    Map data
    List partNumbers
    Set skeys // structured keys

    SolrClient sc = new SolrClient()
    SolrQuery ssq = new SolrQuery() // structured
    SolrResult ssr
    SolrQuery usq = new SolrQuery() // unstructured
    SolrResult usr

    void query (String... args) {
        _get_structured(args)
    }

    /**
     * query structured data
     */
    void _get_structured (args) {
        log.debug "_get_structured($args)"
        skeys = []
        unstructured = false
        def p_pat = ~/\AP/
        data = [:]
        String query = "(${args.collect{"partNumber:$it"}.join(' OR ')})"
        log.debug "query = ${query}"
        String store = "MC_${storeId}_CatalogEntry_${lang}"
        ssq.store = store
        ssq.query = query
        ssr = sc.execute(ssq)
        ssr.data.each { map ->
            if (map.catenttype_id_ntk_cs == 'ProductBean') { // keep only product beans
                map.partNumber = map.partNumber - p_pat // remove the begining 'P'
                if (map.containsKey('seo_token_ntk')) {
                    map.seo_url_path = '/shop/p/' + URLEncoder.encode(map.seo_token_ntk, 'UTF-8')
                }
                else {
                    map.seo_url_path = ''
                }
                map.keySet().each {skeys << it}
                data[map.partNumber] = map
            }
        }
        partNumbers = data.keySet().sort()
    }

    /**
     * query unstructured data
     */
    void _get_unstructured () {
        log.debug "_get_unstructured()"
        List catentry_ids = data.keySet().collect{data[it].catentry_id}.findAll{it != null}
        partNumbers.each { data[it].unstructured = null}
        String query = '(' + catentry_ids.collect{"catentry_id:$it"}.join(' or ') + ')'
        String store = "MC_${storeId}_CatalogEntry_Unstructured_${lang}"
        usq.store = store
        usq.query = query
        usr = sc.execute(usq)
        usr.data.each { Map map ->
            String catentry_id = map.catentry_id
            if (catentry_id) {
                data.keySet().each {
                    Map structured = data[it]
                    if (structured.catentry_id == catentry_id) {
                        if (structured.unstructured == null) {
                            structured.unstructured = []
                        }
                        structured.unstructured << map
                    }
                }
            }
        }
        unstructured = true
    }

    def methodMissing (String name, args) {
        log.debug "methodMissing($name, $args)"
        def seq2int = {String arg -> arg?.isInteger() ? arg.toInteger() : null }.memoize()
        def bySeq = {a, b -> seq2int(a.hds_sequence) <=> seq2int(b.hds_sequence)}
        Map imageFilterMap = [
            smallimage: {it.rulename == 'SMALLIMAGE'},
            thumbnail:  {it.rulename == 'THUMBNAIL'},
            largeimage: {it.rulename == 'LARGEIMAGE'},
            enlarged:   {it.rulename == 'ENLARGED'},
        ]
        Map result = [:]
        if (data) {
            if (skeys.contains(name)) {
                def list = (args.size() > 0) ? args.findAll{partNumbers.contains(it)} : partNumbers
                list.each { pn -> result[pn] = data[pn][name] }
            }
            else if (imageFilterMap.containsKey(name)) {
                if (!unstructured) {
                    _get_unstructured()
                }
                def imageFilter = imageFilterMap[name]
                def list = (args.size() > 0) ? args.findAll{partNumbers.contains(it)} : partNumbers
                list.each { pn ->
                    result[pn] = data[pn].unstructured.findAll(imageFilter).sort(bySeq).collect{it.path}
                }
            }
        }
        result
    }

}