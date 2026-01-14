package Connection;

import Product.Category;

import java.sql.*;
import java.util.ArrayList;

public class CategoryDAO {

    // ===============================
    // Get all categories (for ComboBox, TableView, etc.)
    // ===============================
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

    // ===============================
    // Get category ID by name
    // ===============================
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
        return null; // not found
    }

    // ===============================
    // Insert category and return generated ID
    // ===============================
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

    // ===============================
    // Get category ID or create it automatically
    // ===============================
    public int getOrCreateCategoryId(Connection con, String name) throws SQLException {
        // Normalize (optional but recommended)
        name = name.trim();

        // 1) try find existing
        Integer id = getCategoryIdByName(con, name);
        if (id != null) {
            return id;
        }

        // 2) create new
        return insertCategory(con, name);
    }
}
