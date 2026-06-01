# Employee Leave Management System

**PFE Project — Licence 3 Informatique**  
**University of Chlef**  
**Author:** Mhamedi Yasser

---

## 1. Project Overview

This is a **desktop application** for the HR department (DRH) to manage employee leave requests in a company. It replaces manual tracking with a centralized system that handles:

- Employee records
- Leave request lifecycle (create → approve/reject)
- Automatic leave balance calculation
- Leave history and audit trail
- Reporting (CSV export) and document generation (Word letters)
- Optional file attachments on requests

The application is built with **JavaFX** (rich desktop UI) and **MySQL** (persistent storage).

---

## 2. Actors

| Actor | Role |
|-------|------|
| **HR Admin (DRH)** | Logs in, manages employees, creates and processes leave requests, exports data |
| **System** | Calculates leave balances, records history, validates inputs, stores attachments |

> **Note:** There is no separate employee login in this version. The HR admin acts on behalf of employees.

---

## 3. Technologies Used

| Layer | Technology |
|-------|------------|
| Language | Java 23 |
| UI | JavaFX 23 + FXML + CSS |
| Database | MySQL 8 |
| JDBC | MySQL Connector/J 9.0 |
| Build | Maven |
| Excel import | Apache POI 5.2 |
| Word letters | Apache POI (docx templates) |
| Password hashing | PBKDF2 (HMAC-SHA256) |
| Version control | Git / GitHub |

---

## 4. Project Structure

```
src/main/java/
├── com/gestionconges/     → Main.java, SessionContext
├── controller/            → JavaFX controllers (FXML logic)
├── dao/                   → Database access (CRUD)
├── database/              → JDBC connection
├── model/                 → Entity classes (Employee, LeaveRequest, …)
├── enums/                 → Status enums
└── util/                  → CSV export, Excel import, letters, security, navigation

src/main/resources/
├── view/                  → FXML screens
├── css/                   → Stylesheets
├── img/                   → Images and logos
└── templates/             → Word template for leave letters

sql/
└── leave_management_system.sql   → Database schema + seed data
```

### Architecture pattern

The project follows a **layered MVC-style** architecture:

```
FXML View  →  Controller  →  DAO  →  MySQL
                  ↓
               Model classes
```

- **View (FXML):** UI layout only
- **Controller:** User actions, validation, navigation
- **DAO:** SQL queries and data mapping
- **Model:** Plain Java objects representing database rows

---

## 5. Database Schema

| Table | Purpose |
|-------|---------|
| `admins` | HR administrators (login accounts) |
| `employees` | Employee personal and job information |
| `leave_types` | Types of leave (Annual, Sick, Unpaid) |
| `leave_requests` | Leave requests with status, dates, reason, attachment |
| `leave_balances` | Earned / used / remaining days per employee per year |
| `leave_history` | Audit log of actions (created, approved, rejected, updated) |

### Relationships

- One **employee** → many **leave requests**
- One **leave type** → many **leave requests**
- One **admin** → processes many **leave requests**
- One **employee** → one **leave balance** row per year
- **leave_history** links to employee and optionally to a request

### Default seed data

- **Admin login:** email `a` / password `a` (hashed automatically on first login)
- **Leave types:** Annual Leave (30 days), Sick Leave (15 days), Unpaid Leave (10 days)

---

## 6. Main Features

### 6.1 Authentication

- Admin logs in with email and password
- Passwords are verified using **PBKDF2** hashing
- On first login, plain-text passwords in the database are upgraded to a hash
- Session is stored in `SessionContext` for the logged-in admin

### 6.2 Dashboard

- Total employees and active employees count
- Pending leave requests count
- Approved / rejected this month statistics
- Recent leave requests list with filters (All, Pending, Approved, Rejected)
- Auto-refresh every 5 seconds

### 6.3 Employee Management

- Add employee manually (modal form)
- Import employees from **Excel** (.xlsx / .xls)
- Search employees by code, name, email, or department
- View employee details (department, position, contact, hire date)
- Edit employee information
- Activate / deactivate employees
- Pagination (10 employees per page)

### 6.4 Leave Request Management

- Create a new leave request (employee, type, dates, reason)
- Optional **file attachment** (PDF, PNG, JPG — max 5 MB)
- Filter requests by status
- Search requests
- **Approve** or **Reject** (with optional rejection comment)
- Modify pending requests
- Delete requests (attachment file is removed from disk)
- View attached document

