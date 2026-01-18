package Connection;

import Product.Category;

import java.sql.*;
import java.util.ArrayList;

public class CategoryDAO {
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
                "SELECT COALESCE(c.name, 'Uncategorized') AS category_name, " +
                        "       SUM(oi.line_total) AS revenue " +
                        "FROM order_items oi " +
                        "JOIN products p ON p.id = oi.product_id " +
                        "LEFT JOIN categories c ON c.id = p.category_id " +
                        "GROUP BY COALESCE(c.name, 'Uncategorized') " +
                        "ORDER BY revenue DESC " +
                        "LIMIT 1";

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
