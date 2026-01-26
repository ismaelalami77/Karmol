package Connection;

import ManagerView.EmployeeManagement.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class EmployeeDAO {

    // gets all employees from the users table
    // only selects users with role = 'EMPLOYEE'
    // splits full name into first name and last name
    // returns them as an ArrayList
    public static ArrayList<Employee> getAllEmployees() {
        ArrayList<Employee> list = new ArrayList<>();

        String sql = """
                SELECT id, full_name, username, password, phone, address
                FROM users
                WHERE role = 'EMPLOYEE'
            """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String fullName = rs.getString("full_name");
                String[] names = (fullName == null ? "" : fullName.trim()).split(" ", 2);

                Employee e = new Employee(
                        rs.getInt("id"),
                        names.length > 0 ? names[0] : "",
                        names.length > 1 ? names[1] : "",
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("address")
                );

                list.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // inserts a new employee into the users table
    // combines first name and last name into full_name
    // sets role as EMPLOYEE
    // returns the generated employee id
    public static int insertEmployee(Connection con, Employee employee) {

        String sql = """
                INSERT INTO users (full_name, username, password, phone, address, role)
                VALUES (?, ?, ?, ?, ?, 'EMPLOYEE')
            """;

        String fullName = (employee.getFirstName() + " " + employee.getLastName()).trim();

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, fullName);
            ps.setString(2, employee.getUsername());
            ps.setString(3, employee.getPassword());
            ps.setString(4, employee.getPhoneNumber());
            ps.setString(5, employee.getAddress());

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

            return -1;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // updates employee information in the database
    // updates full name, username, password, phone, and address
    // only updates users with role = EMPLOYEE
    public static boolean updateEmployee(Connection con, Employee employee) {

        String sql = """
                UPDATE users
                SET full_name = ?,
                    username  = ?,
                    password  = ?,
                    phone     = ?,
                    address   = ?
                WHERE id = ? AND role = 'EMPLOYEE'
            """;

        String fullName = (employee.getFirstName() + " " + employee.getLastName()).trim();

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, employee.getUsername());
            ps.setString(3, employee.getPassword());
            ps.setString(4, employee.getPhoneNumber());
            ps.setString(5, employee.getAddress());
            ps.setInt(6, employee.getEmployeeId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // gets employee information using user id
    // used when employee logs in
    // returns the employee object if found
    public static Employee getEmployeeByUserId(int userId) {

        String sql = """
                SELECT id, full_name, username, password, phone, address
                FROM users
                WHERE id = ? AND role = 'EMPLOYEE'
            """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String fullName = rs.getString("full_name");
                    String[] names = (fullName == null ? "" : fullName.trim()).split(" ", 2);

                    return new Employee(
                            rs.getInt("id"),
                            names.length > 0 ? names[0] : "",
                            names.length > 1 ? names[1] : "",
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("phone"),
                            rs.getString("address")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // checks if the entered old password matches the password in the database
    // used before allowing password update
    public static boolean checkOldPassword(int employeeId, String oldPassword) {
        String sql = """
                SELECT 1
                FROM users
                WHERE id = ? AND role='EMPLOYEE' AND password = ?
                LIMIT 1
            """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setString(2, oldPassword);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // updates employee password after old password is verified
    public static boolean updatePassword(int employeeId, String newPassword) {
        String sql = """
                UPDATE users
                SET password = ?
                WHERE id = ? AND role='EMPLOYEE'
            """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, employeeId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // counts total number of employees
    // used for dashboard statistics
    public int getTotalEmployees() {
        String sql = "SELECT COUNT(*) AS total FROM users WHERE role = 'EMPLOYEE'";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt("total");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // finds the employee with the highest total sales
    // used in manager dashboard
    public String getTopEmployee() {
        String sql =
                "SELECT u.username AS employee_name, SUM(o.total_amount) AS revenue " +
                        "FROM orders o " +
                        "JOIN users u ON u.id = o.employee_id " +
                        "GROUP BY u.id, u.username " +
                        "ORDER BY revenue DESC " +
                        "LIMIT 1";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getString("employee_name");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "N/A";
    }
}
