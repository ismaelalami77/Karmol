package Connection;

import DataStructure.LinkedList;
import EmployeeView.Orders.Order;
import Product.Product;

import java.sql.*;
import java.util.ArrayList;

public class OrderDAO {

    // creates a new order with its order items (products)
    // uses a transaction so everything is saved together (order + items + stock update)
    // if any error happens, it rolls back and nothing is saved
    public int createOrderWithItems(Connection con, int employeeId, int customerId, ArrayList<Product> items) throws Exception {

        // insert new order into orders table
        String insertOrderSql =
                "INSERT INTO orders (employee_id, customer_id, order_date, total_amount) VALUES (?, ?, NOW(), ?)";

        // insert each product into order_items table
        String insertItemSql =
                "INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";

        // update product quantity after selling
        // AND quantity >= ? ensures we don't sell more than available stock
        String updateStockSql =
                "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";

        PreparedStatement orderPS = null;
        PreparedStatement itemPS = null;
        PreparedStatement stockPS = null;
        ResultSet rs = null;

        try {
            // start transaction
            con.setAutoCommit(false);

            // calculate the total amount of the order by summing all item totals
            double totalAmount = 0;
            for (Product p : items) totalAmount += p.getTotalPrice();

            // 1) insert order and get generated order id
            orderPS = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            orderPS.setInt(1, employeeId);
            orderPS.setInt(2, customerId);
            orderPS.setDouble(3, totalAmount);
            orderPS.executeUpdate();

            rs = orderPS.getGeneratedKeys();
            if (!rs.next()) throw new Exception("Failed to create order (no generated key)");
            int orderId = rs.getInt(1);

            // prepare statements for inserting items + updating stock
            itemPS = con.prepareStatement(insertItemSql);
            stockPS = con.prepareStatement(updateStockSql);

            // 2) for each product: update stock then insert order item
            for (Product p : items) {
                int productId = p.getId();
                int qty = p.getQuantity();
                double unitPrice = p.getPricePerPiece();
                double lineTotal = p.getTotalPrice();

                // update stock first
                stockPS.setInt(1, qty);
                stockPS.setInt(2, productId);
                stockPS.setInt(3, qty);

                int affected = stockPS.executeUpdate();
                if (affected == 0) {
                    // if stock update failed, means not enough quantity
                    throw new Exception("Not enough stock for: " + p.getItemName());
                }

                // insert into order_items table
                itemPS.setInt(1, orderId);
                itemPS.setInt(2, productId);
                itemPS.setInt(3, qty);
                itemPS.setDouble(4, unitPrice);
                itemPS.setDouble(5, lineTotal);
                itemPS.executeUpdate();
            }

            // if everything succeeded, save changes
            con.commit();
            return orderId;

        } catch (Exception e) {
            // if any error happens, undo all changes
            con.rollback();
            throw e;
        } finally {
            // return connection to normal mode
            con.setAutoCommit(true);

            // close resources manually
            if (rs != null) rs.close();
            if (orderPS != null) orderPS.close();
            if (itemPS != null) itemPS.close();
            if (stockPS != null) stockPS.close();
        }
    }

    // gets all orders from orders table
    // ordered by newest first (DESC)
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

    // gets order item details for a specific order
    // returns a LinkedList of OrderDetails (products inside that order)
    public LinkedList getOrderDetails(Connection con, int orderId) throws SQLException {
        LinkedList list = new LinkedList();

        // tries to find which column name is used for product name in your products table
        // because some projects use different column names (name / productName / product_name ...)
        String productNameCol = findFirstExistingColumn(con, "products",
                "name", "product_name", "productName", "pname", "title", "productTitle", "item_name");

        // if none of the columns exist, stop and show error
        if (productNameCol == null) {
            throw new SQLException("Could not find a product name column in table 'products'. " +
                    "Expected one of: name, product_name, productName, pname, title, item_name");
        }

        // check if products table has category_id column
        String categoryIdCol = findFirstExistingColumn(con, "products", "category_id", "categoryId", "cat_id");
        // check if the categories table has name column
        String categoryNameCol = findFirstExistingColumn(con, "categories", "name", "category_name", "categoryName", "title");

        // if both exist, then categories are supported in this database
        boolean hasCategories = (categoryIdCol != null && categoryNameCol != null);

        String sql;

        if (hasCategories) {
            // query that includes category name (LEFT JOIN to avoid errors if category is missing)
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
            // query without categories (category_name becomes empty string)
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

        // run the query for the given order id
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // create OrderDetails object for each row and add to linked list
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

    // helper method: checks the table columns and returns the first column name found
    // used because column names can be different in different schemas
    private String findFirstExistingColumn(Connection con, String tableName, String... candidates) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();

        java.util.HashSet<String> cols = new java.util.HashSet<>();

        // read all column names from the table
        try (ResultSet rs = meta.getColumns(con.getCatalog(), null, tableName, null)) {
            while (rs.next()) {
                cols.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }

        // return the first matching candidate that exists
        for (String c : candidates) {
            if (cols.contains(c.toLowerCase())) return c;
        }
        return null;
    }

    // gets the next order id (max id + 1)
    // used if you want to show the next invoice/order number
    public int getNextOrderId(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM orders";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("next_id");
        }
        return 1;
    }

    // gets total revenue per employee
    // used for dashboard bar chart (employee name + revenue)
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

                // store as String array to match your LinkedList structure
                revenuePerEmployeeList.addLast(new String[]{name, revenue});
            }
        }
        return revenuePerEmployeeList;
    }

    // gets total revenue per category
    // used for dashboard pie chart (category name + revenue)
    public LinkedList getCategoryRevenue(Connection con) throws SQLException {
        LinkedList categoryRevenueList = new LinkedList();

        String sql =
                "SELECT c.name AS category_name, SUM(oi.line_total) AS total_revenue\n" +
                        "FROM order_items oi\n" +
                        "JOIN products p ON p.id = oi.product_id\n" +
                        "JOIN categories c ON c.id = p.category_id\n" +
                        "GROUP BY c.name\n" +
                        "ORDER BY total_revenue DESC;\n";

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

    // calculates total revenue of all orders
    // used for dashboard total revenue
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

    // finds the customer with the highest total spending
    // used for dashboard top client
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
