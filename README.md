# ChatApplication
A Java-based RMI (Remote Method Invocation) Chat Application with role-based access control, chat subscription, and persistent storage using MySQL and Hibernate. The UI is developed using JavaFX and Scene Builder.

---

## 🛠 Features

### 👤 Users
- Register with email, username, password, nickname, and profile picture.
- Log in and view personalized dashboard.
- Subscribe/unsubscribe to available chats.
- Join and participate in chats.
- View chat notifications.
- Update profile (except email).

### 🛡 Admin
- Create new chats with title, description, and scheduled date.
- Start and stop chats.
- Automatically notify online users upon chat start.
- View all users and remove if needed.
- Manage user subscriptions.

---

## 🧱 Tech Stack

- **Java 21**
- **JavaFX** (UI)
- **RMI** (for client-server chat communication)
- **MySQL** (Database)
- **Hibernate** (ORM for DB interactions)
- **Maven** (Build tool)

---

## 🗃 Database Structure

- `user` — stores user accounts (admin & regular users)
- `chat` — stores chat metadata (title, description, timestamps, transcript path)
- `message` — stores messages linked to chats and users

---

## 🚀 How to Run

1. Start your MySQL server and import the provided schema.
2. Run `RMIServerLauncher.java` to start the RMI registry and server.
3. Run `MainApp.java` to launch the client-side UI.
4. Register as a user or login as admin to access features.

---

## 📌 Notes

- Only one chat can be active at a time.
- Users can participate in only one chat session concurrently.
- Chats are saved as `.txt` transcripts and logged in the database.

---
