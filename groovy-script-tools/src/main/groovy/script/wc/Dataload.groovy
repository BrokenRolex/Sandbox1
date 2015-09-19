package script.wc

import groovy.xml.*
import script.*
import script.sql.*
import groovy.io.FileType

/**
 * IBM WCS Dataload uses java.util.logging
 * see this file for changing logging properties
 * /opt/apps/WAS/CommerceServer70/wc.ear/xml/config/dataload/logging.properties 
 */
@Singleton
@groovy.util.logging.Log4j
class Dataload {

    static final String IBM_PKG = 'com.ibm.commerce.foundation.dataload'
    static final String HDS_PKG = 'com.hdsupply.commerce.foundation.dataload'
    static final String ID_RESOLVER_CLASSNAME = "${IBM_PKG}.idresolve.IDResolverImpl"
    static final String JDBC_DATA_WRITER_CLASSNAME = "${IBM_PKG}.datawriter.JDBCDataWriter"
    static final String BUSINESS_OBJECT_LOADER_CLASSNAME = "${IBM_PKG}.BusinessObjectLoader"
    static final String XML_READER_CLASSNAME = "${IBM_PKG}.datareader.XmlReader"
    static final String CUSTOM_MASSLOAD_XML_HANDLER_CLASSNAME = "${HDS_PKG}.xmlhandler.CustomMassLoadXmlHandler"
    static final String PASS_THROUGH_BUSINESS_OBJECT_BUILDER_CLASSNAME = "${IBM_PKG}.businessobjectbuilder.PassThroughBusinessObjectBuilder"
    static final String MASSLOAD_XML_MEDIATOR_CLASSNAME = "${HDS_PKG}.businessobjectmediator.MassLoadXmlMediator"
    static final String XSI = 'http://www.w3.org/2001/XMLSchema-instance'
    static final File COMMERCE_DIR = new File('/opt/apps/WAS/CommerceServer70')
    static final String COMMERCE_DIR_KEY = 'wc-dataload.commerce.dir'
    static final String DATALOAD_SCRIPT_FILE_KEY = 'wc-dataload.script.file'
    static final String DATALOAD_LOG_DIR_KEY = 'wc-dataload.log.dir'
    static final String DATALOAD_DBNAME_KEY = 'wc-dataload.dbname'
    static final String DATALOAD_DBUSER_KEY = 'wc-dataload.dbuser'
    static final String DATALOAD_ARCHIVE_DIR_KEY = 'wc-dataload.archive.dir'
    static final String CONFIG_NAMESPACE = "http://www.ibm.com/xmlns/prod/commerce/foundation/config"
    static final String SCHEMA_LOCATION = "$CONFIG_NAMESPACE ../../../../xml/config/xsd/wc-dataload.xsd"

    Lock lock
    String lockname = '.dataload.lock'
    String dbuser
    String dbname
    int cacheSize = 1
    int commitCount = 2000
    int batchSize = 1000
    int maxError = 0 // 0 means infinite errors
    File envFile = new File(Env.scriptFile.parentFile, 'wc-dataload-environment.xml')
    File loaderFile = new File(Env.scriptFile.parentFile, 'wc-dataload-loader.xml')
    File dataloadScriptFile
    File dataloadLogDir
    File archiveDir
    File commerceDir
    boolean cleanup = true
    List dataloadProperties = []
    // Table Column exclusions: default inassigned keys with the empty set
    // key = table name , value = set of column names
    Map<String, Set> tableColumnExclusions = [:].withDefault { [] as Set }

    Map database = [
        name: null,
        user: null,
        password: null,
        port: null,
        schema: null,
        server: null,
        type: 'Oracle',
        driverType: 'thin',
    ]

    Map businessContext = [
        storeIdentifier: 'ThdsMroUs',
        catalogIdentifier: 'ThdsMroUs',
        languageId: '-1',
        currency: 'USD',
    ]

    Map businessObjectProperties = [
        timestampPattern: 'yyyy-MM-dd HH:mm:ss.S',
    ]