### 6.5 Leave Balance

- Automatic accrual: **2.5 days per month**, maximum **30 days per year**
- Based on employee **hire date**
- Balance synced when viewing or approving leave
- Admin can manually edit balance for a selected year

### 6.6 Leave History

- Tracks actions: request created, approved, rejected, updated, etc.
- Visible per employee from the employee details modal

### 6.7 Export & Documents

- **CSV export** of leave requests (filter by date range)
- **Leave letter generation** (Word `.docx` from template) for approved requests

### 6.8 Support (UI only)

- Support form opens as a modal window
- Currently shows a confirmation message only (not saved to database)

---

## 7. How the Application Works (Step by Step)

### Startup flow

1. Application starts → **Login screen** (1100×700)
2. Admin enters credentials → `AdminDao` verifies password
3. On success → **Dashboard** opens (1400×700)
4. Navigation between pages uses `ViewNavigator.switchScene()`

### Leave request lifecycle

```
1. HR creates request        → status = PENDING
2. HR opens request details  → can Approve or Reject
3. If APPROVED               → balance updated, history recorded
4. If REJECTED               → rejection comment saved, history recorded
5. Optional                  → generate leave letter (approved only)
```

### Leave balance calculation

```
earned_days = months_worked_in_year × 2.5  (capped at 30)
remaining_days = earned_days - used_days
```

The `LeaveAccrualCalculator` uses the hire date and current date to compute how many months count in the current year.

### File attachments

- Files are stored on disk in `uploads/leave_documents/`
- Only the **relative path** is saved in the database (not the file content)
- Files are validated by extension and size before upload

---

## 8. Installation & Running

### Prerequisites

- **JDK 23** (with JavaFX support)
- **MySQL 8** running locally
- **IntelliJ IDEA** (recommended) or Maven CLI

### Step 1 — Database

```sql
-- Run the script in MySQL Workbench or CLI:
source sql/leave_management_system.sql
```

This creates the database `leave_management_system` with all tables and seed data.

### Step 2 — Database connection

Edit `src/main/java/database/DatabaseConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/leave_management_system";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_mysql_password";
```

### Step 3 — Run the application

**IntelliJ IDEA:**
1. Open the project
2. Run `com.gestionconges.Main`

**Maven (if configured):**
```bash
mvn clean javafx:run
```

### Demo login

| Field | Value |
|-------|-------|
| Email | `a` |
| Password | `a` |

---

## 9. Suggested Demo Script (5–7 minutes)

1. **Login** as HR admin
2. **Dashboard** — show employee stats and recent requests
3. **Employees** — search an employee, open details, show leave balance and history
4. **Leave Requests** — create a new request (optionally attach a file)
5. **Approve** one pending request → show balance change
6. **Reject** one request with a comment
7. **Export CSV** for a date range
8. *(Optional)* Generate a leave letter for an approved request
9. *(Optional)* Import employees from Excel

---

## 10. Jury Questions & Suggested Answers

### General / Project context

**Q: What problem does your project solve?**  
> HR departments often track leave manually (Excel, paper). Our system centralizes employee data, leave requests, balances, and history in one desktop application with a MySQL database.

**Q: Who is the target user?**  
> The HR administrator (DRH). In this version, only the admin uses the system — there is no employee self-service portal yet.

**Q: Why a desktop app and not a web app?**  
> JavaFX gives a native desktop experience, works offline with a local database, and fits our Java curriculum. A web version could be a future extension.

---

### Architecture & design

**Q: Explain your architecture.**  
> Layered MVC: FXML views for UI, controllers for logic, DAOs for database access, and model classes for data. Navigation is centralized in `ViewNavigator`.

**Q: Why did you separate DAO from Controller?**  
> Separation of concerns — controllers handle UI events, DAOs handle SQL. This makes the code easier to maintain and test.

**Q: What design patterns did you use?**  
> DAO pattern for data access, Singleton-like session via `SessionContext`, and MVC for UI separation.

**Q: Show me the database schema.**  
> Six main tables: admins, employees, leave_types, leave_requests, leave_balances, leave_history — linked with foreign keys and CASCADE rules.

---

### Features & business logic

**Q: How are leave balances calculated?**  
> 2.5 days per month worked, max 30 days per year, based on hire date. Implemented in `LeaveAccrualCalculator`. Balances are stored per employee per year in `leave_balances`.

