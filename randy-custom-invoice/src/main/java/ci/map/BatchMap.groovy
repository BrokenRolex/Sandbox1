package ci.map

@groovy.util.logging.Log4j
class BatchMap {
    String name = ''
    String style = 'text' // maybe xml or json in the future?
    String fldsep = ','
    String recsep = "\n"
    Map groups = [:]
    void addGroup (Group group) {
        //log.debug "adding to map: $group"
        groups[group.name] = group
        group.setMap(this)
    }
    Group getGroupByName (String name) {
        groups.containsKey(name) ? groups.get(name) : null
    }
    void setFldsep (String s) {
        fldsep = org.apache.commons.lang.StringEscapeUtils.unescapeJava(s)
    }
    void setRecsep (String s) {
        recsep = org.apache.commons.lang.StringEscapeUtils.unescapeJava(s)
    }
    String toString () {
        String newline = System.getProperty("line.separator")
        StringBuffer buf = new StringBuffer(4096)
        buf.append("batchmap[name=\"").append(name).append("\" style=\"").append(style)
                .append("\" fldsep=\"").append(org.apache.commons.lang.StringEscapeUtils.escapeJava(fldsep))
                .append("\" recsep=\"").append(org.apache.commons.lang.StringEscapeUtils.escapeJava(recsep))
                .append("\"]").append(newline)
        groups.each { k, Group group ->
            buf.append("  ").append(group).append(newline)
            group.sequences.each { Sequence sequence ->
                buf.append("    ").append(sequence).append(newline)
                sequence.positions.each { Position position ->
                    buf.append("      ").append(position).append(newline)
                    position.macros.each { Macro macro ->
                        buf.append("        ").append(macro).append(newline)
                    }
                }
            }
        }
        buf.toString()
    }
}
