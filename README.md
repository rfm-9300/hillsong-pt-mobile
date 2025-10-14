# 🏛️ Church Management Platform

> A comprehensive, full-stack church management ecosystem featuring a cross-platform mobile app, modern admin dashboard, and robust REST API backend.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-15.4.6-000000?logo=next.js&logoColor=white)](https://nextjs.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Screenshots](#-screenshots)

---

## 🎯 Overview

This is a production-ready, enterprise-grade church management platform designed to streamline community engagement, event management, and administrative operations. The system consists of three interconnected applications:

- **📱 Mobile App**: Cross-platform iOS/Android app built with Kotlin Multiplatform and Compose
- **💻 Admin Panel**: Modern web dashboard built with Next.js 15 and React 19
- **⚙️ REST API**: Scalable Spring Boot backend with PostgreSQL database

The platform serves real-world use cases including event management, attendance tracking, kids check-in systems, social feeds, and comprehensive user management.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Applications                      │
├──────────────────────────┬──────────────────────────────────┤
│   Mobile App (KMP)       │   Admin Panel (Next.js)          │
│   - iOS Native           │   - Server Components            │
│   - Android Native       │   - React 19                     │
│   - Compose UI           │   - TailwindCSS 4                │
└──────────────────────────┴──────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot REST API (Kotlin)                   │
├─────────────────────────────────────────────────────────────┤
│  • JWT Authentication    • WebSocket Support                 │
│  • OAuth2 (Google)       • Role-Based Access Control         │
│  • File Management       • Real-time Notifications           │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL Database + Flyway                    │
│              (Containerized with Docker)                     │
└─────────────────────────────────────────────────────────────┘
```

### Design Patterns & Principles

- **Clean Architecture**: Clear separation of concerns across layers
- **Repository Pattern**: Abstracted data access layer
- **Dependency Injection**: Koin (Mobile) & Spring DI (Backend)
- **MVVM**: Model-View-ViewModel for mobile app
- **RESTful API Design**: Standard HTTP methods and status codes
- **Feature-Based Modularization**: Scalable code organization

---

## ✨ Key Features

### 🔐 Authentication & Authorization
- Multi-provider authentication (Email/Password, Google OAuth2)
- JWT token-based security with refresh token support
- Role-based access control (Admin, User roles)
- Password reset with email verification
- Secure session management

### 📅 Event Management
- Create, update, and manage events with rich details
- Event capacity tracking and waiting list management
- Participant approval workflow
- Event search and filtering (upcoming, past, by location)
- Real-time event status updates via WebSocket
- Join/leave event functionality

### 👥 User Management
- Comprehensive user profiles with customizable avatars
- Admin dashboard for user administration
- User search and filtering capabilities
- Profile image upload (local and external URLs)
- Activity tracking and analytics

### 📊 Attendance System
- QR code-based check-in/check-out
- Real-time attendance tracking
- Attendance statistics and analytics
- Bulk check-in capabilities for admins
- Historical attendance records
- Currently checked-in user monitoring

### 👶 Kids Check-In System
- Child registration and management
- Age-appropriate service assignment
- Secure check-in/check-out workflow
- Parent-child relationship management
- Check-in history and reporting
- Safety and security features

### 📱 Social Feed
- Create and share posts with images
- Like and comment functionality
- Post search and filtering
- Author-based post retrieval
- Post statistics and engagement metrics

### 🔔 Real-Time Features
- WebSocket integration for live updates
- Push notifications for event changes
- Real-time attendance updates
- Live check-in status monitoring

### 📁 File Management
- Secure file upload and storage
- Image optimization and serving
- Profile picture management
- Event image handling
- Organized file directory structure

---

## 🛠️ Technology Stack

### Mobile App (Kotlin Multiplatform)

```kotlin
• Kotlin 2.1.10 - Modern, type-safe programming language
• Compose Multiplatform 1.7.3 - Declarative UI framework
• Ktor 3.0.3 - HTTP client for API communication
• Koin 4.0.4 - Dependency injection framework
• Room 2.7.0 - Local database with SQLite
• Coil 3 - Async image loading
• Kotlinx Serialization - JSON parsing
• Navigation Compose - Type-safe navigation
• Coroutines - Asynchronous programming
• Kermit - Multiplatform logging
```

**Platform-Specific:**
- iOS: Native iOS integration with SwiftUI interop
- Android: Material Design 3, Activity Compose

### Admin Panel (Next.js)

```typescript
• Next.js 15.4.6 - React framework with App Router
• React 19.1.0 - Latest React with Server Components
• TypeScript 5 - Type-safe JavaScript
• TailwindCSS 4 - Utility-first CSS framework
• React Hook Form 7.62 - Form state management
• Zod 4.1.5 - Schema validation
• Turbopack - Ultra-fast bundler
• ESLint - Code quality and consistency
```

**Features:**
- Server-side rendering (SSR)
- Server Components for optimal performance
- Responsive design (mobile-first)
- Form validation with Zod schemas
- Custom hooks for API calls and state management
- Error boundary and error handling
- Performance optimization with bundle analysis

### Backend API (Spring Boot)

```kotlin
• Spring Boot 3.2.2 - Enterprise Java framework
• Kotlin 1.9.22 - JVM language
• Spring Security - Authentication & authorization
• Spring Data JPA - ORM and database access
• PostgreSQL - Relational database
• Flyway - Database migration tool
• JWT (jjwt 0.12.3) - Token-based auth
• WebSocket - Real-time communication
• Spring Mail - Email notifications
• Spring Actuator - Monitoring and metrics
• Docker & Docker Compose - Containerization
• Jib - Container image building
```

**Additional Libraries:**
- Jackson Kotlin Module - JSON serialization
- Logback - Structured logging
- Commons Codec - Encoding utilities
- H2 Database - Testing database

---

## 📂 Project Structure

```
.
├── mobileApp/                      # Kotlin Multiplatform Mobile App
│   ├── composeApp/                 # Shared Compose UI code
│   ├── iosApp/                     # iOS-specific code
│   ├── core/                       # Core modules
│   │   ├── data/                   # Data layer (repositories)
│   │   ├── network/                # API client
│   │   ├── model/                  # Domain models
│   │   ├── designsystem/           # UI components
│   │   └── navigation/             # Navigation logic
│   ├── feature/                    # Feature modules
│   │   ├── auth/                   # Authentication
│   │   ├── events/                 # Event management
│   │   ├── feed/                   # Social feed
│   │   ├── home/                   # Home screen
│   │   ├── kids/                   # Kids check-in
│   │   ├── profile/                # User profiles
│   │   ├── groups/                 # Group management
│   │   ├── giving/                 # Donations
│   │   ├── prayer/                 # Prayer requests
│   │   ├── stream/                 # Live streaming
│   │   ├── ministries/             # Ministry info
│   │   └── settings/               # App settings
│   └── util/                       # Utilities
│
├── nextjs-admin-panel/             # Next.js Admin Dashboard
│   ├── src/
│   │   ├── app/                    # App Router pages
│   │   │   ├── admin/              # Admin routes
│   │   │   │   ├── dashboard/      # Dashboard
│   │   │   │   ├── users/          # User management
│   │   │   │   ├── events/         # Event management
│   │   │   │   ├── posts/          # Post management
│   │   │   │   └── attendance/     # Attendance tracking
│   │   │   ├── components/         # React components
│   │   │   │   ├── ui/             # UI primitives
│   │   │   │   ├── forms/          # Form components
│   │   │   │   └── providers/      # Context providers
│   │   │   ├── context/            # React Context
│   │   │   ├── hooks/              # Custom hooks
│   │   │   └── api/                # API routes
│   │   └── lib/                    # Utilities
│   └── public/                     # Static assets
│
└── springboot-api/                 # Spring Boot REST API
    ├── src/main/
    │   ├── kotlin/rfm/com/
    │   │   ├── config/             # Configuration classes
    │   │   ├── controller/         # REST controllers
    │   │   ├── service/            # Business logic
    │   │   ├── repository/         # Data access layer
    │   │   ├── entity/             # JPA entities
    │   │   ├── dto/                # Data transfer objects
    │   │   ├── security/           # Security components
    │   │   │   ├── jwt/            # JWT handling
    │   │   │   └── hashing/        # Password hashing
    │   │   ├── exception/          # Exception handling
    │   │   ├── job/                # Scheduled jobs
    │   │   └── util/               # Utility classes
    │   └── resources/
    │       ├── db/migration/       # Flyway migrations
    │       └── application.yaml    # Configuration
    ├── http-tests/                 # HTTP endpoint tests
    ├── docker-compose.yml          # Docker setup
    └── Dockerfile                  # Container definition
```

---

## 🚀 Getting Started

### Prerequisites

- **Mobile App**: 
  - JDK 17+
  - Android Studio (for Android)
  - Xcode (for iOS)
  - Kotlin 2.1.10+

- **Admin Panel**:
  - Node.js 20+
  - npm/yarn/pnpm

- **Backend API**:
  - JDK 17+
  - Docker & Docker Compose
  - PostgreSQL (or use Docker)

### Quick Start

#### 1. Backend API

```bash
cd springboot-api

# Using Docker Compose (recommended)
docker-compose up -d

# Or run locally
./gradlew bootRun
```

API will be available at `http://localhost:8080`

#### 2. Admin Panel

```bash
cd nextjs-admin-panel

# Install dependencies
npm install

# Run development server
npm run dev
```

Dashboard will be available at `http://localhost:3000`

#### 3. Mobile App

```bash
cd mobileApp

# For Android
./gradlew :composeApp:assembleDebug

# For iOS
cd iosApp
pod install
open iosApp.xcworkspace
```

### Environment Variables

#### Backend (.env)
```env
POSTGRES_DB=your_database
POSTGRES_USERNAME=your_username
POSTGRES_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email
SMTP_PASSWORD=your_password
```

#### Admin Panel (.env.local)
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

---

## 📚 API Documentation

The API provides comprehensive endpoints for all platform features. See [API_ENDPOINTS_INDEX.md](springboot-api/API_ENDPOINTS_INDEX.md) for complete documentation.

### Core Endpoints

| Category | Base Path | Description |
|----------|-----------|-------------|
| Authentication | `/api/auth` | Login, signup, OAuth, password reset |
| Events | `/api/events` | Event CRUD, join/leave, search |
| Attendance | `/api/attendance` | Check-in/out, statistics, tracking |
| Kids | `/api/kids` | Child management, service check-in |
| Posts | `/api/posts` | Social feed, comments, likes |
| Profile | `/api/profile` | User profiles, image upload |
| Admin | `/api/admin` | User management, role assignment |
| Files | `/api/files` | File upload and serving |

### Authentication

All protected endpoints require JWT token:
```
Authorization: Bearer <your_jwt_token>
```

### Response Format

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { }
}
```

---

## 🎨 Screenshots

> Add screenshots of your mobile app and admin panel here to showcase the UI/UX

---

## 🔒 Security Features

- **Password Security**: Bcrypt hashing with salt
- **JWT Tokens**: Secure token generation and validation
- **OAuth2**: Google authentication integration
- **CORS**: Configured cross-origin resource sharing
- **Input Validation**: Request validation at all layers
- **SQL Injection Prevention**: Parameterized queries with JPA
- **XSS Protection**: Input sanitization
- **CSRF Protection**: Token-based CSRF prevention
- **Role-Based Access**: Fine-grained authorization

---

## 📈 Performance Optimizations

### Mobile App
- Lazy loading of images with Coil
- Local caching with Room database
- Efficient state management with Compose
- Coroutines for async operations
- Optimized network calls with Ktor

### Admin Panel
- Server-side rendering with Next.js
- Code splitting and lazy loading
- Image optimization
- Turbopack for fast builds
- React Server Components

### Backend
- Database indexing for fast queries
- Connection pooling
- Caching strategies
- Async processing with coroutines
- Optimized JPA queries
- Docker containerization for scalability

---

## 🧪 Testing

### Backend
```bash
cd springboot-api
./gradlew test
```

### Admin Panel
```bash
cd nextjs-admin-panel
npm run test
```

### HTTP Tests
The backend includes comprehensive HTTP test files in `springboot-api/http-tests/` for manual API testing.

---

## 📦 Deployment

### Backend (Docker)

```bash
cd springboot-api

