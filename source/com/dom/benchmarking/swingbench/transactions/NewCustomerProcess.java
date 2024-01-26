package com.dom.benchmarking.swingbench.transactions;


import com.dom.benchmarking.swingbench.event.JdbcTaskEvent;
import com.dom.benchmarking.swingbench.kernel.SwingBenchException;
import com.dom.benchmarking.swingbench.kernel.SwingBenchTask;
import com.dom.benchmarking.swingbench.utilities.RandomGenerator;

import java.io.BufferedReader;
import java.io.FileReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.jdbc.OracleConnection;


public class NewCustomerProcess extends OrderEntryProcess {

    private static final Logger logger = Logger.getLogger(NewCustomerProcess.class.getName());
    private static final String NAMES_FILE = "data/names.txt";
    private static final String NLS_FILE = "data/nls.txt";
    private static ArrayList firstNames = null;
    private static ArrayList lastNames = null;
    private static ArrayList nlsInfo = null;
    private static Object lock = new Object();
    private PreparedStatement insPs = null;
    private PreparedStatement seqPs = null;

    public NewCustomerProcess() {
    }

    public void init(Map params) throws SwingBenchException {
        boolean initCompleted = false;
        
        Connection connection = (Connection)params.get(SwingBenchTask.JDBC_CONNECTION);
        try {
            this.getMaxandMinCustID(connection, params);
        } catch (SQLException se) {
            logger.log(Level.SEVERE, "Unable to get max and min customer id", se);
        }

        if ((firstNames == null) || !initCompleted) { // load any data you might need (in this case only once)

            synchronized (lock) {
                if (firstNames == null) {
                    firstNames = new ArrayList();
                    lastNames = new ArrayList();
                    nlsInfo = new ArrayList();

                    String value = (String)params.get("SOE_NAMESDATA_LOC");

                    try {
                        BufferedReader br = new BufferedReader(new FileReader((value == null) ? NAMES_FILE : value));
                        String data = null;
                        String firstName = null;
                        String lastName = null;

                        while ((data = br.readLine()) != null) {
                            StringTokenizer st = new StringTokenizer(data, ",");
                            firstName = st.nextToken();
                            lastName = st.nextToken();
                            firstNames.add(firstName);
                            lastNames.add(lastName);
                        }

                        br.close();
                        value = (String)params.get("SOE_NLSDATA_LOC");
                        br = new BufferedReader(new FileReader((value == null) ? NLS_FILE : value));
                        data = null;

                        while ((data = br.readLine()) != null) {
                            NLSSupport nls = new NLSSupport();
                            StringTokenizer st = new StringTokenizer(data, ",");
                            nls.language = st.nextToken();
                            nls.territory = st.nextToken();
                            nlsInfo.add(nls);
                        }

                        br.close();
                    } catch (java.io.FileNotFoundException fne) {
                        logger.log(Level.SEVERE, "File not found", fne);
                    } catch (java.io.IOException ioe) {
                        logger.log(Level.SEVERE, "Cant access file", ioe);
                    }
                }

                initCompleted = true;
            }
        }
    }

