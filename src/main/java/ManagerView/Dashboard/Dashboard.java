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
            topCategoryLabel, topEmployeeLabel;

    public Dashboard() {
        root = new BorderPane();

        dashboardText = UIHelperC.createTitleText("Manager Dashboard");
        BorderPane.setAlignment(dashboardText, Pos.CENTER);
        BorderPane.setMargin(dashboardText, new Insets(20, 0, 0, 0));
        root.setTop(dashboardText);


        leftVBox = new VBox();
        leftVBox.setAlignment(Pos.CENTER);
        leftVBox.setSpacing(15);
        leftVBox.setPadding(new Insets(20));

        barChart = createBarChart();
        pieChart = createPieChart();
        leftVBox.getChildren().addAll(barChart, pieChart);


        rightVBox = statsBox();

        root.setCenter(leftVBox);
        root.setRight(rightVBox);

        refreshAllInBackground();
    }

    private VBox statsBox() {
        VBox vBox = new VBox(20);
        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);

        totalRevenueLabel = createStatLabel("Total Revenue", "Calculating...");
        totalEmployeeLabel = createStatLabel("Total Employees", "0");
        totalCustomersLabel = createStatLabel("Total Customers", "0");
        topCategoryLabel = createStatLabel("Top Category", "-");
        topEmployeeLabel = createStatLabel("Top Employee", "-");

        vBox.getChildren().addAll(
                totalRevenueLabel, totalEmployeeLabel, totalCustomersLabel,
                topCategoryLabel, topEmployeeLabel
        );

        return vBox;
    }

    private Label createStatLabel(String title, String value) {
        Label label = new Label(title + "\n" + value);
        label.getStyleClass().add("statistics-square");
        label.setAlignment(Pos.CENTER);
        label.setMinWidth(180);
        return label;
    }

    private BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Employee");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Revenue Per Employee");

        chart.setAnimated(false);
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);
        chart.setLegendVisible(false);

        barChartSeries.setName("Revenue");
        chart.getData().add(barChartSeries);

        return chart;
    }


    private PieChart createPieChart() {
        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Category Revenue Distribution");

        chart.setAnimated(false);
        chart.setLegendVisible(true);

        return chart;
    }


    private void refreshAllInBackground() {
        Thread t = new Thread(() -> {
            try (Connection con = DBUtil.getConnection()) {

                OrderDAO orderDAO = new OrderDAO();
                CustomerDAO customerDAO = new CustomerDAO();
                EmployeeDAO employeeDAO = new EmployeeDAO();
                CategoryDAO categoryDAO = new CategoryDAO();


                LinkedList empList = orderDAO.getRevenuePerEmployee(con);
                ObservableList<XYChart.Data<String, Number>> barData = FXCollections.observableArrayList();
                Node cur1 = empList.getFront();
                while (cur1 != null) {
                    String[] row = (String[]) cur1.getElement();
                    barData.add(new XYChart.Data<>(row[0], Double.parseDouble(row[1])));
                    cur1 = cur1.getNext();
                }


                LinkedList catList = orderDAO.getCategoryRevenue(con);
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                Node cur2 = catList.getFront();
                while (cur2 != null) {
                    String[] row = (String[]) cur2.getElement();
                    pieData.add(new PieChart.Data(row[0], Double.parseDouble(row[1])));
                    cur2 = cur2.getNext();
                }


                double totalRev = orderDAO.getTotalRevenue();
                int totalEmp = employeeDAO.getTotalEmployees();
                int totalCustomers = customerDAO.getTotalCustomers();
                String topCat = categoryDAO.getTopCategory();
                String topEmp = employeeDAO.getTopEmployee();


                Platform.runLater(() -> {

                    barChartSeries.getData().setAll(barData);
                    pieChartData.setAll(pieData);


                    totalRevenueLabel.setText("Total Revenue\n$" + String.format("%.2f", totalRev));
                    totalEmployeeLabel.setText("Total Employees\n" + totalEmp);
                    totalCustomersLabel.setText("Total Customers\n" + totalCustomers);
                    topCategoryLabel.setText("Top Category\n" + (topCat != null ? topCat : "N/A"));
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