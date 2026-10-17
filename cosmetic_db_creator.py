import sqlite3
import os

# Имя файла нашей базы данных
DB_NAME = "cosmetics_vault.db"

def create_database():
    # Проверяем, существует ли база, чтобы случайно не перезаписать
    if os.path.exists(DB_NAME):
        print(f"🐾 База данных '{DB_NAME}' уже существует!")
        return

    print("🐾 Создаю базу данных для учета косметики...")
    
    # Подключаемся к базе (если файла нет, sqlite создаст его автоматически)
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()

    try:
        # Включаем поддержку внешних ключей (FOREIGN KEY) в SQLite. 
        # Это нужно, чтобы работало каскадное удаление ON DELETE CASCADE.
        cursor.execute("PRAGMA foreign_keys = ON;")

        # 1. Создаем таблицу с основной информацией
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS cosmetics (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            brand TEXT,
            category TEXT NOT NULL,
            description TEXT,
            expiration_date TEXT  -- Сроки годности в формате YYYY-MM-DD
        );
        """)

        # 2. Создаем таблицу для медиа-данных (блобы и пути к оригиналам)
        cursor.execute("""
        CREATE TABLE IF NOT EXISTS cosmetics_media (
            cosmetic_id INTEGER PRIMARY KEY,
            preview_blob BLOB,      -- Сжатая миниатюра 400x400 для всех
            high_res_path TEXT,     -- Путь к оригиналу на диске (для теней)
            FOREIGN KEY(cosmetic_id) REFERENCES cosmetics(id) ON DELETE CASCADE
        );
        """)

        # Сохраняем изменения
        conn.commit()
        print(f"✨ База данных успешно создана и сохранена в файл: {DB_NAME}")
        print("Таблицы 'cosmetics' и 'cosmetics_media' готовы к работе!")

    except sqlite3.Error as e:
        print(f"😿 Ошибка при создании базы данных: {e}")
        conn.rollback()
        
    finally:
        # Всегда закрываем соединение
        conn.close()

if __name__ == "__main__":
    create_database()
