package ManagerView.EmployeeManagement;

public class Employee {

    private int employeeId;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String phoneNumber;
    private String address;

    // ✅ Empty constructor (REQUIRED for JavaFX & JDBC)
    public Employee() {}

    // ✅ Full constructor (used when loading from DB)
    public Employee(int employeeId,
                    String firstName,
                    String lastName,
                    String username,
                    String password,
                    String phoneNumber,
                    String address) {

        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // ✅ Getters & Setters (REQUIRED for TableView)

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    // ⚠️ You may hide this column later
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // ✅ Optional helper
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
