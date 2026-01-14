package Connection;

import ManagerView.EmployeeManagement.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class EmployeeDAO {

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

    public static boolean deleteEmployeeByID(Connection con, int employeeId) {

        String sql = """
        DELETE FROM users
        WHERE id = ? AND role = 'EMPLOYEE'
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

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



}
