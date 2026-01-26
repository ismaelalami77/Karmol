package Connection;

import EmployeeView.Customer.Customer;

import java.sql.*;
import java.util.ArrayList;

public class CustomerDAO {

    // gets all customers from the customers table
    // orders them by customer id
    // returns them as an ArrayList
    public ArrayList<Customer> getAllCustomers(Connection con) {
        ArrayList<Customer> customers = new ArrayList<>();

        String sql = "SELECT customer_id, customer_name, customer_email, customer_phone, customer_address " +
                "FROM customers ORDER BY customer_id";

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

    // insert a new customer into the customers table
    // generates an automatic id for the customer
    // returns the generated customer id
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

    // search for a customer using phone number
    // if found, return the customer object
    // if not found, return null
    public Customer getCustomerByPhone(Connection con, String phone) {
        String sql = "SELECT * FROM customers WHERE customer_phone = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("customer_name"),
                            rs.getString("customer_email"),
                            rs.getString("customer_phone"),
                            rs.getString("customer_address")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // update customer information using customer id
    // updates name, email, phone, and address
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

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // checks if a phone number already exists in the database
    // returns true if found, false otherwise
    public boolean phoneExists(Connection con, String phone) throws SQLException {
        String sql = "SELECT 1 FROM customers WHERE customer_phone = ? LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // counts total number of customers in the database
    // used for dashboard statistics
    public int getTotalCustomers() {
        String sql = "SELECT COUNT(*) AS total FROM customers";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt("total");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
