package Product;

public class Product {
    private int id;
    private String itemName;
    private String categoryName;
    private double pricePerPiece;
    private int quantity;

    public Product(int id, String itemName, String categoryName, double pricePerPiece) {
        this.id = id;
        this.itemName = itemName;
        this.categoryName = categoryName;
        this.pricePerPiece = pricePerPiece;
        this.quantity = 1;
    }

    public int getId() { return id; }
    public String getItemName() { return itemName; }
    public String getCategoryName() { return categoryName; }
    public double getPricePerPiece() { return pricePerPiece; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() {
        return pricePerPiece * quantity;
    }
}
