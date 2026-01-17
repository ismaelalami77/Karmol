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

    public int createOrderWithItems(Connection con, int employeeId, int customerId, ArrayList<Product> items) throws Exception {
        String insertOrderSql =
                "INSERT INTO orders (employee_id, customer_id, order_date, total_amount) VALUES (?, ?, NOW(), ?)";

        String insertItemSql =
                "INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";

        String updateStockSql =
                "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";

        PreparedStatement orderPS = null;
        PreparedStatement itemPS = null;
        PreparedStatement stockPS = null;
        ResultSet rs = null;

        try {
            con.setAutoCommit(false);

            double totalAmount = 0;
            for (Product p : items) totalAmount += p.getTotalPrice();

            // 1) Insert order
            orderPS = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            orderPS.setInt(1, employeeId);
            orderPS.setInt(2, customerId);
            orderPS.setDouble(3, totalAmount);
            orderPS.executeUpdate();

            rs = orderPS.getGeneratedKeys();
            if (!rs.next()) throw new Exception("Failed to create order (no generated key)");
            int orderId = rs.getInt(1);

            // 2) Insert items + update stock
            itemPS = con.prepareStatement(insertItemSql);
            stockPS = con.prepareStatement(updateStockSql);

            for (Product p : items) {
                int productId = p.getId();
                int qty = p.getQuantity();
                double unitPrice = p.getPricePerPiece();
                double lineTotal = p.getTotalPrice();

                // 2a) Decrease stock first (recommended)
                stockPS.setInt(1, qty);
                stockPS.setInt(2, productId);
                stockPS.setInt(3, qty);

                int affected = stockPS.executeUpdate();
                if (affected == 0) {
                    throw new Exception("Not enough stock for: " + p.getItemName());
                }

                // 2b) Insert order item
                itemPS.setInt(1, orderId);
                itemPS.setInt(2, productId);
                itemPS.setInt(3, qty);
                itemPS.setDouble(4, unitPrice);
                itemPS.setDouble(5, lineTotal);
                itemPS.executeUpdate();
            }

            con.commit();
            return orderId;

        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
            if (rs != null) rs.close();
            if (orderPS != null) orderPS.close();
            if (itemPS != null) itemPS.close();
            if (stockPS != null) stockPS.close();
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
