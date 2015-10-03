package script

import script.sql.*
import java.util.regex.Pattern

class StringValidator {

    Integer hostIsReachableTimeout = 2000

    // map of validation rules
    Map rules = [
        'host': { String s ->
            isEmpty(s) ? true : isaHost(s)
        },
        'HOST': { String s ->
            isEmpty(s) ? false : isaHost(s)
        },
        'host_list': { String s ->
            isEmpty(s) ? true : (s.split(/,/,-1).every{isaHost(it)})
        },
        'HOST_LIST': { String s ->
            isEmpty(s) ? false : (s.split(/,/,-1).every{isaHost(it)})
        },
        'file': { String s ->
            isEmpty(s) ? true : new File(s).isFile()
        },
        'FILE': { String s ->
            isEmpty(s) ? false : new File(s).isFile()
        },
        'file_list': { String s ->
            isEmpty(s) ? true : (s.split(/,/,-1).every{new File(it).isFile()})
        },
        'FILE_LIST': { String s ->
            isEmpty(s) ? false : (s.split(/,/,-1).every{new File(it).isFile()})
        },
        'dir': { String s ->
            isEmpty(s) ? true : new File(s).isDirectory()
        },
        'DIR': { String s ->
            isEmpty(s) ? false : new File(s).isDirectory()
        },
        'dir_list': { String s ->
            isEmpty(s) ? true : (s.split(/,/,-1).every{new File(it).isDirectory()})
        },
        'DIR_LIST': { String s ->
            isEmpty(s) ? false : (s.split(/,/,-1).every{new File(it).isDirectory()})
        },
        'mkdir': { String s ->
            isEmpty(s) ? true : mkdirs(s)
        },
        'MKDIR': { String s ->
            isEmpty(s) ? false : mkdirs(s)
        },
        'mkdir_list': { String s ->
            isEmpty(s) ? true : (s.split(/,/,-1).every{mkdirs(it)})
        },
        'MKDIR_LIST': { String s ->
            isEmpty(s) ? false : (s.split(/,/,-1).every{mkdirs(it)})
        },
        'long': { String s ->
            isEmpty(s) ? true : isLong(s)
        },
        'LONG': { String s ->
            isEmpty(s) ? false : isLong(s)
        },
        'long_list': { String s ->
            isEmpty(s) ? true : (s.split(/,/,-1).every{isLong(it)})
        },
        'LONG_LIST': { String s ->
            isEmpty(s) ? false : (s.split(/,/,-1).every{isLong(it)})
        },
        'integer': { String s ->
            isEmpty(s) ? true : isInteger(s)
        },
        'INTEGER': { String s ->
            isEmpty(s) ? false : isInteger(s)
        },
        'integer_list': { String s ->
            isEmpty(s) ? true : (s.split(/,/,-1).every{isInteger(it)})
        },
        'INTEGER_LIST': { String s ->
            isEmpty(s) ? false : (s.split(/,/,-1).every{isInteger(it)})
        },
        'EMPTY': { String s ->
            isEmpty(s) ? true : false
        },
        'NOTEMPTY': { String s ->
            isEmpty(s) ? false : true
        },
        'database': { String s ->
            isEmpty(s) ? true : isaDatabase(s)
        },
        'DATABASE': { String s ->
            isEmpty(s) ? false : isaDatabase(s)
        },
        'database_list': { String s ->
            isEmpty(s) ? true : (s.split(/,/,-1).every{isaDatabase(it)})
        },
        'DATABASE_LIST': { String s ->
            isEmpty(s) ? false : (s.split(/,/,-1).every{isaDatabase(it)})
        },
        'digits': { String s ->
            isEmpty(s) ? true : (s ==~ /\d+/ ? true : false)
        },
        'DIGITS': { String s ->
            isEmpty(s) ? false : (s ==~ /\d+/ ? true : false)
        },
        'digits_list': { String s ->
            isEmpty(s) ? true : (s.split(/,/,-1).every{it ==~ /\d+/})
        },
        'DIGITS_LIST': { String s ->
            isEmpty(s) ? false : (s.split(/,/,-1).every{it ==~ /\d+/})
        },
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

    Boolean validate (String data, String rule) {
        if (!rules.containsKey(rule)) {
            false
        }
        else {
            rules[rule].call(data)
        }
    }

    Boolean mkdirs (String s) {
        File f = new File(s)
        if (!f.exists()) {
            f.mkdirs()
        }
        f.isDirectory()
    }

    Boolean isaDatabase (String s) {
        try {
            OracleTnsnames.instance.getAlias(s)
            true
        }
        catch (e) {
            false
        }
    }

    Boolean isInteger (String s) {
        try {
            Integer.valueOf(s)
            true
        }
        catch (e) {
            false
        }
    }

    Boolean isLong (String s) {
        try {
            Long.valueOf(s)
            true
        }
        catch (e) {
            false
        }
    }

    Boolean isaHost (String s) {
        Host.isReachable(s, hostIsReachableTimeout) ? true : false
    }

    Boolean isEmpty (String s) {
        s == null || s.length() == 0
    }

    Boolean isaRule (String s) {
        rules.containsKey(s)
    }

}
