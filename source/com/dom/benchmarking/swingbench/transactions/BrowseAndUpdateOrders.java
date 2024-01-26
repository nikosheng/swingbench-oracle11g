package com.dom.benchmarking.swingbench.transactions;


import com.dom.benchmarking.swingbench.event.JdbcTaskEvent;
import com.dom.benchmarking.swingbench.kernel.SwingBenchException;
import com.dom.benchmarking.swingbench.kernel.SwingBenchTask;
import com.dom.benchmarking.swingbench.utilities.RandomGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BrowseAndUpdateOrders extends OrderEntryProcess {
    private static final Logger logger = Logger.getLogger(BrowseAndUpdateOrders.class.getName());
    private PreparedStatement ordPs = null;

    public BrowseAndUpdateOrders() {
    }

    public void init(Map params) {
        Connection connection = (Connection)params.get(SwingBenchTask.JDBC_CONNECTION);
        try {
            this.getMaxandMinCustID(connection, params);
        } catch (SQLException se) {
            logger.log(Level.SEVERE, "Unable to get max and min customer id", se);
        }
    }

    public void execute(Map params) throws SwingBenchException {
        Connection connection = (Connection)params.get(SwingBenchTask.JDBC_CONNECTION);
        long custID = RandomGenerator.randomLong(MIN_CUSTID, MAX_CUSTID);
        ResultSet rs = null;
        initJdbcTask();

        long executeStart = System.nanoTime();

        try {
            try {
                logon(connection, custID);
                addInsertStatements(1);
                addCommitStatements(1);
                getCustomerDetails(connection, custID);
                addSelectStatements(1);
                thinkSleep();
                ordPs =
connection.prepareStatement(" SELECT /*+ use_nl */ o.order_id, line_item_id, product_id, unit_price, quantity, order_mode, order_status, order_total, sales_rep_id, promotion_id, c.customer_id, cust_first_name, cust_last_name, credit_limit, cust_email   FROM orders o , order_items oi, customers c  WHERE o.order_id = oi.order_id  and o.customer_id = c.customer_id  and c.customer_id = ?");
                ordPs.setLong(1, custID);
                rs = ordPs.executeQuery();
                rs.next();
                addSelectStatements(1);
            } catch (SQLException se) {
                throw new SwingBenchException(se.getMessage());
            } finally {
                try {
                    rs.close();
                    ordPs.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                }

            }

            processTransactionEvent(new JdbcTaskEvent(this, getId(), (System.nanoTime() - executeStart), true, getInfoArray()));
        } catch (SwingBenchException sbe) {
            processTransactionEvent(new JdbcTaskEvent(this, getId(), (System.nanoTime() - executeStart), false, getInfoArray()));
            throw new SwingBenchException(sbe.getMessage());
        }
    }

    public void close() {
    }
}
