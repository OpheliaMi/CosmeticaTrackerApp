import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

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

        // Устанавливаем дефолтные темы, шрифты и размеры при старте приложения
        Platform.runLater(() -> {
            setTheme("theme-rainbow");
            setFontClass("font-custom");
            setSizeClass("size-medium");
        });
        System.out.println("🐾 Контроллер успешно инициализирован, таблицы и стили готовы!");
    }

    // --- МЕХАНИЗМ СВЯЗИ КОЛОНОК С МОДЕЛЬЮ COSMETIC ---
    private void setupColumns(TableView<Cosmetic> table) {
        if (table == null) return;
        // 0 - фото, 1 - название, 2 - годен до, 3 - комент, 4 - действия
        TableColumn<Cosmetic, ?> nameCol = table.getColumns().get(1);
        TableColumn<Cosmetic, ?> dateCol = table.getColumns().get(2);
        TableColumn<Cosmetic, ?> commentCol = table.getColumns().get(3);

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
    }

    // --- ЗАГРУЗКА ИЗ БАЗЫ ДАННЫХ SQLite И РАСПРЕДЕЛЕНИЕ ПО ВКЛАДКАМ ---
    private void loadDataFromDatabase() {
        masterData.clear();
        String selectSQL = "SELECT * FROM cosmetics";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                masterData.add(new Cosmetic(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("expiration_date"),
                        rs.getString("comment")
                ));
            }
            System.out.println("🐾 Считано из SQLite базы. Всего позиций: " + masterData.size());

            // Обновляем отображение во всех таблицах на вкладках
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

    // --- ТЕСТОВОЕ ДОБАВЛЕНИЕ СЛУЧАЙНОГО ПРОДУКТА В SQLite ---
    @FXML
    private void handleAddProduct() {
        String[] names = {"Палетка Готика 🔮", "Помада Черничная 💄", "Румяна Неоновые ✨", "Контуринг Скульптор 🪐"};
        String[] categories = {"Тени", "Помады", "Румяна", "Контуринг"};

        int randIndex = (int) (Math.random() * names.length);
        String name = names[randIndex];
        String category = categories[randIndex];
        String date = "2028-10-31";
        String comment = "Связанные таблицы работают! Мур ✨";

        String insertCosmeticSQL = "INSERT INTO cosmetics(name, category, expiration_date, comment) VALUES(?,?,?,?)";
        String insertMediaSQL = "INSERT INTO cosmetics_media(cosmetic_id, preview_blob, high_res_path) VALUES(?,?,?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            // Включаем транзакцию, чтобы обе таблицы обновлялись одновременно и без сбоев
            conn.setAutoCommit(false);

            // 1. Сохраняем текстовую часть и просим базу вернуть сгенерированный ID
            try (PreparedStatement pstmt1 = conn.prepareStatement(insertCosmeticSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt1.setString(1, name);
                pstmt1.setString(2, category);
                pstmt1.setString(3, date);
                pstmt1.setString(4, comment);
                pstmt1.executeUpdate();

                // Получаем ID новой помады/теней
                try (ResultSet generatedKeys = pstmt1.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newCosmeticId = generatedKeys.getInt(1);

                        // 2. Создаем связанную строку во второй таблице cosmetics_media под этим же ID
                        try (PreparedStatement pstmt2 = conn.prepareStatement(insertMediaSQL)) {
                            pstmt2.setInt(1, newCosmeticId);
                            pstmt2.setNull(2, java.sql.Types.BLOB); // Пока картинки нет, пишем NULL

                            // Если это тени, готовим путь для хай-рес картинки на диске
                            if ("Тени".equalsIgnoreCase(category)) {
                                pstmt2.setString(3, "assets/high_res/shadow_" + newCosmeticId + ".png");
                            } else {
                                pstmt2.setNull(3, java.sql.Types.VARCHAR);
                            }

                            pstmt2.executeUpdate();
                        }
                    }
                }
            }

            // Подтверждаем сохранение в обе таблицы
            conn.commit();
            System.out.println("✨ Успех! Данные записаны в cosmetics, связь создана в cosmetics_media!");

            // Перезагружаем интерфейс
            loadDataFromDatabase();

        } catch (SQLException e) {
            System.err.println("😿 Ошибка сложного сохранения в БД: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
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
}
