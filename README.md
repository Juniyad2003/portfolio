
# 📊 Portfolio Manager – Spring Boot REST Application with AI Insights

## 📌 Project Overview

**Portfolio Manager** is a backend-driven financial portfolio management system built using **Spring Boot**.
It allows a **single user** to manage portfolios, assets, and transactions, calculate profit/loss, visualize performance, and generate **AI-powered insights** using Groq AI.

The project follows **clean layered architecture**, exposes **REST APIs**, supports **Swagger documentation**, and is designed to be easily connected to a frontend (HTML/CSS/JS or React).

---

## 🎯 Key Features

* ✅ Portfolio, Asset, and Transaction management (Full CRUD)
* 📈 Automatic profit/loss & portfolio value calculations
* 🤖 AI integration using **Groq API**

  * General AI chat
  * Portfolio performance analysis from database data
* 📊 Portfolio performance & statistics APIs
* 📉 Mocked real-world market data (Yahoo Finance style)
* 🛡️ Centralized exception handling
* 🧾 Swagger/OpenAPI documentation
* 🧪 Ready for JUnit testing & CI pipelines
* 🗄️ MySQL database integration

---

## 🧱 Project Architecture

```
Postman / Frontend (HTML, JS, React)
        ↓
Controller Layer (REST APIs)
        ↓
Service Layer (Business Logic + AI)
        ↓
Repository Layer (JPA)
        ↓
MySQL Database
        ↓
AI Prompt built from DB values
        ↓
Groq AI Response
```

---

## 📁 Project Structure

```
src/main/java/com/finance
│
├── controller
│   ├── AiController.java
│   ├── AssetController.java
│   ├── PortfolioController.java
│   ├── TransactionController.java
│   ├── MarketDataController.java
│
├── service
│   ├── AssetService.java
│   ├── PortfolioService.java
│   ├── TransactionService.java
│   ├── GroqAiService.java
│   ├── YahooFinanceService.java
│
├── entity
│   ├── Investor.java
│   ├── Portfolio.java
│   ├── Asset.java
│   ├── Transaction.java
│
├── repo
│   ├── PortfolioRepo.java
│   ├── AssetRepo.java
│   ├── TransactionRepo.java
│
├── logic
│   ├── PortfolioCalculator.java
│   ├── TransactionProcessor.java
│
├── dto
│   ├── PerformancePoint.java
│
├── exception
│   ├── CustomExceptionHandler.java
│   ├── ErrorResponse.java
│   ├── InvalidPortfolioIdException.java
│   ├── InvalidAssetIdException.java
│   ├── InvalidTransactionIdException.java
│
└── PortfolioApplication.java
```

---

## 🗃️ Core Domain Model

### Investor (Base Class)

* name
* email
* investmentGoal
* riskPreference
* createdAt

### Portfolio (extends Investor)

* id
* portfolioName
* totalInvestment
* currentValue
* totalProfitLoss
* assets (One-to-Many)

### Asset

* id
* assetName
* assetType (STOCK, ETF, CRYPTO, etc.)
* quantity
* investedAmount
* currentAmount
* volatilityLevel
* portfolio (Many-to-One)

### Transaction

* id
* transactionType (BUY / SELL)
* quantity
* amount
* transactionDate
* asset (Many-to-One)

---

## 🔗 REST API Endpoints

### Portfolio APIs

```
GET    /portfolios
POST   /portfolios
GET    /portfolios/{id}
PUT    /portfolios/{id}
DELETE /portfolios/{id}

GET    /portfolios/{id}/assets
GET    /portfolios/{id}/performance
GET    /portfolios/{id}/stats/value-by-type
GET    /portfolios/{id}/stats/profit-by-type
```

### Asset APIs

```
GET    /assets
POST   /assets
GET    /assets/{id}
PUT    /assets/{id}
DELETE /assets/{id}
```

### Transaction APIs

```
GET    /transactions
POST   /transactions
GET    /transactions/{id}
POST   /transactions/buy
POST   /transactions/sell
```

### AI APIs

```
POST /api/ai/chat
GET  /api/ai/analyze-portfolio/{portfolioId}
```

### Market Data (Mock)

```
GET /api/market-data/{symbol}
```

---

## 🤖 AI Integration (Groq)

### AI Capabilities

1. **General AI Chat**

   * Free-form finance-related questions
2. **Portfolio Analysis AI**

   * Reads data from MySQL
   * Builds structured prompt
   * Returns:

     * Risk assessment
     * Diversification quality
     * Improvement suggestions
   * No investment advice (education-only)

### AI Flow

```
Portfolio + Assets from DB
        ↓
Prompt Builder
        ↓
Groq AI API
        ↓
Plain-text AI Response
```

---

## 🧪 Testing

* REST APIs tested using **Postman**
* Business logic testable via **JUnit**
* AI service supports mock RestTemplate for unit tests

---

## 📜 Swagger API Documentation

Once the application is running:

```
http://localhost:8080/swagger-ui.html
```

or

```
http://localhost:8080/swagger-ui/index.html
```

---

## ⚙️ Configuration

### application.properties (example)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/portfolio_db
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

groq.api-key=YOUR_GROQ_API_KEY

rapidapi.key=mock
rapidapi.host=mock
```

---

## 🚀 How to Run the Project

```bash
git clone https://github.com/your-username/portfolio-manager.git
cd portfolio-manager
mvn clean install
mvn spring-boot:run
```

---

## 🧠 Why This Project Stands Out

* Clean layered architecture
* Strong REST API design
* Real-world finance logic
* AI integration with real database context
* Logging & exception handling
* Ready for CI/CD & frontend integration
* Easily explainable to non-technical clients



## 👨‍💻 Developed By

**Junaid T , Chirag , Deeksha and Hiya  (Portfolio Manager Project)**
Spring Boot • REST API • AI Integration • MySQL • DevOps Ready

