package ci.map

@groovy.util.logging.Log4j
abstract class Macro extends Data {
    Position position // the position that this macro belongs to
    Object obj1
    Object obj2
    Macro () { }
    abstract String execute (Map m, String s)
    String toString () {
        "macro[${this.getClass().getSimpleName()}, value=\"${value}\", source=\"${source}\"]"
    }
}