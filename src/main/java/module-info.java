module com.example.comp333finalproj {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires jdk.jdi;
    requires java.desktop;
    requires kernel;
    requires io;
    requires layout;
    requires org.slf4j;

    opens EmployeeView to javafx.base, javafx.fxml;
    opens Product to javafx.base, javafx.fxml;

    opens ManagerView.EmployeeManagement to javafx.base;



    exports com.example.comp333finalproj;
    opens EmployeeView.Customer to javafx.base, javafx.fxml;
    opens EmployeeView.Orders to javafx.base, javafx.fxml;
    opens EmployeeView.Cash to javafx.base, javafx.fxml;


}
