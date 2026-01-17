// ========================= OrderHistory.java =========================
package EmployeeView;

import Connection.DBUtil;
import Connection.OrderDAO;
import DataStructure.LinkedList;
import DataStructure.Node;
import com.example.comp333finalproj.UIHelperC;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class OrderHistory {

    public static TableView<Order> ordersHistoryTableView;
    public static ArrayList<Order> ordersArrayList = new ArrayList<>();
    public static ObservableList<Order> ordersObservableList = FXCollections.observableArrayList();

    private final BorderPane root;
    private final VBox centerVbox;
    private final Text titleText;

    private static final DeviceRgb BRAND_GREEN = new DeviceRgb(0, 166, 80);
    private static final DeviceRgb LIGHT_GREEN = new DeviceRgb(230, 247, 239); // background
    private static final DeviceRgb ROW_ALT = new DeviceRgb(210, 238, 225);     // zebra rows


    public OrderHistory() {
        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);
        centerVbox.setPadding(new Insets(20));

        titleText = UIHelperC.createTitleText("Order History");

        ordersHistoryTableView = new TableView<>();

        TableColumn<Order, Integer> orderIDCol = new TableColumn<>("Order ID");
        orderIDCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<Order, Integer> employeeIDCol = new TableColumn<>("Employee ID");
        employeeIDCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));

        TableColumn<Order, Integer> customerIDCol = new TableColumn<>("Customer ID");
        customerIDCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<Order, Timestamp> orderDateCol = new TableColumn<>("Order Date");
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        TableColumn<Order, Double> amountCol = new TableColumn<>("Total Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Order, Void> printCol = new TableColumn<>("Print");
        setupPrintColumn(printCol);

        ordersHistoryTableView.getColumns().addAll(
                orderIDCol, employeeIDCol, customerIDCol, orderDateCol, amountCol, printCol
        );

        ordersHistoryTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ordersHistoryTableView.setPrefHeight(400);
        ordersHistoryTableView.setItems(ordersObservableList);

        centerVbox.getChildren().addAll(titleText, ordersHistoryTableView);
        root.setCenter(centerVbox);

        loadAllOrdersFromDB();
    }

    private void setupPrintColumn(TableColumn<Order, Void> printCol) {
        printCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = UIHelperC.createStyledButton("Print");

            {
                btn.setOnAction(e -> {
                    if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) return;
                    Order order = getTableView().getItems().get(getIndex());
                    printBill(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    public static void loadAllOrdersFromDB() {
        try (Connection con = DBUtil.getConnection()) {
            OrderDAO dao = new OrderDAO();
            ordersArrayList = dao.getAllOrders(con);
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void refreshTable() {
        ordersObservableList.clear();
        ordersObservableList.addAll(ordersArrayList);
    }

    public BorderPane getRoot() {
        return root;
    }

    private void printBill(Order order) {
        if (order == null) return;

        try {
            // Save to Desktop
            String directoryPath = System.getProperty("user.home") + File.separator + "Desktop";
            String fileName = "Bill_Order_" + order.getOrderId() + ".pdf";
            String filePath = directoryPath + File.separator + fileName;

            File directory = new File(directoryPath);
            if (!directory.exists()) directory.mkdirs();

            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Logo (put it in: src/main/resources/images/logo.png)
            try {
                var url = getClass().getResource("/com/example/comp333finalproj/logo.png");
                if (url != null) {
                    Image logo = new Image(ImageDataFactory.create(url.toExternalForm()));
                    logo.setWidth(100);
                    logo.setHeight(100);
                    logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    document.add(logo);
                }
            } catch (Exception ignored) { }

            // Header
            document.add(new Paragraph("Karmol")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY));

            document.add(new Paragraph("Bill for Order ID: " + order.getOrderId())
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph(" "));

            String employeeName = "N/A";
            String customerName = "N/A";

            try(Connection con = DBUtil.getConnection()) {
                if (con != null){
                    employeeName = getEmployeeName(con, order.getEmployeeId());
                    customerName = getCustomerName(con, order.getCustomerId());
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

            // Details table
            Table detailsTable = new Table(new float[]{1, 2});
            detailsTable.setHorizontalAlignment(HorizontalAlignment.CENTER);
            detailsTable.setBackgroundColor(LIGHT_GREEN);


            detailsTable.addCell(new Cell().add(new Paragraph("Employee ID:").setBold()));
            detailsTable.addCell(new Cell().add(new Paragraph(String.valueOf(order.getEmployeeId()))));

            detailsTable.addCell(new Cell().add(new Paragraph("Employee Name:").setBold()));
            detailsTable.addCell(new Cell().add(new Paragraph(employeeName)));

            detailsTable.addCell(new Cell().add(new Paragraph("Customer ID:").setBold()));
            detailsTable.addCell(new Cell().add(new Paragraph(String.valueOf(order.getCustomerId()))));

            detailsTable.addCell(new Cell().add(new Paragraph("Customer Name:").setBold()));
            detailsTable.addCell(new Cell().add(new Paragraph(customerName)));

            detailsTable.addCell(new Cell().add(new Paragraph("Order Date:").setBold()));
            detailsTable.addCell(new Cell().add(new Paragraph(String.valueOf(order.getOrderDate()))));

            detailsTable.addCell(new Cell().add(new Paragraph("Total Amount:").setBold()));
            detailsTable.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", order.getAmount()))));

            document.add(detailsTable);

            document.add(new Paragraph(" "));

            // Fetch items
            LinkedList orderDetailsList;
            try (Connection con = DBUtil.getConnection()) {
                OrderDAO orderDAO = new OrderDAO();
                orderDetailsList = orderDAO.getOrderDetails(con, order.getOrderId());
            }

            if (orderDetailsList != null && orderDetailsList.getSize() > 0) {
                document.add(new Paragraph("Items:")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setFontSize(14)
                        .setFontColor(ColorConstants.DARK_GRAY));

                Table itemsTable = new Table(new float[]{1, 3, 2, 1, 1, 1});
                itemsTable.setWidth(500);
                itemsTable.setMarginTop(10);
                itemsTable.setMarginBottom(20);
                itemsTable.setHorizontalAlignment(HorizontalAlignment.CENTER);

                addHeader(itemsTable, "Item ID");
                addHeader(itemsTable, "Item Name");
                addHeader(itemsTable, "Category");
                addHeader(itemsTable, "Qty");
                addHeader(itemsTable, "Unit");
                addHeader(itemsTable, "Line Total");

                boolean alternate = false;
                Node current = orderDetailsList.getFront();

                while (current != null) {
                    OrderDAO.OrderDetails od = (OrderDAO.OrderDetails) current.getElement();
                    addRowToTable(itemsTable, od, alternate);
                    alternate = !alternate;
                    current = current.getNext();
                }

                document.add(itemsTable);
            } else {
                document.add(new Paragraph("No items found for this order.")
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE))
                        .setFontSize(12)
                        .setFontColor(ColorConstants.GRAY));
            }

            // Footer
            document.add(new Paragraph("Thank you for your purchase!")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            document.close();

            File pdfFile = new File(filePath);
            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "Error");
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Error");
        }
    }

    private void addHeader(Table table, String text) {
        table.addHeaderCell(
                new Cell()
                        .setBackgroundColor(BRAND_GREEN)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6)
                        .add(new Paragraph(text).setBold())
        );
    }



    private void addRowToTable(Table table, OrderDAO.OrderDetails od, boolean alternate) {

        DeviceRgb bg = alternate ? ROW_ALT : LIGHT_GREEN;

        table.addCell(makeCell(String.valueOf(od.getProductId()), bg));
        table.addCell(makeCell(od.getProductName(), bg));
        table.addCell(makeCell(od.getCategoryName(), bg));
        table.addCell(makeCell(String.valueOf(od.getQuantity()), bg));
        table.addCell(makeCell(String.format("%.2f", od.getUnitPrice()), bg));
        table.addCell(makeCell(String.format("%.2f", od.getLineTotal()), bg));
    }


    private Cell makeCell(String text, DeviceRgb bg) {
        return new Cell()
                .setBackgroundColor(bg)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5)
                .add(new Paragraph(text));
    }


    private String getEmployeeName(Connection con, int employeeId) throws SQLException {
        String sql = "SELECT full_name FROM users WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("full_name");
                    return (name == null || name.trim().isEmpty()) ? "N/A" : name;
                }
            }
        }
        return "N/A";
    }


    private String getCustomerName(Connection con, int customerId) throws SQLException {
        String sql = "SELECT customer_name FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("customer_name");
                    return (name == null || name.trim().isEmpty()) ? "N/A" : name;
                }
            }
        }
        return "N/A";
    }

}
