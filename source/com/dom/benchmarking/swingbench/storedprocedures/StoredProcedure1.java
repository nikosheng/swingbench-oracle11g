package com.dom.benchmarking.swingbench.storedprocedures;


import com.dom.benchmarking.swingbench.event.JdbcTaskEvent;
import com.dom.benchmarking.swingbench.kernel.DatabaseTransaction;
import com.dom.benchmarking.swingbench.kernel.SwingBenchException;
import com.dom.benchmarking.swingbench.kernel.SwingBenchTask;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.Map;

import oracle.jdbc.OracleTypes;

import oracle.sql.ARRAY;


public class StoredProcedure1 extends DatabaseTransaction {
    public StoredProcedure1() {
    }

    public void close() {
    }

    public void init(Map params) {
    }

    public void execute(Map params) throws SwingBenchException {
        Connection connection = (Connection)params.get(SwingBenchTask.JDBC_CONNECTION);
        int queryTimeOut = 60;

        if (params.get(SwingBenchTask.QUERY_TIMEOUT) != null) {
            queryTimeOut = ((Integer)(params.get(SwingBenchTask.QUERY_TIMEOUT))).intValue();
        }

        long executeStart = System.nanoTime();
        int[] dmlArray = null;

        try {
            try {
                CallableStatement cs = connection.prepareCall("{? = call swingbench.storedprocedure1(?,?)}");
                cs.registerOutParameter(1, OracleTypes.ARRAY, "INTEGER_RETURN_ARRAY");
                cs.setInt(2, (int)this.getMinSleepTime());
                cs.setInt(3, (int)this.getMaxSleepTime());
                cs.setQueryTimeout(queryTimeOut);
                cs.executeUpdate();
                dmlArray = (((ARRAY)cs.getArray(1)).getIntArray());
                cs.close();
            } catch (SQLException se) {
                throw new SwingBenchException(se.getMessage());
            }

            processTransactionEvent(new JdbcTaskEvent(this, getId(), (System.nanoTime() - executeStart), true, dmlArray));
        } catch (SwingBenchException ex) {
            processTransactionEvent(new JdbcTaskEvent(this, getId(), (System.nanoTime() - executeStart), false, dmlArray));
            throw new SwingBenchException(ex);
        } finally {
        }
    }
}
