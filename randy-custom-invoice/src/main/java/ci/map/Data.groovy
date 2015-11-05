package ci.map

@groovy.util.logging.Log4j
abstract class Data {
    String source = 'value'
    String value

    // look at source and value to decide what the field data is
    String data (Map data) {
        String field = null
        if (source?.equals('data')) {
            if (data.containsKey(value)) {
                field = data[value]
            }
            else {
                log.warn "unknown data key [$value]"
            }
        }
        else if (source?.equals('value')) {
            field = value
        }
        else {
            log.warn "unknown source [$source] assuming [value]"
            field = value
        }
        field = (field == null) ? '' : field
        field.replaceAll(~/[\n\r]/, '')
    }
}
