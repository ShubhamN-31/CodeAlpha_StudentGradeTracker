# 📊 Student Grade Tracker Pro

A robust **Full-Stack JavaFX** application built for educators to track student performance with real-time analytics and MySQL integration.

## 🚀 Key Features
* **Full CRUD Operations:** Add, Update, and Delete student records directly to a MySQL database.
* **Visual Analytics:** Dynamic **PieChart** showing grade distributions (A, B, C, D).
* **Live Statistics:** Instant calculation of Class Average, Highest, and Lowest marks.
* **Search & Filter:** Real-time search bar to find students by name instantly.
* **UI/UX Polish:** Double-click to edit rows and Keyboard shortcuts (`ENTER` to save).

## 🛠️ Tech Stack
* **Language:** Java 17
* **Framework:** JavaFX (UI)
* **Database:** MySQL (JDBC)
* **Build Tool:** IntelliJ IDEA

## 📋 Setup Instructions
1.  **Database Setup:** Create a database named `student_db` in MySQL Workbench.
2.  **Configuration:** * Open `src/ui/DatabaseHandler.java`.
    * Update the `USER` and `PASS` variables with your local MySQL credentials.
3.  **Drivers:** Ensure the `mysql-connector-java` JAR is added to your project libraries.
4.  **Run:** Execute `MainApp.java` to launch the dashboard.

## 📸 Dashboard Preview
*(Tip: You can upload your screenshot to GitHub and link it here to show off the UI!)*