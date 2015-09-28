package custom_invoices.map

@groovy.util.logging.Log4j
class Sequence {
    Integer num
    Group group // the group that this sequence belongs to
    List positions = [] // all the positions in this sequence
    void addPosition (Position p) {
        if (p) { positions << p }
    }
    String toString () {
        "sequence[num=${num}]"
    }
}
