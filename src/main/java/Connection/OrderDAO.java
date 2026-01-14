package Connection;

import EmployeeView.Order;
import Product.Product;

import java.sql.*;
import java.util.ArrayList;

public class OrderDAO {
    public int createOrderWithItems(Connection con, int employeeId, int customerId, ArrayList<Product> items) throws SQLException {
        if (items == null || items.isEmpty()) return -1;

        String insertOrderSql = "INSERT INTO orders(employee_id, customer_id, total_amount) VALUES (?, ?, ?)";
        String insertItemSql  = "INSERT INTO order_items(order_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";

        double total = 0;
        for (Product p : items) total += p.getTotalPrice();

        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try (
                PreparedStatement psOrder = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement psItem = con.prepareStatement(insertItemSql)
        ) {

            psOrder.setInt(1, employeeId);
            psOrder.setInt(2, customerId);
            psOrder.setDouble(3, total);
            psOrder.executeUpdate();

            int newOrderId;
            try (ResultSet rs = psOrder.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Creating order failed, no ID returned.");
                newOrderId = rs.getInt(1);
            }


            for (Product p : items) {
                psItem.setInt(1, newOrderId);
                psItem.setInt(2, p.getId());
                psItem.setInt(3, p.getQuantity());
                psItem.setDouble(4, p.getPricePerPiece());
                psItem.setDouble(5, p.getTotalPrice());
                psItem.addBatch();
            }
            psItem.executeBatch();

            con.commit();
            return newOrderId;

        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }


    public ArrayList<Order> getAllOrders(Connection con) throws SQLException {
        ArrayList<Order> list = new ArrayList<>();
        String sql = "SELECT id, employee_id, customer_id, order_date, total_amount FROM orders ORDER BY id DESC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("employee_id"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("order_date"),
                        rs.getDouble("total_amount")
                ));
            }
        }
        return list;
    }


    public ArrayList<Order> getOrdersByCustomerId(Connection con, int customerId) throws SQLException {
        ArrayList<Order> list = new ArrayList<>();
        String sql = "SELECT id, employee_id, customer_id, order_date, total_amount " +
                "FROM orders WHERE customer_id = ? ORDER BY id DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Order(
                            rs.getInt("id"),
                            rs.getInt("employee_id"),
                            rs.getInt("customer_id"),
                            rs.getTimestamp("order_date"),
                            rs.getDouble("total_amount")
                    ));
                }
            }
        }
        return list;
    }
}
