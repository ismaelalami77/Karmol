module com.example.comp333finalproj {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires jdk.jdi;

    opens EmployeeView to javafx.base, javafx.fxml;
    opens Product to javafx.base, javafx.fxml;

    opens ManagerView.EmployeeManagement to javafx.base;



    exports com.example.comp333finalproj;



}
