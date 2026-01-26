package Connection;

import DataStructure.LinkedList;
import EmployeeView.Orders.Order;
import Product.Product;

import java.sql.*;
import java.util.ArrayList;

public class OrderDAO {

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


            orderPS = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            orderPS.setInt(1, employeeId);
            orderPS.setInt(2, customerId);
            orderPS.setDouble(3, totalAmount);
            orderPS.executeUpdate();

            rs = orderPS.getGeneratedKeys();
            if (!rs.next()) throw new Exception("Failed to create order (no generated key)");
            int orderId = rs.getInt(1);

            itemPS = con.prepareStatement(insertItemSql);
            stockPS = con.prepareStatement(updateStockSql);

            for (Product p : items) {
                int productId = p.getId();
                int qty = p.getQuantity();
                double unitPrice = p.getPricePerPiece();
                double lineTotal = p.getTotalPrice();


                stockPS.setInt(1, qty);
                stockPS.setInt(2, productId);
                stockPS.setInt(3, qty);

                int affected = stockPS.executeUpdate();
                if (affected == 0) {
                    throw new Exception("Not enough stock for: " + p.getItemName());
                }


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
    public LinkedList getOrderDetails(Connection con, int orderId) throws SQLException {
        LinkedList list = new LinkedList();

        String productNameCol = findFirstExistingColumn(con, "products",
                "name", "product_name", "productName", "pname", "title", "productTitle", "item_name");

        if (productNameCol == null) {
            throw new SQLException("Could not find a product name column in table 'products'. " +
                    "Expected one of: name, product_name, productName, pname, title, item_name");
        }

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
                    list.addLast(od);
                }
            }
        }

        return list;
    }
    private String findFirstExistingColumn(Connection con, String tableName, String... candidates) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();


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
    public int getNextOrderId(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM orders";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("next_id");
        }
        return 1;
    }
    public LinkedList getRevenuePerEmployee(Connection con) throws SQLException {
        LinkedList revenuePerEmployeeList = new LinkedList();
        String sql =
                "SELECT u.username AS employee_name, SUM(o.total_amount) AS revenue " +
                        "FROM orders o " +
                        "JOIN users u ON u.id = o.employee_id " +
                        "GROUP BY u.username " +
                        "ORDER BY revenue DESC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("employee_name");
                String revenue = String.valueOf(rs.getDouble("revenue"));

                revenuePerEmployeeList.addLast(new String[]{name, revenue});
            }
        }
        return revenuePerEmployeeList;
    }
    public LinkedList getCategoryRevenue(Connection con) throws SQLException {
        LinkedList categoryRevenueList = new LinkedList();

        String sql =
                "SELECT COALESCE(c.name, 'Uncategorized') AS category_name, " +
                        "       SUM(oi.line_total) AS total_revenue " +
                        "FROM order_items oi " +
                        "JOIN products p ON p.id = oi.product_id " +
                        "LEFT JOIN categories c ON c.id = p.category_id " +
                        "GROUP BY COALESCE(c.name, 'Uncategorized') " +
                        "ORDER BY total_revenue DESC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String categoryName = rs.getString("category_name");
                double totalRevenue = rs.getDouble("total_revenue");

                categoryRevenueList.addLast(new String[]{categoryName, String.valueOf(totalRevenue)});
            }
        }

        return categoryRevenueList;
    }
    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) AS total_revenue FROM orders";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getDouble("total_revenue");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }
    public String getTopClient() {
        String sql =
                "SELECT c.customer_name AS customer_name, SUM(o.total_amount) AS revenue " +
                        "FROM orders o " +
                        "JOIN customers c ON c.customer_id = o.customer_id " +
                        "GROUP BY c.customer_id, c.customer_name " +
                        "ORDER BY revenue DESC " +
                        "LIMIT 1";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getString("customer_name");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "N/A";
    }





}
