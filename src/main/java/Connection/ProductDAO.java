package Connection;

import Product.Product;

import java.sql.*;
import java.util.ArrayList;

public class ProductDAO {

    public ArrayList<Product> getProductsByCategoryId(Connection con, int categoryId) {
        ArrayList<Product> products = new ArrayList<>();

        String sql =
                "SELECT p.id, p.productName, p.productPrice, p.quantity, c.name AS categoryName " +
                        "FROM products p " +
                        "JOIN categories c ON p.category_id = c.id " +
                        "WHERE p.category_id = ? " +
                        "ORDER BY p.productName";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(
                            rs.getInt("id"),
                            rs.getString("productName"),
                            rs.getString("categoryName"),
                            rs.getDouble("productPrice")
                    );

                    p.setQuantity(rs.getInt("quantity"));

                    products.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }



    public Integer getCategoryIdByName(Connection con, String categoryName) {
        String sql = "SELECT id FROM categories WHERE name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer getProductIdByNameAndCategory(Connection con, String productName, int categoryId) {
        String sql = "SELECT id FROM products WHERE productName = ? AND category_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productName);
            ps.setInt(2, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertProductWithQuantity(Connection con, Product p, int categoryId, int quantity) {
        String sql = "INSERT INTO products (productName, productPrice, category_id, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getItemName());
            ps.setDouble(2, p.getPricePerPiece());
            ps.setInt(3, categoryId);
            ps.setInt(4, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addQuantityToExistingProduct(Connection con, int productId, int addQty) {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, addQty);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Product> getAllProducts(Connection con) {
        ArrayList<Product> list = new ArrayList<>();

        String sql =
                "SELECT p.id, p.productName, p.productPrice, p.quantity, c.name AS categoryName " +
                        "FROM products p " +
                        "JOIN categories c ON p.category_id = c.id " +
                        "ORDER BY c.name, p.productName";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("productName"),
                        rs.getString("categoryName"),
                        rs.getDouble("productPrice")
                );
                p.setQuantity(rs.getInt("quantity")); // ✅ inventory quantity
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean updateProduct(Connection con, int productId, String name, int categoryId, double price, int qty) {
        String sql = "UPDATE products SET productName=?, category_id=?, productPrice=?, quantity=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, categoryId);
            ps.setDouble(3, price);
            ps.setInt(4, qty);
            ps.setInt(5, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public String getTopProduct() {
        String sql =
                "SELECT p.productName AS product_name, SUM(oi.line_total) AS revenue " +
                        "FROM order_items oi " +
                        "JOIN products p ON p.id = oi.product_id " +
                        "GROUP BY p.id, p.productName " +
                        "ORDER BY revenue DESC " +
                        "LIMIT 1";

        try(Connection con = DBUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()){
            if (rs.next()) return rs.getString("product_name");
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }
}
