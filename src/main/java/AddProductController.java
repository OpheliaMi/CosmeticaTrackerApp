import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class AddProductController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private DatePicker datePicker;
    @FXML private ImageView imageView; // Линкуем ImageView
    @FXML private TextArea commentArea;

    private boolean saveClicked = false;
    private File selectedImageFile = null; // Здесь храним файл выбранного фото

    @FXML
    public void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList(
                "Тени", "Контуринг", "Хайлайтеры", "Румяна", "Помады", "Иное"
        ));
        categoryBox.setValue("Иное");
    }

    // Обработчик нажатия на кнопку "Загрузить фото"
    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбрать фотографию косметики");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) nameField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            // Отображаем выбранное фото в интерфейсе окна добавления
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
        }
    }

    public boolean isSaveClicked() { return saveClicked; }
    public File getSelectedImageFile() { return selectedImageFile; } // Геттер для файла

    public String getName() { return nameField.getText().trim(); }
    public String getCategory() { return categoryBox.getValue(); }
    public String getComment() { return commentArea.getText().trim(); }
    public String getExpirationDate() {
        if (datePicker.getValue() == null) return "";
        return datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @FXML
    private void handleSave() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение");
            alert.setHeaderText("Поле пустое");
            alert.setContentText("Пожалуйста, введите название косметики!");
            alert.showAndWait();
            return;
        }
        saveClicked = true;
        closeWindow();
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}
