package script

import hds.groovy.db.*
import java.util.regex.Pattern

class StringValidator {
    
    int hostIsReachableTimeout = 2000

    // map of validation rules
    Map rules = [
        'HOST': { String s ->
            if (s == null || s.length() == 0) return false
            isaHost(s)
        },
        'HOST_LIST': { String s ->
            if (s == null || s.length() == 0) return false
            Boolean result = true
            for (String ss in s.split(',')) {
                result = result && isaHost(ss)
            }
            result
        },
        'host': { String s ->
            if (s == null || s.length() == 0) return true
            isaHost(s)
        },
        'host_list': { String s ->
            if (s == null || s.length() == 0) return true
            Boolean result = true
            for (String ss in s.split(',')) {
                if (!isaHost(ss)) return false
            }
            true
        },
        'FILE': { String s ->
            if (s == null || s.length() == 0) return false
            new File(s).isFile()
        },
        'file': { String s ->
            if (s == null || s.length() == 0) return true
            new File(s).isFile()
        },
        'FILE_LIST': { String s ->
            if (s == null || s.length() == 0) return false
            for (String ss in s.split(',')) {
                if (!new File(ss).isFile()) return false
            }
            true
        },
        'file_list': { String s ->
            if (s == null || s.length() == 0) return true
            for (String ss in s.split(',')) {
                if (!new File(ss).isFile()) return false
            }
            true
        },
        'DIR': { String s ->
            if (s == null || s.length() == 0) return false
            new File(s).isDirectory()
        },
        'dir': { String s ->
            if (s == null || s.length() == 0) return true
            new File(s).isDirectory()
        },
        'DIR_LIST': { String s ->
            if (s == null || s.length() == 0) return false
            for (String ss in s.split(',')) {
                if (!new File(ss).isDirectory()) return false
            }
            true
        },
        'dir_list': { String s ->
            if (s == null || s.length() == 0) return true
            for (String ss in s.split(',')) {
                if (!new File(ss).isDirectory()) return false
            }
            true
        },
        'MKDIR': { String s ->
            if (s == null || s.length() == 0) return false
            makeDirectory(s)
        },
        'mkdir': { String s ->
            if (s == null || s.length() == 0) return true
            makeDirectory(s)
        },
        'MKDIR_LIST': { String s ->
            if (s == null || s.length() == 0) return false
            for (String ss in s.split(',')) {
                if (!makeDirectory(ss)) return false
            }
            true
        },
        'mkdir_list': { String s ->
            if (s == null || s.length() == 0) return true
            for (String ss in s.split(',')) {
                if (!makeDirectory(ss)) return false
            }
            true
        },
        'LONG': { String s ->
            if (s == null || s.length() == 0) return false
            isLong(s)
        },
        'long': { String s ->
            if (s == null || s.length() == 0) return true
            isLong(s)
        },
        'LONG_LIST': { String s ->
            if (s == null || s.length() == 0) return false
            for (String ss in s.split(',')) {
                if (!isLong(ss)) return false
            }
            true
        },
        'long_list': { String s ->
            if (s == null || s.length() == 0) return true
            for (String ss in s.split(',')) {
                if (!isLong(ss)) return false
            }
            true
        },
        'INTEGER': { String s ->
            if (s == null || s.length() == 0) return false
            isInteger(s)
        },
        'integer': { String s ->
            if (s == null || s.length() == 0) return true
            isInteger(s)
        },
        'INTEGER_LIST': { String s ->
            if (s == null || s.length() == 0) return false
            for (String ss in s.split(',')) {
                if (!isInteger(ss)) return false
            }
            true
        },
        'integer_list': { String s ->
            if (s == null || s.length() == 0) return true
            for (String ss in s.split(',')) {
                if (!isInteger(ss)) return false
            }
            true
        },
        'EMPTY': { String s ->
            s == null || s.length() == 0 ? true : false
        },
        'NOTEMPTY': { String s ->
            s == null || s.length() == 0 ? false : true
        },
        'DATABASE': { String s ->
            if (s == null || s.length() == 0) return false
            isaDatabase(s)
        },
        'database': { String s ->
            if (s == null || s.length() == 0) return true
            isaDatabase(s)
        },
        'DATABASE_LIST': { String s ->
            if (s == null || s.length() == 0) return false
            for (String ss in s.split(',')) {
                if (!isaDatabase(ss)) return false
            }
            true
        },
        'database_list': { String s ->
            if (s == null || s.length() == 0) return true
            for (String ss in s.split(',')) {
                if (!isaDatabase(ss)) return false
            }
            true
        },
        'DIGITS': { String s ->
            if (s == null || s.length() == 0) return false
            s =~ /\D/ ? false : true
        },
        'digits': { String s ->
            if (s == null || s.length() == 0) return true
            s =~ /\D/ ? false : true
        },
        'DIGITS_LIST': { String s ->
            if (s == null || s.length() == 0) return true
            for (String ss in s.split(',')) {
                if (s =~ /\D/) return false
            }
            true
        },
        'digits_list': { String s ->
            if (s == null || s.length() == 0) return true
            for (String ss in s.split(',')) {
                if (s =~ /\D/) return false
            }
            true
        },
        'REGEX': { String s, Pattern pattern ->
            if (s == null || s.length() == 0) return false
            pattern.matcher(s).matches()
        },
        'regex': { String s, Pattern pattern ->
            if (s == null || s.length() == 0) return true
            pattern.matcher(s).matches()
        }
    ]

