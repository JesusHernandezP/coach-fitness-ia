# 🧠 Fitness AI Coach

Aplicación fitness multiplataforma con inteligencia artificial orientada a nutrición, entrenamiento y seguimiento físico personalizado.

El sistema integra perfil metabólico, cálculo automático de macros, seguimiento de progreso y un asistente conversacional impulsado por IA.

---

# 🚀 Objetivo

Construir una plataforma moderna enfocada en salud y fitness utilizando arquitectura backend robusta, aplicaciones multiplataforma y servicios de inteligencia artificial aplicados a usuarios reales.

---

# 🛠 Tecnologías utilizadas

## Backend
- Java 21
- Spring Boot 3
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- PostgreSQL
- Docker
- Docker Compose

## Frontend Web
- Angular 18
- TypeScript
- RxJS

## Android
- Kotlin
- Jetpack Compose
- MVVM
- Retrofit
- Hilt

## Inteligencia Artificial
- Groq API
- Llama 3.3 70B

---

# ✨ Características principales

- Perfil metabólico personalizado
- Cálculo automático de calorías y macronutrientes
- Seguimiento físico y nutricional
- Chat IA tipo nutricionista/entrenador
- Arquitectura backend modular
- API REST segura con JWT
- Persistencia relacional con PostgreSQL
- Cliente web y aplicación Android
- Contenedorización completa con Docker

---

# 📚 Arquitectura

El proyecto sigue arquitectura por capas para separar responsabilidades entre controladores, servicios, persistencia y clientes.

La comunicación entre plataformas se realiza mediante APIs REST centralizadas.

---

# 📱 Plataformas

| Plataforma | Tecnología |
|---|---|
| Web | Angular 18 |
| Android | Kotlin + Jetpack Compose |
| Backend | Java 21 + Spring Boot |
| Base de datos | PostgreSQL |
| IA | Groq API + Llama 3 |

---

# ⚙️ Ejecución local

## Requisitos

- Docker + Docker Compose
- Java 21
- Node.js 20+
- Android Studio

---

## Con Docker

```bash
cp .env.example .env
docker compose up
```

| Servicio    | URL                                                                            |
| ----------- | ------------------------------------------------------------------------------ |
| Web Angular | [http://localhost:4200](http://localhost:4200)                                 |
| Backend API | [http://localhost:8080](http://localhost:8080)                                 |
| Swagger UI  | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| PostgreSQL  | localhost:5432                                                                 |

DB_URL=jdbc:postgresql://localhost:5432/fitnesscoach
DB_USER=postgres
DB_PASSWORD=changeme
JWT_SECRET=your-secret-key
GROQ_API_KEY=your-api-key
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200
SPRING_PROFILES_ACTIVE=dev

🔗 Repositorio

https://github.com/JesusHernandezP/coach-fitness-ia

👨‍💻 Autor

Jesús Hernández

GitHub:
https://github.com/JesusHernandezP

LinkedIn:
https://www.linkedin.com/in/jesushernandezp/

Portfolio:
https://jesus-hernandez.es


📄 Licencia


MIT License
