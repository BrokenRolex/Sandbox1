package ci.map

class Field {
    String source
    String value

    // look at source and value to decide what the field data is
    String sourceData (Map data) {
        String field
        if (source?.equals('data')) { field = data[value] }
        else if (source?.equals('value')) { field = value }
        else { field = value }
        field.replaceAll(~/[\n\r]/, '')
        field ?: ''
    }
}
