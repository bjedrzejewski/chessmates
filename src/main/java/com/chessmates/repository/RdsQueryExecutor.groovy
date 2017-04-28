package com.chessmates.repository

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.postgresql.util.PGobject
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

import java.sql.DriverManager
import java.sql.ResultSet

/**
 * class that executes insert/select queries PostGreSql DB hosted on AWS
 */

@Component
class RdsQueryExecutor implements QueryExecutor {

    @Value('${chessmates.rds.dbName}')
    private String dbName

    @Value('${chessmates.rds.userName}')
    private String userName

    @Value('${chessmates.rds.password}')
    private String password

    @Value('${chessmates.rds.hostName}')
    private String hostName

    @Value('${chessmates.rds.port}')
    private String port

    @Override
    void executeInsert(givenTableName, givenItems) {
        def injectJson = {
            paramIndex, jsonString, stmt ->
                def jsonObject = new PGobject()
                jsonObject.setType("jsonb")
                jsonObject.setValue(jsonString)
                stmt.setObject(paramIndex, jsonObject)
        }

        def prepareAndInvokeInsertStatement = {
            conn ->
                def valueString = ', (?)'.multiply(givenItems.size()).toString().substring(1)
                def stmt = conn.prepareStatement("INSERT INTO public.${givenTableName}(data) VALUES ${valueString};")
                givenItems.eachWithIndex { x, i -> injectJson ((i + 1), JsonOutput.toJson(x).toString(), stmt)}
                def itemsInserted = stmt.executeUpdate()
                [stmt, itemsInserted]
        }

        if(givenItems.size() <= 0) return

        executeQuery(prepareAndInvokeInsertStatement)
    }

    @Override
    List executeSelect(givenTableName, givenId = null) {
        def prepareAndInvokeSelectStatement = {
            conn ->
                def stmt = conn.prepareStatement("SELECT data FROM ${givenTableName}" + (givenId ? " WHERE data->>'id' = '${givenId}'" : ""));
                def rs = stmt.executeQuery();
                [stmt, rs]
        }

        def resultSet = executeQuery(prepareAndInvokeSelectStatement)
        resultSet
    }

    private def executeQuery(handleEmbeddedQueryUsing){
        def conn = getConnection()

        def queryStatement
        def resultSet
        (queryStatement,resultSet) = handleEmbeddedQueryUsing(conn)
        def returnValue = buildOutputObject(resultSet)
        queryStatement.close()

        conn.close()

        returnValue
    }

    private def getConnection(){
        Class.forName("org.postgresql.Driver")
        def jdbcUrl = "jdbc:postgresql://" + hostName + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password
        def conn = DriverManager.getConnection(jdbcUrl)
        return conn
    }

    private def buildOutputObject(item){
        //lambda for creating a list of objects from sql result set
        def populateOutputObject = {
            resultObjects ->
                def outputObject = []
                while (resultObjects.next() ) {
                    outputObject.add(new JsonSlurper().parseText(resultObjects.getString("data")))
                }
                resultObjects.close()

                outputObject
        }


        //we only populate if it was a select query in which case it returns a result set
        if(!(item instanceof ResultSet)) return

        populateOutputObject(item)
    }
}
