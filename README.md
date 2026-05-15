# CodeCraftHub Course API

A beginner-friendly Spring Boot REST API project for managing courses using CRUD operations with JSON file storage.

---

# 🚀 Features

- Create, Read, Update, and Delete courses
- JSON file-based storage (`courses.json`)
- RESTful API design
- Auto-generated course IDs
- Validation for required fields
- Status validation
- Automatic timestamp generation
- Global exception handling
- CORS support for frontend integration
- Beginner-friendly code with comments

---

# 🛠️ Technologies Used

- Java 17
- Spring Boot 3
- Maven
- Jackson JSON Processor
- REST API
- Bean Validation

---

# 📁 Project Structure

```text
src/main/java/com/codecrafthub/courseapi
|
├── controller
│   └── CourseController.java
|
├── service
│   └── CourseService.java
|
├── model
│   └── Course.java
|
├── config
│   └── CorsConfig.java
|
└── CodeCraftHubApplication.java
