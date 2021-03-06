package ci.map

@groovy.util.logging.Log4j
class Position extends Data {
    Integer num
    Sequence sequence // the sequence that this position belongs to
    private List macros = [] // all the macros that this position will execute
    void addMacro (Macro m) {
        macros << m
    }
    String toString () {
        "position[num='${num}', value='${value}', source='${source}']"
    }
}
