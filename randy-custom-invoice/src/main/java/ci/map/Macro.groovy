package ci.map

@groovy.util.logging.Log4j
abstract class Macro extends Field {
    Position position // the position that this macro belongs to
    Object obj1
    Object obj2
    Boolean valid

    Macro () {
        (value, source, valid) = ['', '', true]
    }

    abstract String execute (Map data, String s)

    String toString () {
        "macro[${this.getClass().getSimpleName()}, value=\"${value}\", source=\"${source}\"]"
    }
}