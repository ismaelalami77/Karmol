package Connection;

public class OrderDetails {
    private final int productId;
    private final String productName;
    private final String categoryName;
    private final int quantity;
    private final double unitPrice;
    private final double lineTotal;

    public OrderDetails(int productId, String productName, String categoryName,
                        int quantity, double unitPrice, double lineTotal) {
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }
}