import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // ==========================================================================
// ==========================================================================
// 🔮 РЕГИСТРИРУЕМ ШРИФТЫ ИЗ РЕСУРСОВ (С ТОЧНЫМИ ИМЕНАМИ ФАЙЛОВ!)
// ==========================================================================
        var fontUrl1 = getClass().getResource("/fonts/TagesschriftCyrillic-Regular.ttf");
        var fontUrl2 = getClass().getResource("/fonts/Senotakaiguriddo-Regular.otf");

        if (fontUrl1 != null) {
            Font.loadFont(fontUrl1.toExternalForm(), 12);
            System.out.println("🐾 Шрифт Tagesschrift успешно загружен в память!");
        } else {
            System.err.println("😿 Ошибка: Файл TagesschriftCyrillic-Regular.ttf не найден!");
        }

        if (fontUrl2 != null) {
            Font.loadFont(fontUrl2.toExternalForm(), 12);
            System.out.println("🐾 Шрифт Senotakaiguriddo успешно загружен в память!");
        } else {
            System.err.println("😿 Ошибка: Файл Senotakaiguriddo-Regular.otf не найден!");
        }
// ==

        try {
            // Загружаем макет из папки src/main/resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cosmetica.fxml"));
            BorderPane root = loader.load();

            // Создаем сцену и задаем размеры окна
            Scene scene = new Scene(root, 900, 600);

            // NEWWWWW   3. Подключаем файл стилей (СТРОГО после создания scene!)
            if (getClass().getResource("/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            } else {
                System.err.println("😿 Предупреждение: Файл style.css не найден в ресурсах!");
            }

            // ==========================================================================
            // 🌸 ПОДКЛЮЧАЕМ ТВОИ АВТОРСКИЕ ИКОНКИ ДЛЯ ОКНА И ПАНЕЛИ ЗАДАЧ
            // ==========================================================================
            var res16 = getClass().getResource("/images/icon16.png");
            var res32 = getClass().getResource("/images/icon32.png");
            var res64 = getClass().getResource("/images/icon64.png");

            if (res16 != null && res32 != null && res64 != null) {
                primaryStage.getIcons().addAll(
                        new Image(res16.toExternalForm()),
                        new Image(res32.toExternalForm()),
                        new Image(res64.toExternalForm())
                );
                System.out.println("🐾 Успешно загружены иконки для окна и панели задач!");
            } else {
                System.err.println("😿 Предупреждение: Проверь, все ли три файла лежат в resources/images/");
            }


            primaryStage.setTitle("Учет косметики");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("😿 Не удалось загрузить FXML файл! Проверьте путь.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
