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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class OrderHistory {

    public static TableView<Order> ordersHistoryTableView;
    public static ArrayList<Order> ordersArrayList = new ArrayList<>();
    public static ObservableList<Order> ordersObservableList = FXCollections.observableArrayList();

    private final BorderPane root;
    private final VBox centerVbox;
    private final Text titleText;

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

            // Details table
            Table detailsTable = new Table(new float[]{1, 2});
            detailsTable.setHorizontalAlignment(HorizontalAlignment.CENTER);

            detailsTable.addCell(new Cell().add(new Paragraph("Employee ID:").setBold()));
            detailsTable.addCell(new Cell().add(new Paragraph(String.valueOf(order.getEmployeeId()))));

            detailsTable.addCell(new Cell().add(new Paragraph("Customer ID:").setBold()));
            detailsTable.addCell(new Cell().add(new Paragraph(String.valueOf(order.getCustomerId()))));

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

    private void addHeader(Table table, String text) throws IOException {
        table.addHeaderCell(new Cell()
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setFontColor(ColorConstants.WHITE)
                .add(new Paragraph(text).setBold())
        );
    }

    private void addRowToTable(Table table, OrderDAO.OrderDetails od, boolean alternate) {
        DeviceRgb alt = new DeviceRgb(240, 240, 240);
        DeviceRgb normal = new DeviceRgb(255, 255, 255);

        Cell c1 = new Cell().add(new Paragraph(String.valueOf(od.getProductId())));
        Cell c2 = new Cell().add(new Paragraph(od.getProductName()));
        Cell c3 = new Cell().add(new Paragraph(od.getCategoryName()));
        Cell c4 = new Cell().add(new Paragraph(String.valueOf(od.getQuantity())));
        Cell c5 = new Cell().add(new Paragraph(String.format("%.2f", od.getUnitPrice())));
        Cell c6 = new Cell().add(new Paragraph(String.format("%.2f", od.getLineTotal())));

        if (alternate) {
            c1.setBackgroundColor(alt);
            c2.setBackgroundColor(alt);
            c3.setBackgroundColor(alt);
            c4.setBackgroundColor(alt);
            c5.setBackgroundColor(alt);
            c6.setBackgroundColor(alt);
        } else {
            c1.setBackgroundColor(normal);
            c2.setBackgroundColor(normal);
            c3.setBackgroundColor(normal);
            c4.setBackgroundColor(normal);
            c5.setBackgroundColor(normal);
            c6.setBackgroundColor(normal);
        }

        table.addCell(c1);
        table.addCell(c2);
        table.addCell(c3);
        table.addCell(c4);
        table.addCell(c5);
        table.addCell(c6);
    }
}