    StringValidator () {
        // synonyms
        rules['TNSNAME'] = rules['DATABASE']
        rules['tnsname'] = rules['database']
        rules['TNSNAME_LIST'] = rules['DATABASE_LIST']
        rules['tnsname_list'] = rules['database_list']
        rules['INT'] = rules['INTEGER']
        rules['INT_LIST'] = rules['INTEGER_LIST']
        rules['int_list'] = rules['integer_list']
        rules['int'] = rules['integer']
        rules['MKDIRS'] = rules['MKDIR']
        rules['mkdirs'] = rules['mkdir']
        rules['empty'] = rules['EMPTY']
        rules['notempty'] = rules['NOTEMPTY']
        rules['MKDIRS_LIST'] = rules['MKDIR_LIST']
        rules['mkdirs_list'] = rules['mkdir_list']
    }
    
    Set rules () {
        rules.keySet()
    }

    Boolean validate (String data, String rule) {
        if (data == null || rule == null) {
            return false
        }
        if (rules.containsKey(rule)) {
            return rules[rule](data)
        }
        String[] ruleAry= rule.split(':', 2)
        if (ruleAry.length == 2) {
            if (ruleAry[0].equalsIgnoreCase('regex')) {
                if (rules.containsKey(ruleAry[0])) {
                    return rules[ruleAry[0]](data, ~ruleAry[1])
                }
            }
        }
        false
    }

    private Boolean makeDirectory (String s) {
        File f = new File(s)
        if (f.exists()) {
            if (!f.isDirectory()) {
                return false
            }
        }
        else {
            f.mkdirs()
            if (!f.isDirectory()) {
                return false
            }
        }
        true
    }

    private Boolean isaDatabase (String s) {
        //println "isaDatabase($s)"
        try {
            OracleTnsnames.instance.getAlias(s)
            return true
        }
        catch (e) {
            //println e.message
            return false
        }
    }

    private Boolean isInteger (String s) {
        try {
            Integer.valueOf(s)
            return true
        }
        catch (e) {
            return false
        }
    }

    private Boolean isLong (String s) {
        try {
            Long.valueOf(s)
            return true
        }
        catch (e) {
            return false
        }
    }

    private Boolean isaHost (String s) {
        Host.isReachable(s, hostIsReachableTimeout) ? true : false
    }

}
