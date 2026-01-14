package Connection;

import EmployeeView.Customer;

import java.sql.*;
import java.util.ArrayList;

public class CustomerDAO {
    public ArrayList<Customer> getAllCustomers(Connection con) {
        ArrayList<Customer> customers = new ArrayList<>();

        String sql = "SELECT customer_id, customer_name, customer_email, customer_phone, customer_address " +
                "FROM customers Order By customer_id";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_email"),
                        rs.getString("customer_phone"),
                        rs.getString("customer_address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;

    }

    public int getNextCustomerID(Connection con) {
        String sql = "SELECT COALESCE(Max(customer_id),0) + 1 AS next_id from customers";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public int insertCustomer(Connection con, Customer c) {
        String sql = "INSERT INTO customers (customer_name, customer_email, customer_phone, customer_address) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCustomerName());
            ps.setString(2, c.getCustomerEmail());
            ps.setString(3, c.getCustomerPhone());
            ps.setString(4, c.getCustomerAddress());

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_email"),
                        rs.getString("customer_phone"),
                        rs.getString("customer_address")
                );
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteCustomerByID(Connection con, int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1,customerId);
            return ps.executeUpdate() > 0;
        }catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCustomer(Connection con, int customerId,
                                  String name, String email, String phone, String address) {

        String sql = "UPDATE customers SET customer_name=?, customer_email=?, customer_phone=?, customer_address=? " +
                "WHERE customer_id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setInt(5, customerId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
