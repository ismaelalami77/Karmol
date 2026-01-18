package ManagerView.Dashboard;

import Connection.*;
import DataStructure.LinkedList;
import DataStructure.Node;
import com.example.comp333finalproj.UIHelperC;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;

public class Dashboard {

    private final XYChart.Series<String, Number> barChartSeries = new XYChart.Series<>();
    private final ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
    private BorderPane root;
    private Text dashboardText;
    private BarChart<String, Number> barChart;
    private PieChart pieChart;

    private VBox leftVBox, rightVBox;

    private Label totalRevenueLabel, totalEmployeeLabel, totalCustomersLabel,
            topCategoryLabel, topProductLabel, topClientLabel, topEmployeeLabel;

    public Dashboard() {
        root = new BorderPane();

        // 1. Title at the Top
        dashboardText = UIHelperC.createTitleText("Manager Dashboard");
        BorderPane.setAlignment(dashboardText, Pos.CENTER);
        BorderPane.setMargin(dashboardText, new Insets(20, 0, 0, 0));
        root.setTop(dashboardText);

        // 2. Charts (Left Side)
        leftVBox = new VBox();
        leftVBox.setAlignment(Pos.CENTER);
        leftVBox.setSpacing(15);
        leftVBox.setPadding(new Insets(20));

        barChart = createBarChart();
        pieChart = createPieChart();
        leftVBox.getChildren().addAll(barChart, pieChart);

        // 3. Stats (Right Side)
        rightVBox = statsBox();

        root.setCenter(leftVBox);
        root.setRight(rightVBox);

        refreshAllInBackground();
    }

    private VBox statsBox() {
        VBox vBox = new VBox(20); // Reduced spacing slightly for fit
        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.TOP_CENTER);

        totalRevenueLabel = createStatLabel("Total Revenue", "Calculating...");
        totalEmployeeLabel = createStatLabel("Total Employees", "0");
        totalCustomersLabel = createStatLabel("Total Customers", "0");
        topCategoryLabel = createStatLabel("Top Category", "-");
        topProductLabel = createStatLabel("Top Product", "-");
        topClientLabel = createStatLabel("Top Client", "-");
        topEmployeeLabel = createStatLabel("Top Employee", "-");

        vBox.getChildren().addAll(
                totalRevenueLabel, totalEmployeeLabel, totalCustomersLabel,
                topCategoryLabel, topProductLabel, topClientLabel, topEmployeeLabel
        );

        return vBox;
    }

    private Label createStatLabel(String title, String value) {
        Label label = new Label(title + "\n" + value);
        label.getStyleClass().add("statistics-square");
        label.setAlignment(Pos.CENTER);
        label.setMinWidth(180); // Ensure consistent box size
        return label;
    }

    private BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Employee");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Revenue Per Employee");
        barChartSeries.setName("Revenue");
        chart.getData().add(barChartSeries);
        return chart;
    }

    private PieChart createPieChart() {
        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Category Revenue Distribution");
        return chart;
    }

    private void refreshAllInBackground() {
        Thread t = new Thread(() -> {
            try (Connection con = DBUtil.getConnection()) {
                // DAOs
                OrderDAO orderDAO = new OrderDAO();
                CustomerDAO customerDAO = new CustomerDAO();
                EmployeeDAO employeeDAO = new EmployeeDAO();
                CategoryDAO categoryDAO = new CategoryDAO();
                ProductDAO productDAO = new ProductDAO();

                // 1. Process Bar Chart Data
                LinkedList empList = orderDAO.getRevenuePerEmployee(con);
                ObservableList<XYChart.Data<String, Number>> barData = FXCollections.observableArrayList();
                Node cur1 = empList.getFront();
                while (cur1 != null) {
                    String[] row = (String[]) cur1.getElement();
                    barData.add(new XYChart.Data<>(row[0], Double.parseDouble(row[1])));
                    cur1 = cur1.getNext();
                }

                // 2. Process Pie Chart Data
                LinkedList catList = orderDAO.getCategoryRevenue(con);
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                Node cur2 = catList.getFront();
                while (cur2 != null) {
                    String[] row = (String[]) cur2.getElement();
                    pieData.add(new PieChart.Data(row[0], Double.parseDouble(row[1])));
                    cur2 = cur2.getNext();
                }

                // 3. Fetch Statistics
                double totalRev = orderDAO.getTotalRevenue();
                int totalEmp = employeeDAO.getTotalEmployees();
                int totalCust = customerDAO.getTotalCustomers();
                String topCat = categoryDAO.getTopCategory();
                String topProd = productDAO.getTopProduct();
                String topCli = orderDAO.getTopClient();
                String topEmp = employeeDAO.getTopEmployee();

                // 4. Update UI on JavaFX Application Thread
                Platform.runLater(() -> {
                    // Update Charts
                    barChartSeries.getData().setAll(barData);
                    pieChartData.setAll(pieData);

                    // Update Labels (Fixed logic errors where topClientLabel was reused)
                    totalRevenueLabel.setText("Total Revenue\n$" + String.format("%.2f", totalRev));
                    totalEmployeeLabel.setText("Total Employees\n" + totalEmp);
                    totalCustomersLabel.setText("Total Customers\n" + totalCust);
                    topCategoryLabel.setText("Top Category\n" + (topCat != null ? topCat : "N/A"));
                    topProductLabel.setText("Top Product\n" + (topProd != null ? topProd : "N/A"));
                    topClientLabel.setText("Top Client\n" + (topCli != null ? topCli : "N/A"));
                    topEmployeeLabel.setText("Top Employee\n" + (topEmp != null ? topEmp : "N/A"));
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t.setDaemon(true);
        t.start();
    }

    public BorderPane getRoot() {
        return root;
    }
}