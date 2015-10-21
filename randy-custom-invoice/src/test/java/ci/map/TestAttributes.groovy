package ci.map

import org.xml.sax.Attributes

class TestAttributes implements Attributes {
    
        private List names = []
        private List values = []
    
        void setValue (String name, String value) {
            if (name) {
                def index = names.findIndexOf {it == name}
                if (index >= 0) {
                    values[index] = value
                }
                else {
                    names << name
                    values << (value ?: '')
                }
            }
        }
    
        @Override
        int getLength() {
            names.size()
        }
    
        @Override
        String getURI(int index) {
            null
        }
    
        @Override
        String getLocalName(int index) {
            (index >= 0 && index < getLength()) ? names[index] : null
        }
    
        @Override
        String getQName(int index) {
            (index >= 0 && index < getLength()) ? names[index] : null
        }
    
        @Override
        String getType(int index) {
            null
        }
    
        @Override
        String getValue(int index) {
            (index >= 0 && index < getLength()) ? values[index] : null
        }
    
        @Override
        int getIndex(String uri, String localName) {
            names.findIndexOf { it == localName}
        }
    
        @Override
        int getIndex(String qName) {
            names.findIndexOf { it == qName}
        }
    
        @Override
        String getType(String uri, String localName) {
            null
        }
    
        @Override
        String getType(String qName) {
            null
        }
    
        @Override
        String getValue(String uri, String localName) {
            getValue(getIndex(localName))
        }
    
        @Override
        String getValue(String qName) {
            getValue(getIndex(qName))
        }
    
    }
