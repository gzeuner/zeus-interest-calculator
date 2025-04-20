# ⚡ Zeus Interest Calculator

A powerful and flexible interest plan calculator for loans and deposits – with extra payments, yearly grouping, and export/import functionality.

Built with:
- Spring Boot (Java 17+)
- Thymeleaf for UI rendering
- Bootstrap 5 for styling

## ✨ Features

- Loan and deposit calculation (mode switch)
- Pro-rata interest for first period
- Extra payments (Sondertilgungen)
- Yearly grouping of payment plans
- Internationalization (German / English)
- Simple and clean UI with Bootstrap

## 🛠️ Getting Started

```
./mvnw spring-boot:run
```

Visit the app at: http://localhost:8080/interest

---

## 📂 Project Structure

```
src
├── controller       → Spring MVC logic
├── dto              → Request/response models
├── model            → Core domain models
├── service          → Strategy-based calculation logic
├── util             → Helpers (e.g., 30/360 interest)
└── templates        → Thymeleaf templates
```

---

## 🧹 Dependencies & Licenses

This project uses the following open-source libraries:

| Library                                  | License            |
|------------------------------------------|--------------------|
| Spring Boot (Web, Thymeleaf, Validation) | Apache License 2.0 |
| Lombok                                   | MIT License         |
| Jackson (via Spring Boot)                | Apache License 2.0 |
| Bootstrap 5                              | MIT License         |
| Thymeleaf                                | Apache License 2.0 |
| Jakarta Bean Validation                  | Apache License 2.0 |

> ℹ️ See the `pom.xml` for full dependency declarations.

---

## 📄 License

This project is licensed under the **Apache License 2.0**.  
See the `LICENSE` file for details.

---

## 🌐 UI & Styling

The frontend is rendered using Thymeleaf and styled with Bootstrap 5.  
Language selection is available via a simple `?lang=` parameter in the URL or language switcher in the UI.