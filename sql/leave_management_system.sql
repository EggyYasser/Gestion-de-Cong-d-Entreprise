
CREATE DATABASE IF NOT EXISTS leave_management_system
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE leave_management_system;

SET NAMES utf8mb4;

DROP TABLE IF EXISTS leave_history;
DROP TABLE IF EXISTS leave_balances;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS leave_types;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS admins;

CREATE TABLE admins (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
);

CREATE TABLE employees (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_code VARCHAR(50) NOT NULL UNIQUE,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  birth_date DATE,
  hire_date DATE,
  department VARCHAR(100),
  position VARCHAR(100),
  email VARCHAR(150) UNIQUE,
  phone VARCHAR(30),
  status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE leave_types (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  max_days INT NOT NULL
);

CREATE TABLE leave_requests (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_id BIGINT NOT NULL,
  leave_type_id BIGINT NOT NULL,
  processed_by BIGINT NULL,
  request_date DATE NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  reason TEXT,
  attachment_path VARCHAR(255) NULL,
  status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
  rejection_comment TEXT,
  CONSTRAINT fk_leave_request_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  CONSTRAINT fk_leave_request_type
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id)
      ON DELETE RESTRICT
      ON UPDATE CASCADE,
  CONSTRAINT fk_leave_request_admin
    FOREIGN KEY (processed_by) REFERENCES admins(id)
      ON DELETE SET NULL
      ON UPDATE CASCADE
);

CREATE TABLE leave_balances (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_id BIGINT NOT NULL,
  year INT NOT NULL,
  earned_days DECIMAL(5,2) NOT NULL DEFAULT 0,
  used_days DECIMAL(5,2) NOT NULL DEFAULT 0,
  remaining_days DECIMAL(5,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_leave_balance_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  CONSTRAINT uq_employee_year UNIQUE (employee_id, year)
);

CREATE TABLE leave_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_id BIGINT NOT NULL,
  leave_request_id BIGINT NULL,
  action VARCHAR(100) NOT NULL,
  action_date DATE NOT NULL,
  note TEXT,
  CONSTRAINT fk_leave_history_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  CONSTRAINT fk_leave_history_request
    FOREIGN KEY (leave_request_id) REFERENCES leave_requests(id)
      ON DELETE SET NULL
      ON UPDATE CASCADE
);

----------Values----------

INSERT INTO admins (first_name, last_name, email, password)
VALUES ('Admin', 'HR', 'a', 'a');

INSERT INTO leave_types (name, description, max_days)
VALUES
  ('Annual Leave', 'Regular annual paid leave', 30),
  ('Sick Leave', 'Leave for health reasons', 15),
  ('Unpaid Leave', 'Leave without salary', 10);





