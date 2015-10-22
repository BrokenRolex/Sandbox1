package ci.map

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
        }
        else if (source?.equals('value')) {
            field = value
        }
        else {
            field = value
        }
        field = (field == null) ? '' : field
        field.replaceAll(~/[\n\r]/, '')
    }
}