    public void execute(Map params) throws SwingBenchException {
        Connection connection = (Connection)params.get(SwingBenchTask.JDBC_CONNECTION);
        long custID;
        String firstName = (String)firstNames.get(RandomGenerator.randomInteger(0, firstNames.size()));
        String lastName = (String)lastNames.get(RandomGenerator.randomInteger(0, lastNames.size()));
        NLSSupport nls = (NLSSupport)nlsInfo.get(RandomGenerator.randomInteger(0, nlsInfo.size()));
        initJdbcTask();

        long executeStart = System.nanoTime();
        ResultSet rs = null;
        try {
            try {
                seqPs = connection.prepareStatement("select customer_seq.nextval from dual");

                rs = seqPs.executeQuery();
                rs.next();
                custID = rs.getInt(1);

                addSelectStatements(1);
                thinkSleep();

                try {
                    insPs =
connection.prepareStatement("insert into customers (customer_id ,cust_first_name ,cust_last_name ,nls_language ,nls_territory ,credit_limit ,cust_email ,account_mgr_id ) " +
                            "values (? , ? , ? , ? , ? , ? ,? , ?) ");
                    insPs.setLong(1, custID);
                    insPs.setString(2, firstName);
                    insPs.setString(3, lastName);
                    insPs.setString(4, nls.language);
                    insPs.setString(5, nls.territory);
                    insPs.setInt(6, RandomGenerator.randomInteger(MIN_CREDITLIMIT, MAX_CREDITLIMIT));
                    insPs.setString(7, firstName + "." + lastName + "@" + "oracle.com");
                    insPs.setInt(8, RandomGenerator.randomInteger(MIN_SALESID, MAX_SALESID));
                    insPs.execute();

                } catch (SQLException se) {
                    throw new SwingBenchException(se);
                }
                addInsertStatements(1);
                connection.commit();
                addCommitStatements(1);
                thinkSleep();
                logon(connection, custID);
                addInsertStatements(1);
                addCommitStatements(1);
                getCustomerDetails(connection, custID);
                addSelectStatements(1);
            } catch (SQLException se) {
                throw new SwingBenchException(se.getMessage());
            } finally {
                try {
                    rs.close();
                    seqPs.close();
                    insPs.close();
                } catch (Exception ex) {
                }

            }

            processTransactionEvent(new JdbcTaskEvent(this, getId(), (System.nanoTime() - executeStart), true, getInfoArray()));
        } catch (SwingBenchException sbe) {
            processTransactionEvent(new JdbcTaskEvent(this, getId(), (System.nanoTime() - executeStart), false, getInfoArray()));

            try {
                connection.rollback();
            } catch (SQLException er) {
                return;
            }

            throw new SwingBenchException(sbe);
        }
    }

    public void close() {
    }

    public void populate(int numToPopulate, Connection connection) throws SQLException { // used for initial population
        ((OracleConnection)connection).setDefaultExecuteBatch(100);

        long custID;
        String firstName = (String)firstNames.get(RandomGenerator.randomInteger(0, firstNames.size()));
        String lastName = (String)lastNames.get(RandomGenerator.randomInteger(0, lastNames.size()));
        NLSSupport nls = (NLSSupport)nlsInfo.get(RandomGenerator.randomInteger(0, nlsInfo.size()));
        seqPs = connection.prepareStatement("select customer_seq.nextval from dual");
        insPs =
connection.prepareStatement("insert into customers (customer_id ,cust_first_name ,cust_last_name ,nls_language ,nls_territory ,credit_limit ,cust_email ,account_mgr_id ) " + "values (? , ? , ? , ? , ? , ? ,? , ?) ");

        ResultSet rs = seqPs.executeQuery();
        rs.next();
        custID = rs.getLong(1);

        for (int i = 0; i < numToPopulate; i++) {
            firstName = (String)firstNames.get(RandomGenerator.randomInteger(0, firstNames.size()));
            lastName = (String)lastNames.get(RandomGenerator.randomInteger(0, lastNames.size()));
            nls = (NLSSupport)nlsInfo.get(RandomGenerator.randomInteger(0, nlsInfo.size()));

            rs.close();
            insPs.setLong(1, custID++);
            insPs.setString(2, firstName);
            insPs.setString(3, lastName);
            insPs.setString(4, nls.language);
            insPs.setString(5, nls.territory);
            insPs.setInt(6, RandomGenerator.randomInteger(MIN_CREDITLIMIT, MAX_CREDITLIMIT));
            insPs.setString(7, firstName + "." + lastName + "@" + "oracle.com");
            insPs.setInt(8, RandomGenerator.randomInteger(MIN_SALESID, MAX_SALESID));
            insPs.execute();

            if ((i % 10000) == 0) {
                connection.commit();
            }
        }
        Statement st = connection.createStatement();
        st.execute("alter sequence customer_seq increment by " + numToPopulate);
        rs = seqPs.executeQuery();
        st.execute("alter sequence customer_seq increment by 1");

        connection.commit();

        ((OracleConnection)connection).setDefaultExecuteBatch(1);

        seqPs.close();
        insPs.close();
    }

    private class NLSSupport {

        String language = null;
        String territory = null;

    }

}
