-- Vaultly MySQL schema
-- Generated from the current project features in:
-- - authentication
-- - transaction management
-- - budget planning
-- - financial reports
-- - deficit detection

CREATE DATABASE IF NOT EXISTS vaultly
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE vaultly;

SET NAMES utf8mb4;

-- =========================================================
-- TABLE: users
-- Purpose:
-- Stores application users for register/login features.
-- =========================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key user',
    full_name VARCHAR(100) NOT NULL COMMENT 'Nama lengkap pengguna',
    email VARCHAR(255) NOT NULL COMMENT 'Email unik untuk login',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Hash password pengguna',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Waktu akun dibuat',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Waktu akun terakhir diperbarui',
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB COMMENT='Data master pengguna Vaultly';

-- =========================================================
-- TABLE: transactions
-- Purpose:
-- Stores income and expense transactions used by:
-- - home balance summary
-- - transaction list/detail/create/edit/delete
-- - monthly deficit detection
-- - financial reports by period and category
-- =========================================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key transaksi',
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'Relasi ke pemilik transaksi',
    title VARCHAR(150) NOT NULL COMMENT 'Judul transaksi',
    amount DECIMAL(15,2) NOT NULL COMMENT 'Nominal transaksi',
    category VARCHAR(50) NOT NULL COMMENT 'Kategori transaksi sesuai pilihan aplikasi',
    transaction_type ENUM('INCOME', 'EXPENSE') NOT NULL COMMENT 'Jenis transaksi',
    transaction_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Tanggal/waktu transaksi untuk laporan dan analisis defisit',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Waktu data dibuat',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Waktu data terakhir diperbarui',
    PRIMARY KEY (id),
    KEY idx_transactions_user_id (user_id),
    KEY idx_transactions_user_date (user_id, transaction_at),
    KEY idx_transactions_user_category (user_id, category),
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_transactions_amount
        CHECK (amount > 0)
) ENGINE=InnoDB COMMENT='Data pemasukan dan pengeluaran pengguna';

-- =========================================================
-- TABLE: budgets
-- Purpose:
-- Stores budget targets per user and category for:
-- - budget planning
-- - budget vs actual reporting
-- Note:
-- "spent" is not stored because it is derived from expense transactions.
-- =========================================================
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key budget',
    user_id BIGINT UNSIGNED NOT NULL COMMENT 'Relasi ke pemilik budget',
    category VARCHAR(50) NOT NULL COMMENT 'Kategori budget',
    amount DECIMAL(15,2) NOT NULL COMMENT 'Target nominal budget per kategori',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Waktu budget dibuat',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Waktu budget terakhir diperbarui',
    PRIMARY KEY (id),
    UNIQUE KEY uq_budgets_user_category (user_id, category),
    KEY idx_budgets_user_id (user_id),
    CONSTRAINT fk_budgets_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_budgets_amount
        CHECK (amount > 0)
) ENGINE=InnoDB COMMENT='Target anggaran per kategori untuk tiap pengguna';

-- =========================================================
-- DATA DICTIONARY SUMMARY
-- =========================================================
-- users
-- - id            : BIGINT, PK
-- - full_name     : nama user
-- - email         : email login, unique
-- - password_hash : password yang sudah di-hash
-- - created_at    : waktu insert
-- - updated_at    : waktu update
--
-- transactions
-- - id               : BIGINT, PK
-- - user_id          : FK ke users.id
-- - title            : judul transaksi
-- - amount           : nominal > 0
-- - category         : kategori transaksi
-- - transaction_type : INCOME / EXPENSE
-- - transaction_at   : tanggal transaksi
-- - created_at       : waktu insert
-- - updated_at       : waktu update
--
-- budgets
-- - id         : BIGINT, PK
-- - user_id    : FK ke users.id
-- - category   : kategori budget
-- - amount     : target budget > 0
-- - created_at : waktu insert
-- - updated_at : waktu update
