# 🧠 AI Note Finder – Semantic Search POC

A production-ready proof-of-concept for saving and semantically searching notes using:

- 💻 Spring Boot (REST APIs)
- 🤖 Ollama (`nomic-embed-text` model for embeddings)
- 📦 Qdrant (Vector Database for similarity search)

---

## 🎯 Features

| Endpoint                   | Description                                      |
|----------------------------|--------------------------------------------------|
| `POST /note`               | Add a single note                                |
| `POST /note/bulk`          | Insert bulk notes with embeddings (1000+ capable)|
| `GET /note/search?q=...`   | Semantic search for similar notes                |

---

## 🏗️ Architecture

```plaintext
User
 │
 └──> Spring Boot REST API
         ├── POST /note         - Add note
         ├── POST /note/bulk    - Add multiple notes
         └── GET  /note/search  - Search notes
              ↓
     ┌────────────────────┐
     │ Embedding Provider │ ← Ollama (nomic-embed-text)
     └────────────────────┘
              ↓
     ┌────────────────────┐
     │  Vector Database   │ ← Qdrant
     └────────────────────┘