    File writeEnvironment () {
        validateFile(envFile)
        envFile.delete()
        def builder = new StreamingMarkupBuilder()
        def writer = builder.bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace('xsi' : XSI)
            mkp.declareNamespace('_config': CONFIG_NAMESPACE)
            _config.DataLoadEnvConfiguration('xsi:schemaLocation': SCHEMA_LOCATION) {
                _config.BusinessContext(businessContext)
                _config.Database(database)
                _config.IDResolver(className: ID_RESOLVER_CLASSNAME, cacheSize: cacheSize)
                _config.DataWriter(className: JDBC_DATA_WRITER_CLASSNAME)
            }
        }
        log.debug "writing [$envFile]"
        envFile.withWriter('UTF-8') { it << XmlUtil.serialize(writer) }
        return envFile
    }

    void deleteLoader () {
        loaderFile.delete()
    }

    File writeLoader () {
        validateFile(loaderFile)
        if (!loaderFile.exists()) {
            def builder = new StreamingMarkupBuilder()
            def writer = builder.bind {
                mkp.xmlDeclaration()
                mkp.declareNamespace('xsi': XSI)
                mkp.declareNamespace('_config': CONFIG_NAMESPACE)
                _config.DataloadBusinessObjectConfiguration('xsi:schemaLocation': SCHEMA_LOCATION) {
                    _config.DataLoader(className: BUSINESS_OBJECT_LOADER_CLASSNAME) {
                        if (tableColumnExclusions.size() > 0) {
                            _config.ColumnExclusionList() {
                                tableColumnExclusions.each { String tableName, Set columnNames ->
                                    _config.table(name: tableName, columns: columnNames.join(','))
                                }
                            }
                        }
                        _config.DataReader(className: XML_READER_CLASSNAME) {
                            _config.XmlHandler(className: CUSTOM_MASSLOAD_XML_HANDLER_CLASSNAME)
                        }
                        _config.BusinessObjectBuilder(className: PASS_THROUGH_BUSINESS_OBJECT_BUILDER_CLASSNAME) {
                            _config.BusinessObjectMediator(className: MASSLOAD_XML_MEDIATOR_CLASSNAME) {
                                businessObjectProperties.each  { name, value ->
                                    _config.property(name: name, value: value)
                                }
                            }
                        }
                    }
                }
            }
            log.debug "writing [$loaderFile]"
            loaderFile.withWriter('UTF-8') { it << XmlUtil.serialize(writer) }
        }
        return loaderFile
    }

    File writeConfigFor (File file, String dataLoadMode) {
        validateFile(file)
        if (file.parentFile == null) {
            file = new File('.', file.name).canonicalFile
        }
        String configName = file.name.replaceFirst(/\.xml\z/, '')
        File configFile = new File(file.parentFile, "wc-dataload-${configName}.xml")
        log.debug "writing [$configFile]"
        def loadOrder = [
            commitCount: commitCount,
            batchSize: batchSize,
            maxError: maxError,
            dataLoadMode: dataLoadMode,
        ]
        def loadItem = [
            businessObjectConfigFile: loaderFile.path,
            name: file.name,
        ]
        def builder = new StreamingMarkupBuilder()
        def writer = builder.bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace('xsi': XSI)
            mkp.declareNamespace('_config': CONFIG_NAMESPACE)
            _config.DataLoadConfiguration('xsi:schemaLocation': SCHEMA_LOCATION) {
                _config.DataLoadEnvironment(configFile: envFile.path)
                _config.LoadOrder(loadOrder) {
                    _config.LoadItem(loadItem) {
                        _config.DataSourceLocation(location: file.path)
                    }
                }
            }
        }
        configFile.withWriter('UTF-8') { it << XmlUtil.serialize(writer) }
        return configFile
    }

    void begin () {
        findDataloadScriptFile()
        findDataloadLogDir()
        findArchiveDir()
        configureDatabase()
        writeEnvironment()
        writeLoader()
        acquireLock()
    }

    void findArchiveDir () {
        if (archiveDir == null) {
            if (Props.instance.containsKey(DATALOAD_ARCHIVE_DIR_KEY)) {
                archiveDir = Props.instance.getFileProp(DATALOAD_ARCHIVE_DIR_KEY)
            }
        }
        if (!archiveDir) {
            log.warn "archive directory not set"
        }
    }

    void findDataloadScriptFile () {
        if (dataloadScriptFile == null) {
            if (Props.instance.containsKey(DATALOAD_SCRIPT_FILE_KEY)) {
                dataloadScriptFile = Props.instance.getFileProp(DATALOAD_SCRIPT_FILE_KEY)
            }
            else {
                dataloadScriptFile = new File(new File(getCommerceDir(),'bin'), 'dataload.sh')
            }
        }
        if (dataloadScriptFile == null || !dataloadScriptFile.isFile()) {
            throw new Exception("cannot find dataload script [$dataloadScriptFile]")
        }
        log.debug "dataload script [$dataloadScriptFile]"
    }

    void findDataloadLogDir () {
        if (dataloadLogDir == null) {
            if (Props.instance.containsKey(DATALOAD_LOG_DIR_KEY)) {
                dataloadLogDir = Props.instance.getFileProp(DATALOAD_LOG_DIR_KEY)
            }
            else {
                dataloadLogDir = new File(getCommerceDir(), 'logs')
            }
        }
        if (dataloadLogDir == null || !dataloadLogDir.isDirectory()) {
            throw new Exception("cannot find dataload log dir [$dataloadLogDir]")
        }
        log.debug "dataload log dir [$dataloadLogDir]"
    }

    private File getCommerceDir () {
        if (commerceDir == null) {
            if (Props.instance.containsKey(COMMERCE_DIR_KEY)) {
                commerceDir = Props.instance.getFileProp(COMMERCE_DIR_KEY)
            }
            else {
                log.warn "using default commerce dir [$COMMERCE_DIR]"
                commerceDir = COMMERCE_DIR
            }
        }
        return commerceDir
    }

    private void findDbname(String dbn) {
        if (dbname == null) {
            dbname = dbn
            if (dbname == null) {
                if (Props.instance.containsKey(DATALOAD_DBNAME_KEY)) {
                    dbname = Props.instance.getProp(DATALOAD_DBNAME_KEY)
                }
                if (dbname == null) {
                    dbname = OracleAccounts.instance.getDefaultAlias()
                }
                if (dbname == null) {
                    throw new Exception("cannot find database name [$dbname]")
                }
            }
        }
    }

    private void findDbuser(String dbu = null) {
        if (dbuser == null) {
            dbuser = dbu
            if (dbuser == null) {
                if (Props.instance.containsKey(DATALOAD_DBUSER_KEY)) {
                    dbuser = Props.instance.getProp(DATALOAD_DBUSER_KEY)
                }
                if (dbuser == null) {
                    dbuser = OracleAccounts.instance.getDefaultUser(dbname)
                }
                if (dbuser == null) {
                    throw new Exception("cannot find database user [$dbuser]")
                }
            }
        }
    }

    private void checkMapForKeys (Map map, List keys, String msg) {
        if (map == null) {
            throw new Exception("$msg map is null")
        }
        if (keys == null) {
            throw new Exception("$msg no keys specified")
        }
        keys.each { String key ->
            if (key == null) {
                return
            }
            if (!map.containsKey(key)) {
                throw new Exception("$msg key [$key] is missing : $map")
            }
            String value = map[key]
            if (value == null || value.length() == 0) {
                throw new Exception("$msg key [$key] value [$value] is missing : $map")
            }
        }
    }

    Map findOracleAccount() {
        log.debug "getting Oracle account map for database [$dbname] and user [$dbuser]"
        def map = OracleAccounts.instance.getAccount(dbuser, dbname)
        checkMapForKeys(map, ['encpwd'], "Oracle tnsname map check failed,")
        map
    }

    Map findOracleTnsname() {
        log.debug "getting Oracle tnsname map for database [$dbname]"
        def map = OracleTnsnames.instance.getAlias(dbname)
        checkMapForKeys(map, ['host', 'port'], "Oracle account map check failed, ")
        map
    }

    void configureDatabase (String dbn = null, String dbu = null) {
        findDbname(dbn)
        findDbuser(dbu)
        database.name = dbname
        database.user = dbuser
        database.schema = dbuser
        def acct = findOracleAccount()
        database.password = acct.encpwd
        def tns = findOracleTnsname()
        database.server = tns.host
        database.port = tns.port
        log.info "database $database"
    }

    private void validateFile (File file) {
        if (file == null) {
            throw new Exception("file is null")
        }
        if (!file.name.endsWith(".xml")) {
            throw new Exception("file [$file] is not an [*.xml] file")
        }
    }

    private int execute (List cmd) {
        log.debug "executing [${cmd.join(' ')}]"
        int exitValue = 1
        try {
            Process proc = cmd.execute()
            proc.consumeProcessOutput()
            proc.waitFor()
            exitValue = proc.exitValue()
            if (exitValue != 0) {
                log.error "execution of [${cmd.join(' ')}] return code = $exitValue"
            }
        }
        catch (e) {
            log.error e.message
        }
        exitValue
    }

    void load (File file, Boolean isDelete = false) {
        validateFile(file)
        String dataLoadMode = isDelete ? 'Delete' : 'Replace'
        File configFile = writeConfigFor(file, dataLoadMode)
        log.info "loading [$file] as [$dataLoadMode] size [${file.size()}]"
        List cmd = [dataloadScriptFile.path, configFile.path]
        if (dataloadProperties.size() > 0) {
            dataloadProperties.each {
                if (it) {
                    cmd << it
                }
            }
        }
        int exitValue = execute(cmd)
        archive_wc_dataload_log() // Zaheer wanted this
        if (exitValue != 0) {
            archive(file)
            archive(configFile)
            look4errorlog(file)
        }
        if (cleanup) {
            file.delete()
            configFile.delete()
        }
    }

    void archive_wc_dataload_log () {
        String log_name = 'wc-dataload.log'
        if (archiveDir?.isDirectory() && dataloadLogDir?.isDirectory()) {
            Boolean log_not_found = true
            dataloadLogDir.eachFile(FileType.FILES) { File file ->
                if (file.name.startsWith(log_name)) {
                    String newName = file.name + '.' + DateString.date_yyyyMMddHHmmssSSS()
                    File archiveFile = new File(archiveDir, newName)
                    try {
                        log_not_found = false
                        FileUtils.move(file, archiveFile)
                        log.info "archived [${file}] to [${archiveDir}] as [${newName}]"
                    }
                    catch (e) {
                        log.warn "problem occurred attempting to archive [$file] as [$archiveFile] - ${e.message}"
                    }
                }
            }
            if (log_not_found) {
                log.info "no files like [$log_name] found in [$dataloadLogDir]"
            }
        }
        else {
            log.info "archive dir [$archiveDir] is either not set or does not exist, cannot save [$log_name] files"
        }
    }


    void look4errorlog (File loadFile) {
        def errorPattern = ~/\A${loadFile.name}_ERROR_\d+\.\d+\.\d+_\d+\.\d+\.\d+\.\d+\.log\z/
        File errorlog
        dataloadLogDir.eachFileMatch(FileType.FILES, errorPattern) { errorlog = it }
        if (errorlog) {
            if (archiveDir) {
                FileUtils.copy(errorlog, archiveDir)
                errorlog.delete()
                log.error "found error log [$errorlog], moved to [$archiveDir]"
            }
            else {
                log.error "found error log [$errorlog]"
            }
        }
    }

    void archive (File file) {
        if (file == null) {
            log.warn "cannot archive a null file object"
            return
        }
        if (archiveDir == null) {
            log.warn "cannot archive [$file] because the archive directory is not set"
            return
        }
        if (!file.isFile()) {
            log.warn "cannot archive [$file] because it does not exist"
            return
        }
        if (!archiveDir.isDirectory()) {
            log.warn "cannot archive [$file] because the archive directory [$archiveDir] does not exist"
            return
        }
        FileUtils.copy(file, archiveDir)
        log.debug "archived [$file] to [$archiveDir]"
    }

    void acquireLock (int wait = 3600, int sleep = 60) {
        if (lock == null && lockname != null) {
            File lockFile = new File(Env.getHomeDir(), lockname)
            lock = new Lock().waitAcquire(lockFile, wait, sleep)
        }
    }

    void releaseLock() {
        lock?.release()
        lock = null
    }

    void end () {
        releaseLock()
    }

    void addProperty (String key, String value) {
        if (key != null) {
            if (value == null) {
                dataloadProperties << "-D${key}" as String
            }
            else {
                dataloadProperties << "-D${key}=${value}" as String
            }
        }
    }

    void addTableColumnExclusion (String tableName, String columnName) {
        tableName = tableName.trim().toUpperCase()
        if (tableName && columnName) {
            // split columns on commma, trim then only add if there is a column name (the empty string is false in Groovy)
            tableColumnExclusions[tableName].addAll(columnName.split(/,/).collect{it.trim()}.findAll{it})
        }
    }

    void addTableColumnExclusionsFromFile (File file) {
        if (file.isFile()) {
            file.eachLine { String line ->
                if (line =~ /=/ && !(line =~ /\A#/)) {
                    def (tableName, columnName) = line.split(/=/)
                    if (tableName && columnName) {
                        addTableColumnExclusion(tableName, columnName)
                    }
                }
            }
        }
    }

}
