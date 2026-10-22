import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:cosmetics_vault.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        // Таблица А: Основная текстовая информация
        String createCosmeticsTable = """
            CREATE TABLE IF NOT EXISTS cosmetics (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                expiration_date TEXT,
                comment TEXT
            );
            """;

        // Таблица Б: Медиа-данные (Блобы для миниатюр и пути к оригиналам)
        // ON DELETE CASCADE означает: удалим помаду — картинка удалится сама!
        String createMediaTable = """
            CREATE TABLE IF NOT EXISTS cosmetics_media (
                cosmetic_id INTEGER PRIMARY KEY,
                preview_blob BLOB,
                high_res_path TEXT,
                FOREIGN KEY(cosmetic_id) REFERENCES cosmetics(id) ON DELETE CASCADE
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Включаем поддержку внешних ключей в SQLite принудительно
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Создаем обе таблицы
            stmt.execute(createCosmeticsTable);
            stmt.execute(createMediaTable);

            System.out.println("🐾 SQLite: Обе таблицы (cosmetics и cosmetics_media) успешно готовы!");
        } catch (SQLException e) {
            System.err.println("😿 Ошибка SQLite при создании структуры: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
