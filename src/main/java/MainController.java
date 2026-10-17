import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class MainController {

    // Связываем главную таблицу для управления стилями всего окна
    @FXML private TableView<?> tableAll;

    @FXML
    public void initialize() {
        // Устанавливаем дефолтные настройки при старте приложения
        Platform.runLater(() -> {
            setTheme("theme-rainbow");
            setFontClass("font-custom");
            setSizeClass("size-medium");
        });
        System.out.println("🐾 Контроллер успешно инициализирован, стили готовы!");
    }

    // ==========================================================================
    // 🔮 УПРАВЛЕНИЕ 19 ТЕМАМИ ОФОРМЛЕНИЯ
    // ==========================================================================
    private void setTheme(String themeClassName) {
        if (tableAll != null && tableAll.getScene() != null) {
            var root = tableAll.getScene().getRoot();
            root.getStyleClass().removeAll(
                    "theme-rainbow", "theme-pink", "theme-vintage", "theme-cotton", "theme-winter", "theme-ghost",
                    "theme-cyber", "theme-sambucus", "theme-mint", "theme-viper", "theme-succulent", "theme-beetle",
                    "theme-samhain", "theme-halloween", "theme-syrup", "theme-spooky", "theme-shimmer", "theme-mist", "theme-purple"
            );
            root.getStyleClass().add(themeClassName);
        }
    }

    @FXML private void setRainbowTheme() { setTheme("theme-rainbow"); }
    @FXML private void setPinkTheme() { setTheme("theme-pink"); }
    @FXML private void setVintageTheme() { setTheme("theme-vintage"); }
    @FXML private void setCottonTheme() { setTheme("theme-cotton"); }
    @FXML private void setWinterTheme() { setTheme("theme-winter"); }
    @FXML private void setGhostTheme() { setTheme("theme-ghost"); }
    @FXML private void setCyberTheme() { setTheme("theme-cyber"); }
    @FXML private void setSambucusTheme() { setTheme("theme-sambucus"); }
    @FXML private void setMintTheme() { setTheme("theme-mint"); }
    @FXML private void setViperTheme() { setTheme("theme-viper"); }
    @FXML private void setSucculentTheme() { setTheme("theme-succulent"); }
    @FXML private void setBeetleTheme() { setTheme("theme-beetle"); }
    @FXML private void setSamhainTheme() { setTheme("theme-samhain"); }
    @FXML private void setHalloweenTheme() { setTheme("theme-halloween"); }
    @FXML private void setSyrupTheme() { setTheme("theme-syrup"); }
    @FXML private void setSpookyTheme() { setTheme("theme-spooky"); }
    @FXML private void setShimmerTheme() { setTheme("theme-shimmer"); }
    @FXML private void setMistTheme() { setTheme("theme-mist"); }
    @FXML private void setPurpleTheme() { setTheme("theme-purple"); }

    // ==========================================================================
    // 🌸 УПРАВЛЕНИЕ ДИНАМИЧЕСКИМИ ШРИФТАМИ (onAction из FXML)
    // ==========================================================================
    private void setFontClass(String fontClassName) {
        if (tableAll != null && tableAll.getScene() != null) {
            var root = tableAll.getScene().getRoot();
            root.getStyleClass().removeAll("font-custom", "font-modern", "font-system");
            root.getStyleClass().add(fontClassName);
        }
    }

    @FXML private void setFontCustom() { setFontClass("font-custom"); }
    @FXML private void setFontModern() { setFontClass("font-modern"); }
    @FXML private void setFontSystem() { setFontClass("font-system"); }

    // ==========================================================================
    // 🔎 УПРАВЛЕНИЕ РАЗМЕРАМИ ТЕКСТА (onAction из FXML)
    // ==========================================================================
    private void setSizeClass(String sizeClassName) {
        if (tableAll != null && tableAll.getScene() != null) {
            var root = tableAll.getScene().getRoot();
            root.getStyleClass().removeAll("size-small", "size-medium", "size-large");
            root.getStyleClass().add(sizeClassName);
        }
    }

    @FXML private void setSizeSmall() { setSizeClass("size-small"); }
    @FXML private void setSizeMedium() { setSizeClass("size-medium"); }
    @FXML private void setSizeLarge() { setSizeClass("size-large"); }
}
