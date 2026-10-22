import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

public class Cosmetic {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty category;
    private final SimpleStringProperty expirationDate;
    private final SimpleStringProperty comment;
    private Image previewImage;
    private String highResPath;

    public Cosmetic(int id, String name, String category, String expirationDate, String comment, Image previewImage, String highResPath) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.expirationDate = new SimpleStringProperty(expirationDate);
        this.comment = new SimpleStringProperty(comment);
        this.previewImage = previewImage;
        this.highResPath = highResPath; // Инициализируем
    }

    public String getHighResPath() { return highResPath; }
    public Image getPreviewImage() { return previewImage; }
    public void setPreviewImage(Image previewImage) { this.previewImage = previewImage; }
    // Геттеры для JavaFX TableView
    public int getId() { return id.get(); }
    public SimpleIntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public SimpleStringProperty nameProperty() { return name; }

    public String getCategory() { return category.get(); }
    public SimpleStringProperty categoryProperty() { return category; }

    public String getExpirationDate() { return expirationDate.get(); }
    public SimpleStringProperty expirationDateProperty() { return expirationDate; }

    public String getComment() { return comment.get(); }
    public SimpleStringProperty commentProperty() { return comment; }
}