# Build and push to Docker Hub
./gradlew jib

# Or use Docker Compose
docker-compose up -d
```

### Admin Panel (Vercel)

```bash
cd nextjs-admin-panel
npm run build
# Deploy to Vercel, Netlify, or any Node.js host
```

### Mobile App

- **Android**: Generate signed APK/AAB via Android Studio
- **iOS**: Archive and distribute via Xcode

---

## 🤝 Contributing

This is a portfolio project demonstrating full-stack development capabilities. Feel free to explore the code and reach out with questions!

---

## 📄 License

This project is part of a professional portfolio.


---

## 🎓 Skills Demonstrated

This project showcases expertise in:

✅ **Mobile Development**: Kotlin Multiplatform, Compose, iOS/Android  
✅ **Backend Development**: Spring Boot, Kotlin, REST APIs, WebSocket  
✅ **Frontend Development**: Next.js, React, TypeScript, TailwindCSS  
✅ **Database Design**: PostgreSQL, JPA, Flyway migrations  
✅ **Authentication**: JWT, OAuth2, Security best practices  
✅ **Architecture**: Clean Architecture, MVVM, Repository Pattern  
✅ **DevOps**: Docker, Docker Compose, CI/CD ready  
✅ **API Design**: RESTful principles, comprehensive documentation  
✅ **Real-time Features**: WebSocket, live updates  
✅ **Testing**: Unit tests, integration tests, HTTP tests  
✅ **Version Control**: Git, modular project structure  

---

<div align="center">

**⭐ If you find this project interesting, please consider giving it a star! ⭐**

Made with ❤️ and lots of ☕

</div>
