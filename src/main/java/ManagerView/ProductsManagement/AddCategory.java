package ManagerView.ProductsManagement;

import Connection.CategoryDAO;
import Connection.DBUtil;
import com.example.comp333finalproj.UIHelperC;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;

public class AddCategory {
    private final Runnable onSuccessRefresh;

    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private VBox centerVbox;
    private HBox buttonsHbox;
    private GridPane grid;

    private Text addCategoryText;
    private Button cancelButton, addButton;

    private Text categoryNameText;
    private TextField categoryNameTextField;

    public AddCategory(Runnable onSuccessRefresh) {
        this.onSuccessRefresh = onSuccessRefresh;
        root = new BorderPane();

        centerVbox = new VBox();
        centerVbox.setAlignment(Pos.CENTER);
        centerVbox.setSpacing(15);

        addCategoryText = UIHelperC.createTitleText("Add Category");

        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        categoryNameText = UIHelperC.createInfoText("Category: ");
        categoryNameTextField = UIHelperC.createStyledTextField("category");


        grid.add(categoryNameText, 0, 0);
        grid.add(categoryNameTextField, 1, 0);

        buttonsHbox = new HBox();
        buttonsHbox.setAlignment(Pos.CENTER);
        buttonsHbox.setSpacing(15);
        buttonsHbox.setPadding(new Insets(20));

        addButton = UIHelperC.createStyledButton("Add");
        cancelButton = UIHelperC.createStyledButton("Cancel");
        buttonsHbox.getChildren().addAll(addButton, cancelButton);

        centerVbox.getChildren().addAll(addCategoryText, grid, buttonsHbox);

        root.setCenter(centerVbox);
        stage = new Stage();
        scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.setTitle("Add Category");

        cancelButton.setOnAction(e -> stage.close());
        addButton.setOnAction(e -> addAction());
        categoryNameTextField.setOnAction(e -> addAction());
    }

    public void showStage() {
        stage.show();
    }

    private void addAction() {
        String categoryName = categoryNameTextField.getText();
        if (categoryName.isEmpty()) {
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Enter category name");
            categoryNameTextField.requestFocus();
            return;
        }

        try (Connection con = DBUtil.getConnection()) {
            if (con == null) {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "Connection error");
                return;
            }

            CategoryDAO dao = new CategoryDAO();

            int id = dao.getOrCreateCategoryId(con, categoryName);

            if (id > 0) {
                if (onSuccessRefresh != null)
                    onSuccessRefresh.run();

                UIHelperC.showAlert(Alert.AlertType.INFORMATION, "Category added successfully");
                categoryNameTextField.clear();
                stage.close();
            } else {
                UIHelperC.showAlert(Alert.AlertType.ERROR, "failed to add category");
            }

        } catch (Exception e) {
            e.printStackTrace();
            UIHelperC.showAlert(Alert.AlertType.ERROR, "Error adding category");
        }
    }
}