**Q: What happens when a leave request is approved?**  
> Status changes to APPROVED, the admin who processed it is recorded, used days increase in the balance, and an entry is added to leave history.

**Q: Can two leave requests overlap for the same employee?**  
> Currently we validate that end date is not before start date. **Automatic overlap detection is planned for a future version** — be honest about this.

**Q: How do file attachments work?**  
> Files (PDF/images, max 5 MB) are saved to `uploads/leave_documents/` on disk. Only the file path is stored in the database. The file is deleted when the request is deleted.

**Q: How does Excel import work?**  
> Apache POI reads `.xlsx`/`.xls` files. Each row is parsed, validated, and inserted via `EmployeeDao`. Errors are reported row by row.

**Q: What does CSV export contain?**  
> Request ID, employee code, employee name, leave type, start date, end date, and status — filtered by a date range chosen by the admin.

**Q: How does leave letter generation work?**  
> A Word template (`leave-letter-template.docx`) contains placeholders like `{employee_name}`, `{start_date}`. Apache POI replaces them and saves a temporary `.docx` file.

---

### Security

**Q: How are passwords stored?**  
> Using PBKDF2 with HMAC-SHA256, 65536 iterations, random salt. Stored format: `pbkdf2$iterations$salt$hash`. Plain-text passwords from the seed script are auto-upgraded on first login.

**Q: Is the application secure enough for production?**  
> For a university project, yes — we demonstrate password hashing and session management. For production, we would add: environment variables for DB credentials, HTTPS if web-based, role-based access, and audit logging.

**Q: Where is the database password stored?**  
> Currently hardcoded in `DatabaseConnection.java` — acceptable for local development, not for production. We would use environment variables or a config file in a real deployment.

---

### Technical choices

**Q: Why JavaFX?**  
> Modern Java UI framework, good for desktop apps, integrates with FXML for declarative UI design, and is part of our Java curriculum.

**Q: Why MySQL?**  
> Relational data (employees, requests, balances with foreign keys), widely used, free, and well supported with JDBC.

**Q: Why Maven?**  
> Dependency management (JavaFX, MySQL driver, Apache POI) and standardized project structure.

---

### Limitations & future work

**Q: What are the limitations of your project?**  
> - No employee self-service login  
> - No automatic date overlap detection  
> - Support module is UI-only (not saved to DB)  
> - Database credentials are hardcoded  
> - No automated unit tests  
> - Desktop only (not accessible remotely)

**Q: What would you add in the future?**  
> - Employee portal (submit own requests)  
> - Email notifications on approval/rejection  
> - Calendar view of absences  
> - PDF report generation  
> - Date conflict validation  
> - Web or mobile version  
> - Role-based permissions (multiple admin levels)

---

### Difficulties encountered

**Q: What was the hardest part?**  
> *(Prepare your own honest answer. Examples:)*  
> - Connecting JavaFX ListView with database refresh without UI glitches  
> - MySQL schema migrations (adding columns like `attachment_path`)  
> - Leave balance accrual logic based on hire date  
> - Excel import with varying column layouts  

**Q: How did you handle errors?**  
> Try-catch in DAOs with meaningful messages, validation before saving (dates, file types, required fields), and user-friendly alert dialogs via `ViewNavigator.showInformation()`.

---

## 11. Quick Reference — Key Classes

| Class | Role |
|-------|------|
| `Main.java` | Application entry point |
| `LoginController` | Handles login |
| `DashboardController` | Dashboard stats and recent requests |
| `MenuEmployeeController` | Employee list, search, import |
| `MenuCongesController` | Leave requests CRUD, approve/reject, export |
| `EmployeeController` | Employee detail modal |
| `AddEmployeeController` | Add employee form |
| `LeaveBalanceController` | View/edit leave balance |
| `LeaveHistoryController` | View leave history |
| `ViewNavigator` | Scene switching and modals |
| `SessionContext` | Logged-in admin session |
| `PasswordSecurity` | PBKDF2 hash and verify |
| `LeaveAccrualCalculator` | Balance calculation rules |
| `LeaveAttachmentService` | File upload validation and storage |
| `LeaveRequestCsvExporter` | CSV export |
| `LeaveLetterGenerator` | Word document generation |
| `EmployeeExcelImporter` | Excel import |

---

## 12. License

Academic project — University of Chlef, Licence 3 Informatique.  
For educational purposes only.
