package custom_invoices.map

@groovy.util.logging.Log4j
class Group {
    String name
    BatchMap map // the batch map that this group belongs to
    List sequences = []
    void addSequence (Sequence s) {
        if (s) {
            s.num = sequences.size()
            sequences << s
        }
    }
    String toString () {
        "group[name=${name}]"
    }
}

