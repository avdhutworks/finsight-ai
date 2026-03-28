# 💰 FinSight AI

**FinSight AI** is a GenAI-powered financial intelligence application that analyzes bank statement PDFs and provides meaningful insights about spending using **Spring AI + Ollama (local LLM)**.

It combines **RAG (Retrieval-Augmented Generation)**, **vector search**, and **structured transaction parsing** to deliver accurate financial analysis.

---

## 🚀 Features

### 📄 Statement Processing

* Upload bank statement PDFs
* Extract and process unstructured transaction data
* Convert raw text into structured transactions

### 🤖 AI Chat (RAG)

* Ask questions about your spending
* Uses hybrid RAG:

    * Vector search (semantic)
    * Category filtering (rule-based)
* Works completely on **local LLM (Ollama)**

### 📊 Financial Insights

* Category-wise spending breakdown
* Percentage-based insights (e.g., *"You spend 35% on Food"*)
* Top merchants detection
* Rule-based financial insights
* AI-generated recommendations

---

## 🧠 Architecture Overview

```
PDF → Chunking → Embeddings → Vector Store
                        ↓
User Query → Embedding → Similarity Search
                        ↓
Hybrid Filtering → LLM → Answer

Unstructured Text → TransactionParser → Structured Transactions
                                      ↓
                           Insights Engine
```

---

## 🏗️ Tech Stack

### Backend

* Java 17
* Spring Boot 3.5
* Spring AI
* Ollama (local LLM)
* Apache PDFBox

### AI / ML

* Embedding Model: `nomic-embed-text`
* Chat Model: `llama3`
* Vector Store: In-memory (custom implementation)

---

## ⚙️ Setup & Installation

### 1️⃣ Clone Repository

```bash
git clone https://github.com/avdhutworks/finsight-ai.git
cd finsight-ai
```

---

### 2️⃣ Install & Run Ollama

Install Ollama

```bash
ollama serve
```

Download required models:

```bash
ollama pull llama3
ollama pull nomic-embed-text
```

---

### 3️⃣ Configure Application

Update `application.properties`:

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3
spring.ai.ollama.embedding.options.model=nomic-embed-text
```

---

### 4️⃣ Run Application

```bash
mvn clean install
mvn spring-boot:run
```

---

### 5️⃣ Access APIs

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## 📡 API Endpoints

### 📄 Upload Statement

```
POST /api/v1/finSight/upload/pdf
```

### 🤖 Ask Questions

```
POST /api/v1/finSight/ask
POST /api/v1/finSight/embedded-model/ask
```

### 📊 Summary

```
GET /api/v1/insights/summary
```

### 💡 Full Insights

```
GET /api/v1/insights
```

---

## 🧪 Sample Questions

* How much did I spend on food?
* Where do I spend the most?
* What are my medical expenses?
* Show top merchants

---

## 🧠 Key Concepts Implemented

* Retrieval-Augmented Generation (RAG)
* Hybrid retrieval (vector + rule-based)
* Embedding-based semantic search
* Cosine similarity ranking
* Transaction parsing from unstructured data
* Financial analytics engine

---

## 🔥 Highlights

* Fully **local AI system** (no external API dependency)
* Converts **unstructured PDFs → structured financial data**
* Combines **AI + rule-based intelligence**
* Clean, scalable architecture (separated services)

---

## 📈 Future Enhancements

### 📊 Analytics

* Monthly spending trends
* Budget tracking & alerts
* Category-wise visual charts

### 🏪 Intelligence

* Advanced merchant normalization
* Duplicate transaction detection
* Subscription detection

### 🤖 AI

* Conversational memory
* Personalized financial advice
* Multi-document support

### 🌐 UI

* React dashboard
* Chat interface
* Insights visualization (charts)

### 🗄️ Backend

* Persistent vector DB (PostgreSQL + pgvector)
* Caching for faster responses
* Multi-user support

---

## 🎯 Project Goal

To build a **production-grade GenAI financial assistant** that combines:

* AI (LLM + embeddings)
* Backend engineering
* Financial domain logic

---

## 👨‍💻 Author

**Avdhut Parab**

GitHub: https://github.com/avdhutworks

---

## ⭐ Contribution

Feel free to fork, improve, and contribute!
