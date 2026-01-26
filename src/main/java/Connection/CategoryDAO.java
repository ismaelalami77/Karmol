package Connection;

import Product.Category;

import java.sql.*;
import java.util.ArrayList;


public class CategoryDAO {
    //gets all categories from categories tables
    //order them by name
    //then returns them as an array list
    public ArrayList<Category> getAllCategories(Connection con) {
        ArrayList<Category> categories = new ArrayList<>();

        String sql = "SELECT id, name FROM categories ORDER BY name";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(
                        new Category(
                                rs.getInt("id"),
                                rs.getString("name")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    //search for category by name and shows its id
    public Integer getCategoryIdByName(Connection con, String name) {
        String sql = "SELECT id FROM categories WHERE name = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //insert new category
    // generate an automatic id for it
    public int insertCategory(Connection con, String name) throws SQLException {
        String sql = "INSERT INTO categories (name) VALUES (?)";

        try (PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to insert category: " + name);
    }

    //check for category name
    // if it already exists it returns the id
    // if not it create a new category
    // so it can prevent duplicates
    public int getOrCreateCategoryId(Connection con, String name) throws SQLException {

        name = name.trim();

        Integer id = getCategoryIdByName(con, name);
        if (id != null) {
            return id;
        }

        return insertCategory(con, name);
    }

    public String getTopCategory() {

        String sql =
                //select the category name and calculates total revenue for each category
                "SELECT c.name AS category_name, SUM(oi.line_total) AS revenue\n" +
                        //get data from order items
                        "FROM order_items oi\n" +
                        //join products to get which product sold the most
                        "JOIN products p ON p.id = oi.product_id\n" +
                        //join categories to know the category of each product
                        "JOIN categories c ON c.id = p.category_id\n" +
                        //group all sales by category name
                        "GROUP BY c.name\n" +
                        //orders categories by revenue
                        "ORDER BY revenue DESC \n" +
                        //returns category with the highest revenue
                        "LIMIT 1;\n";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getString("category_name");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "N/A";
    }

}
