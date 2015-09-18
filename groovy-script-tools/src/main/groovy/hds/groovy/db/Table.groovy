package hds.groovy.db

import java.sql.Connection
import java.sql.DatabaseMetaData;
import java.sql.ResultSet

import groovy.sql.*

class Table {
    String tableName
    Map pk = [:]
    String pkWhere
    Map cols
    List colNames
    List pkColNames
    String insert
    String insert_map
    String update
    //String update_map
    String delete
    //String delete_map
    String select
    //String select_map
    Sql sql
    DatabaseMetaData dmd
    
    Table (Sql sql, String tableName) {
        this.sql = sql
        this.tableName = tableName
        dmd = OracleUtils.databaseMetaData(sql)
        insert = "insert into ${tableName} "
        insert_map = insert
        update = "update ${tableName} "
        //update_map = update
        delete = "delete ${tableName} "
        //delete_map = delete
        select = 'select '
        //select_map = select
        buildPkCols()
        buildCols()
        colNames = cols.values().sort{a, b -> a.SEQ <=> b.SEQ}.collect{it.NM}
        pkColNames = pk.values().sort{a, b -> a.SEQ <=> b.SEQ}.collect{it.NM}
        insert_map += '(' + colNames.join(', ') + ') values (' + colNames.collect{ ":$it" }.join(', ') + ')'
        insert += '(' + colNames.join(', ') + ') values (' + colNames.collect{'?'}.join(', ') + ')'
        select += colNames.join(', ') + " from ${tableName}"
        update += 'set ' + cols.values().findAll{!it.PK}.collect{it.NM + ' = ?'}.join(', ')
        pkWhere = ' where ' + pkColNames.collect{it + ' = ?'}.join(' and ')
        update += pkWhere
        delete += pkWhere
        select += pkWhere
    }

    void buildCols () {
        cols = [:]
        ResultSet rs = dmd.getColumns(null, null, tableName.toUpperCase(), null)
        while (rs.next()) {
            String nm = rs.getString('COLUMN_NAME')
            cols[nm] = [
                NM: nm,
                SEQ: rs.getInt('ORDINAL_POSITION'),
                NULL: rs.getString('IS_NULLABLE') == 'YES' ? true : false,
                PK: pk.containsKey(nm),
            ]
        }
    }
    
    void buildPkCols () {
        pk = [:]
        ResultSet rs = dmd.getPrimaryKeys(null, null, tableName.toUpperCase())
        while (rs.next()) {
            String nm = rs.getString('COLUMN_NAME')
            pk[nm] = [
                NM: nm,
                SEQ: rs.getInt('KEY_SEQ'),
            ]
        }
    }

}
