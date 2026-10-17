import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Инициализируем базу данных SQLite при старте
        DatabaseManager.initializeDatabase();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cosmetica.fxml"));
            BorderPane root = loader.load();

            Scene scene = new Scene(root, 900, 600);

            if (getClass().getResource("/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }

            // Подключение ваших авторских иконок из Krita
            var res16 = getClass().getResource("/images/icon16.png");
            var res32 = getClass().getResource("/images/icon32.png");
            var res64 = getClass().getResource("/images/icon64.png");
            if (res16 != null && res32 != null && res64 != null) {
                primaryStage.getIcons().addAll(
                        new Image(res16.toExternalForm()),
                        new Image(res32.toExternalForm()),
                        new Image(res64.toExternalForm())
                );
            }

            primaryStage.setTitle("Учет косметики 💄");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("😿 Не удалось загрузить FXML файл!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
