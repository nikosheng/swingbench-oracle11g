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


public class ProcessOrders extends OrderEntryProcess {

    private PreparedStatement orderPs3 = null;
    private PreparedStatement updoPs = null;
    private int orderID;

    public ProcessOrders() {
    }

    public void close() {
    }

    public void init(Map parameters) {
    }

    public void execute(Map params) throws SwingBenchException {
        Connection connection = (Connection)params.get(SwingBenchTask.JDBC_CONNECTION);
        initJdbcTask();
        ResultSet rs = null;

        long executeStart = System.nanoTime();

        try {
            try {
                orderPs3 =
                        connection.prepareStatement("SELECT /*+  first_rows index(customers, customers_pk) index(orders, order_status_ix) */  o.order_id, line_item_id, product_id, unit_price, quantity, order_mode, order_status, order_total, sales_rep_id, promotion_id, c.customer_id, cust_first_name, cust_last_name, credit_limit, cust_email, order_date FROM orders o , order_items oi, customers c WHERE o.order_id = oi.order_id  and o.customer_id = c.customer_id  and o.order_status <= 4");

                rs = orderPs3.executeQuery();
                rs.next();
                orderID = rs.getInt(1);
                addSelectStatements(1);
                thinkSleep(); //update the order
                updoPs = connection.prepareStatement("update /*+ index(orders, order_pk) */ " + "orders " + "set order_status = ? " + "where order_id = ?");
                updoPs.setInt(1, RandomGenerator.randomInteger(AWAITING_PROCESSING + 1, ORDER_PROCESSED));
                updoPs.setInt(2, orderID);
                updoPs.execute();
                addUpdateStatements(1);
                connection.commit();
                addCommitStatements(1);
            } catch (SQLException se) {
                throw new SwingBenchException(se.getMessage());
            } finally {
                hardClose(rs);
                hardClose(orderPs3);
                hardClose(updoPs);
            }

            processTransactionEvent(new JdbcTaskEvent(this, getId(), (System.nanoTime() - executeStart), true, getInfoArray()));
        } catch (SwingBenchException sbe) {
            processTransactionEvent(new JdbcTaskEvent(this, getId(), (System.nanoTime() - executeStart), false, getInfoArray()));

            try {
                connection.rollback();
            } catch (SQLException er) {
            }

            throw new SwingBenchException(sbe.getMessage());
        }
    }

}
