import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainController {

    // Подключаем все наши таблицы из FXML по их fx:id
    @FXML private TableView<Cosmetic> tableAll;
    @FXML private TableView<Cosmetic> tableShadows;
    @FXML private TableView<Cosmetic> tableContour;
    @FXML private TableView<Cosmetic> tableHighlighters;
    @FXML private TableView<Cosmetic> tableBlush;
    @FXML private TableView<Cosmetic> tableLipsticks;
    @FXML private TableView<Cosmetic> tableOther;

    // Главный список, где хранится вся косметика в памяти приложения
    private final ObservableList<Cosmetic> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Настраиваем привязку колонок для всех 7 таблиц
        setupColumns(tableAll);
        setupColumns(tableShadows);
        setupColumns(tableContour);
        setupColumns(tableHighlighters);
        setupColumns(tableBlush);
        setupColumns(tableLipsticks);
        setupColumns(tableOther);

        // Загружаем данные из файла SQLite
        loadDataFromDatabase();

        setupShadowsClickWindow();

        // Устанавливаем дефолтные темы, шрифты и размеры при старте приложения
        Platform.runLater(() -> {
            setTheme("theme-rainbow");
            setFontClass("font-custom");
            setSizeClass("size-medium");
            if (tableAll.getScene() != null) {
                var menuBarNode = tableAll.getScene().lookup("MenuBar");
                if (menuBarNode instanceof MenuBar) {
                    bindContextMenuThemes((MenuBar) menuBarNode);
                }
            }
        });
        System.out.println("🐾 Контроллер успешно инициализирован, таблицы и стили готовы!");
    }

    // --- МЕХАНИЗМ СВЯЗИ КОЛОНОК С МОДЕЛЬЮ COSMETIC ---

    private void setupColumns(TableView<Cosmetic> table) {
        if (table == null) return;

        // Ссылки на колонки из FXML по индексам
        TableColumn<Cosmetic, Image> photoCol = (TableColumn<Cosmetic, Image>) table.getColumns().get(0);
        TableColumn<Cosmetic, String> nameCol = (TableColumn<Cosmetic, String>) table.getColumns().get(1);
        TableColumn<Cosmetic, String> dateCol = (TableColumn<Cosmetic, String>) table.getColumns().get(2);
        TableColumn<Cosmetic, String> commentCol = (TableColumn<Cosmetic, String>) table.getColumns().get(3);

        // --- НАСТРОЙКА АВТОПЕРЕНblock ДЛЯ КОЛОНКИ "НАЗВАНИЕ" ---
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(tc -> new TableCell<>() {
            private final Text text = new Text();
            {
                // Связываем ширину переноса с шириной колонки (минус отступы)
                text.wrappingWidthProperty().bind(nameCol.widthProperty().subtract(15));
                // Подключаем стили шрифтов к внутреннему текстовому объекту
                text.styleProperty().bind(styleProperty());
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    // Переносим динамический класс заливки текста для фикса выделения
                    text.getStyleClass().setAll("text");
                    setGraphic(text);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                }
            }
        });

        // --- НАСТРОЙКА АВТОПЕРЕНblock ДЛЯ КОЛОНКИ "КОММЕНТАРИЙ" ---
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentCol.setCellFactory(tc -> new TableCell<>() {
            private final Text text = new Text();
            {
                text.wrappingWidthProperty().bind(commentCol.widthProperty().subtract(15));
                text.styleProperty().bind(styleProperty());
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    text.getStyleClass().setAll("text");
                    setGraphic(text);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                }
            }
        });

        // Логика форматирования даты
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter uiFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        dateCol.setCellValueFactory(cellData -> {
            String rawDate = cellData.getValue().getExpirationDate();
            if (rawDate == null || rawDate.trim().isEmpty()) {
                return new SimpleStringProperty("");
            }
            try {
                LocalDate date = LocalDate.parse(rawDate, dbFormatter);
                return new SimpleStringProperty(date.format(uiFormatter));
            } catch (Exception e) {
                return new SimpleStringProperty(rawDate);
            }
        });

        // Делаем так, чтобы текст даты тоже наследовал класс "text" для фикса выделения
        dateCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("text");
                }
            }
        });

        // Фабрика и кастомный рендеринг для Фото товара
        photoCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPreviewImage()));
        photoCol.setCellFactory(column -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            @Override
            protected void updateItem(Image image, boolean empty) {
                super.updateItem(image, empty);
                if (empty || image == null) {
                    setGraphic(null);
                } else {
                    iv.setImage(image);
                    iv.setFitWidth(50);
                    iv.setFitHeight(50);
                    iv.setPreserveRatio(true);
                    setGraphic(iv);
                }
            }
        });
    }

    // --- ЗАГРУЗКА ИЗ БАЗЫ ДАННЫХ SQLite И РАСПРЕДЕЛЕНИЕ ПО ВКЛАДКАМ ---
    private void loadDataFromDatabase() {
        masterData.clear();
        // Добавили m.high_res_path в запрос
        String selectSQL = """
        SELECT c.*, m.preview_blob, m.high_res_path 
        FROM cosmetics c 
        LEFT JOIN cosmetics_media m ON c.id = m.cosmetic_id
        """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                Image img = null;
                byte[] bytes = rs.getBytes("preview_blob");

                if (bytes != null && bytes.length > 0) {
                    img = new Image(new ByteArrayInputStream(bytes));
                }

                masterData.add(new Cosmetic(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("expiration_date"),
                        rs.getString("comment"),
                        img,
                        rs.getString("high_res_path") // Передаем путь к оригиналу
                ));
            }
            System.out.println("🐾 Считано из SQLite базы. Всего позиций: " + masterData.size());
            updateTableViews();

        } catch (SQLException e) {
            System.err.println("😿 Не удалось загрузить данные из базы SQLite: " + e.getMessage());
        }
    }


    private void updateTableViews() {
        tableAll.setItems(masterData);

        tableShadows.setItems(masterData.filtered(c -> "Тени".equalsIgnoreCase(c.getCategory())));
        tableContour.setItems(masterData.filtered(c -> "Контуринг".equalsIgnoreCase(c.getCategory())));
        tableHighlighters.setItems(masterData.filtered(c -> "Хайлайтеры".equalsIgnoreCase(c.getCategory())));
        tableBlush.setItems(masterData.filtered(c -> "Румяна".equalsIgnoreCase(c.getCategory())));
        tableLipsticks.setItems(masterData.filtered(c -> "Помады".equalsIgnoreCase(c.getCategory())));
        tableOther.setItems(masterData.filtered(c -> "Иное".equalsIgnoreCase(c.getCategory())));
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_product.fxml"));
            GridPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Добавить косметику в коллекцию 🌸");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableAll.getScene().getWindow());

            Scene scene = new Scene(page);
            if (tableAll.getScene().getStylesheets().size() > 0) {
                scene.getStylesheets().addAll(tableAll.getScene().getStylesheets());
            }
            dialogStage.setScene(scene);

            AddProductController controller = loader.getController();
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                String name = controller.getName();
                String category = controller.getCategory();
                String date = controller.getExpirationDate();
                String comment = controller.getComment();
                File imageFile = controller.getSelectedImageFile(); // Получаем файл

                saveProductToDatabase(name, category, date, comment, imageFile);
            }

        } catch (IOException e) {
            System.err.println("😿 Не удалось открыть окно добавления косметики!");
            e.printStackTrace();
        }
    }

    private void saveProductToDatabase(String name, String category, String date, String comment, File imageFile) {
        String insertCosmeticSQL = "INSERT INTO cosmetics(name, category, expiration_date, comment) VALUES(?,?,?,?)";
        String insertMediaSQL = "INSERT INTO cosmetics_media(cosmetic_id, preview_blob, high_res_path) VALUES(?,?,?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Стартуем транзакцию

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertCosmeticSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt1.setString(1, name);
                pstmt1.setString(2, category);
                pstmt1.setString(3, date);
                pstmt1.setString(4, comment);
                pstmt1.executeUpdate();

                try (ResultSet generatedKeys = pstmt1.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newCosmeticId = generatedKeys.getInt(1);
                        String calculatedHighResPath = null;

                        // --- ЛОГИКА ДЛЯ ТЕНЕЙ: КОПИРОВАНИЕ НА ДИСК ---
                        if ("Тени".equalsIgnoreCase(category)) {
                            if (imageFile != null && imageFile.exists()) {
                                try {
                                    // 1. Создаем целевую директорию assets/high_res в корне проекта, если её нет
                                    Path dirPath = Paths.get("assets", "high_res");
                                    if (!Files.exists(dirPath)) {
                                        Files.createDirectories(dirPath);
                                    }

                                    // 2. Вычисляем расширение исходного файла (.png, .jpg и т.д.)
                                    String extension = "";
                                    String fileName = imageFile.getName();
                                    int i = fileName.lastIndexOf('.');
                                    if (i > 0) {
                                        extension = fileName.substring(i);
                                    } else {
                                        extension = ".png"; // Дефолтное расширение, если его нет
                                    }

                                    // 3. Формируем красивый результирующий путь: assets/high_res/shadow_ID.ext
                                    Path targetPath = dirPath.resolve("shadow_" + newCosmeticId + extension);

                                    // 4. Копируем файл на диск (с заменой, если вдруг файл уже существовал)
                                    Files.copy(imageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                                    // Сохраняем строку пути для записи в базу данных SQLite (используем прямой слэш для совместимости)
                                    calculatedHighResPath = "assets/high_res/shadow_" + newCosmeticId + extension;
                                    System.out.println("📸 Фото теней успешно скопировано на диск: " + targetPath.toAbsolutePath());

                                } catch (Exception e) {
                                    System.err.println("😿 Не удалось скопировать файл картинки на диск: " + e.getMessage());
                                    // Не прерываем транзакцию, товар всё равно запишется, но выведем ошибку в консоль
                                }
                            }
                        }

                        // 2. Создаем запись во второй таблице (cosmetics_media)
                        try (PreparedStatement pstmt2 = conn.prepareStatement(insertMediaSQL)) {
                            pstmt2.setInt(1, newCosmeticId);

                            // Запись BLOB для миниатюры в таблице (работает для всех категорий)
                            if (imageFile != null && imageFile.exists()) {
                                try (FileInputStream fis = new FileInputStream(imageFile)) {
                                    pstmt2.setBinaryStream(2, fis, (int) imageFile.length());
                                }
                            } else {
                                pstmt2.setNull(2, java.sql.Types.BLOB);
                            }

                            // Запись пути к оригиналу высокой четкости (только для теней, для остальных — NULL)
                            if (calculatedHighResPath != null) {
                                pstmt2.setString(3, calculatedHighResPath);
                            } else {
                                pstmt2.setNull(3, java.sql.Types.VARCHAR);
                            }

                            pstmt2.executeUpdate();
                        }
                    }
                }
            }

            conn.commit(); // Подтверждаем транзакцию в БД
            System.out.println("✨ Успех! Товар добавлен, связь в БД и файл на диске синхронизированы.");

            // Обновляем таблицы в интерфейсе
            loadDataFromDatabase();

        } catch (Exception e) {
            System.err.println("😿 Ошибка транзакции при сохранении: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }


    private void setupShadowsClickWindow() {
        if (tableShadows == null) return;

        tableShadows.setRowFactory(tv -> {
            TableRow<Cosmetic> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Проверяем: двойной клик И строка не пустая
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Cosmetic clickedCosmetic = row.getItem();

                    // Вызываем метод открытия большого фото
                    openHighResImageWindow(clickedCosmetic);
                }
            });
            return row;
        });
    }

    private void openHighResImageWindow(Cosmetic cosmetic) {
        String path = cosmetic.getHighResPath();

        // Проверяем, задан ли путь в БД
        if (path == null || path.trim().isEmpty()) {
            showNoImageAlert("У этого товара нет ссылки на оригинальное фото.");
            return;
        }

        File file = new File(path);
        // Проверяем, существует ли физически файл на диске
        if (!file.exists()) {
            showNoImageAlert("Файл не найден по пути: " + file.getAbsolutePath() +
                    "\nУбедитесь, что папка assets/high_res создана и файл существует.");
            return;
        }

        // Создаем динамическое модальное окно для отображения большого фото
        try {
            Image highResImage = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(highResImage);

            // Ограничиваем максимальный размер окна под экран, сохраняя пропорции
            imageView.setFitWidth(600);
            imageView.setFitHeight(600);
            imageView.setPreserveRatio(true);

            StackPane root = new StackPane(imageView);
            root.setStyle("-fx-background-color: #121212; -fx-padding: 10;"); // Красивая темная подложка

            Stage imageStage = new Stage();
            imageStage.setTitle("Просмотр оригинала: " + cosmetic.getName() + " 🔮");
            imageStage.initModality(Modality.APPLICATION_MODAL);
            imageStage.initOwner(tableShadows.getScene().getWindow());

            Scene scene = new Scene(root);
            imageStage.setScene(scene);
            imageStage.show();

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке тяжелого изображения: " + e.getMessage());
        }
    }

    private void showNoImageAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText("Не удалось открыть оригинал теней");
        alert.setContentText(message);
        alert.showAndWait();
    }



    // --- КНОПКА «ВЫХОД» ---
    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }

    // ==========================================================================
    // 🔮 ЛОГИКА ПЕРЕКЛЮЧЕНИЯ 19 ТЕМ ОФОРМЛЕНИЯ
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

    // Обработчики для Пастельных тем (onAction из FXML)
    @FXML private void setRainbowTheme() { setTheme("theme-rainbow"); }
    @FXML private void setPinkTheme() { setTheme("theme-pink"); }
    @FXML private void setVintageTheme() { setTheme("theme-vintage"); }
    @FXML private void setCottonTheme() { setTheme("theme-cotton"); }
    @FXML private void setWinterTheme() { setTheme("theme-winter"); }
    @FXML private void setGhostTheme() { setTheme("theme-ghost"); }

    // Обработчики для тем Неон и Готика (onAction из FXML)
    @FXML private void setCyberTheme() { setTheme("theme-cyber"); }
    @FXML private void setSambucusTheme() { setTheme("theme-sambucus"); }
    @FXML private void setMintTheme() { setTheme("theme-mint"); }
    @FXML private void setViperTheme() { setTheme("theme-viper"); }
    @FXML private void setSucculentTheme() { setTheme("theme-succulent"); }
    @FXML private void setBeetleTheme() { setTheme("theme-beetle"); }

    // Обработчики для Spooky Halloween тем (onAction из FXML)
    @FXML private void setSamhainTheme() { setTheme("theme-samhain"); }
    @FXML private void setHalloweenTheme() { setTheme("theme-halloween"); }
    @FXML private void setSyrupTheme() { setTheme("theme-syrup"); }
    @FXML private void setSpookyTheme() { setTheme("theme-spooky"); }
    @FXML private void setShimmerTheme() { setTheme("theme-shimmer"); }
    @FXML private void setMistTheme() { setTheme("theme-mist"); }
    @FXML private void setPurpleTheme() { setTheme("theme-purple"); }

    // ==========================================================================
    // 🌸 ДИНАМИЧЕСКИЕ ШРИФТЫ
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
    // 🔎 ДИНАМИЧЕСКИЕ РАЗМЕРЫ ТЕКСТА
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

    private void bindContextMenuThemes(MenuBar menuBar) {
        if (menuBar == null) return;

        // Полный список всех классов тем приложения для очистки и перезаписи
        String[] allThemes = {
                "theme-rainbow", "theme-pink", "theme-vintage", "theme-cotton", "theme-winter", "theme-ghost",
                "theme-cyber", "theme-sambucus", "theme-mint", "theme-viper", "theme-succulent", "theme-beetle",
                "theme-samhain", "theme-halloween", "theme-syrup", "theme-spooky", "theme-shimmer", "theme-mist", "theme-purple"
        };

        // Обходим все главные вкладки панели меню ("Меню", "Темы"...)
        for (Menu menu : menuBar.getMenus()) {

            // Подписываемся на триггер открытия меню
            menu.showingProperty().addListener((obs, wasShowing, isShowing) -> {
                if (isShowing && menu.getParentPopup() != null) {
                    // Вычисляем, какая тема сейчас активна на главном экране
                    var mainRoot = tableAll.getScene().getRoot();
                    String activeTheme = "theme-rainbow"; // дефолт
                    for (String theme : allThemes) {
                        if (mainRoot.getStyleClass().contains(theme)) {
                            activeTheme = theme;
                            break;
                        }
                    }

                    // Находим окно выпадающего списка и принудительно красим в цвет темы
                    var popup = menu.getParentPopup();
                    popup.getStyleClass().removeAll(allThemes);
                    popup.getStyleClass().add(activeTheme);
                }
            });

            // Запускаем рекурсивный фикс для всех внутренних подменю (Пастельные, Неон и т.д.)
            bindSubMenus(menu, allThemes);
        }
    }

    // Вспомогательный метод для глубокой обработки раскрывающихся списков внутри меню
    private void bindSubMenus(Menu parentMenu, String[] allThemes) {
        for (MenuItem item : parentMenu.getItems()) {
            if (item instanceof Menu) {
                Menu subMenu = (Menu) item;

                subMenu.showingProperty().addListener((obs, wasShowing, isShowing) -> {
                    if (isShowing && subMenu.getParentPopup() != null) {
                        var mainRoot = tableAll.getScene().getRoot();
                        String activeTheme = "theme-rainbow";
                        for (String theme : allThemes) {
                            if (mainRoot.getStyleClass().contains(theme)) {
                                activeTheme = theme;
                                break;
                            }
                        }

                        // Красим само всплывающее окно этого подменю
                        var popup = subMenu.getParentPopup();
                        popup.getStyleClass().removeAll(allThemes);
                        popup.getStyleClass().add(activeTheme);
                    }
                });

                // Идём дальше вниз по дереву иерархии меню
                bindSubMenus(subMenu, allThemes);
            }
        }
    }
}
