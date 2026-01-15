// ========================= OrderDAO.java =========================
package Connection;

import DataStructure.LinkedList;
import EmployeeView.Order;
import Product.Product;

import java.sql.*;
import java.util.ArrayList;

public class OrderDAO {

    // DTO for printing (stored inside your LinkedList)
    public static class OrderDetails {
        private final int productId;
        private final String productName;
        private final String categoryName;
        private final int quantity;
        private final double unitPrice;
        private final double lineTotal;

        public OrderDetails(int productId, String productName, String categoryName,
                            int quantity, double unitPrice, double lineTotal) {
            this.productId = productId;
            this.productName = productName;
            this.categoryName = categoryName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategoryName() { return categoryName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getLineTotal() { return lineTotal; }
    }

    public int createOrderWithItems(Connection con, int employeeId, int customerId, ArrayList<Product> items) throws SQLException {
        if (items == null || items.isEmpty()) return -1;

        String insertOrderSql = "INSERT INTO orders(employee_id, customer_id, total_amount) VALUES (?, ?, ?)";
        String insertItemSql  = "INSERT INTO order_items(order_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";

        double total = 0;
        for (Product p : items) total += p.getTotalPrice();

        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try (PreparedStatement psOrder = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psItem  = con.prepareStatement(insertItemSql)) {

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

    /**
     * Returns order items for a given order_id.
     * Make sure your schema matches these names:
     * - order_items(order_id, product_id, quantity, unit_price, line_total, id)
     * - products(id, name, category_id)
     * - categories(id, name)
     */
    public LinkedList getOrderDetails(Connection con, int orderId) throws SQLException {
        LinkedList list = new LinkedList();

        // Try to find the "product name" column in your products table
        String productNameCol = findFirstExistingColumn(con, "products",
                "name", "product_name", "productName", "pname", "title", "productTitle", "item_name");

        if (productNameCol == null) {
            throw new SQLException("Could not find a product name column in table 'products'. " +
                    "Expected one of: name, product_name, productName, pname, title, item_name");
        }

        // Try to find category relationship (optional)
        String categoryIdCol = findFirstExistingColumn(con, "products", "category_id", "categoryId", "cat_id");
        String categoryNameCol = findFirstExistingColumn(con, "categories", "name", "category_name", "categoryName", "title");

        boolean hasCategories = (categoryIdCol != null && categoryNameCol != null);

        String sql;

        if (hasCategories) {
            sql =
                    "SELECT " +
                            "  oi.product_id, " +
                            "  p." + productNameCol + " AS product_name, " +
                            "  COALESCE(c." + categoryNameCol + ", '') AS category_name, " +
                            "  oi.quantity, " +
                            "  oi.unit_price, " +
                            "  oi.line_total " +
                            "FROM order_items oi " +
                            "JOIN products p ON p.id = oi.product_id " +
                            "LEFT JOIN categories c ON c.id = p." + categoryIdCol + " " +
                            "WHERE oi.order_id = ? " +
                            "ORDER BY oi.id ASC";
        } else {
            // No categories table/relationship: still return items
            sql =
                    "SELECT " +
                            "  oi.product_id, " +
                            "  p." + productNameCol + " AS product_name, " +
                            "  '' AS category_name, " +
                            "  oi.quantity, " +
                            "  oi.unit_price, " +
                            "  oi.line_total " +
                            "FROM order_items oi " +
                            "JOIN products p ON p.id = oi.product_id " +
                            "WHERE oi.order_id = ? " +
                            "ORDER BY oi.id ASC";
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetails od = new OrderDetails(
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("category_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("line_total")
                    );
                    list.addLast(od); // <-- matches your LinkedList
                }
            }
        }

        return list;
    }

    /**
     * Finds the first existing column in tableName from a list of candidate column names.
     */
    private String findFirstExistingColumn(Connection con, String tableName, String... candidates) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();

        // Load all column names in the table into a set-like check
        java.util.HashSet<String> cols = new java.util.HashSet<>();

        try (ResultSet rs = meta.getColumns(con.getCatalog(), null, tableName, null)) {
            while (rs.next()) {
                cols.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }

        for (String c : candidates) {
            if (cols.contains(c.toLowerCase())) return c;
        }
        return null;
    }

}
