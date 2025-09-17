# Active Hive - Event Management Platform

A modern, full-stack event management platform built with Kotlin and Ktor on the backend with a responsive frontend using HTMX, TailwindCSS, and vanilla JavaScript.

## 🌟 Key Features

### Authentication & User Management
- Multi-provider authentication (Email/Password and Google OAuth)
- JWT token-based authentication
- Password reset functionality with email verification
- User profiles with customizable images (supports both local upload and external URLs)
- Robust role-based authorization (admin vs regular users)

### Event Management
- Create and manage events with detailed information
- Rich event details (title, date, time, location, description)
- Event capacity tracking
- Automatic waiting list management
- Participant approval workflow
- Past/upcoming event filtering

### Real-time Interactions
- HTMX-powered fast and responsive UI with minimal JavaScript
- Server-sent updates for event status changes
- Dynamic participant management

### Social Features
- User profile with event history (hosted, attended, attending, waitlisted)
- Google Maps integration for event locations
- Event sharing capabilities

## 🔧 Technical Implementation

### Architecture
- Clean architecture with clear separation of concerns
- Repository pattern for data access
- Use case driven business logic
- Exposed SQL framework for database operations
- Dependency injection pattern

### Backend (Kotlin/Ktor)
- RESTful API endpoints
- Asynchronous request handling with coroutines
- Efficient database querying with Exposed
- JWT token generation and validation
- Input validation and error handling
- File upload management

### Frontend
- HTMX for seamless partial page updates without SPA complexity
- Progressive enhancement approach
- TailwindCSS for utility-first styling
- Mobile-responsive design
- Vanilla JavaScript for enhanced interactivity
- Modular component architecture

### Security Features
- Password hashing and salting
- CSRF protection
- Input validation and sanitization
- Secure authentication flows
- XSS prevention

### Performance Optimizations
- Efficient database queries
- Partial page updates rather than full reloads
- Lazy loading of resources
- Minimal JavaScript footprint
- Optimized image delivery

## 🧠 Advanced Techniques

### HTMX Integration
- Server-side rendering with partial updates
- Clean URL management with history API
- Transition animations between states
- Form validation with live feedback
- Optimistic UI updates

### Kotlin/Ktor Features
- Structured concurrency with coroutines
- Extension functions for reusable components
- Type-safe HTML DSL
- Route organization with type-safe builders
- Suspension functions for non-blocking operations

### Database Design
- Normalized relational schema
- Foreign key constraints for data integrity
- Indexes for query performance
- Transaction management for data consistency

## 🚀 Development Practices

- Modular code organization
- Reusable components
- Clean, documented code
- Type safety throughout the application
- Error handling and graceful degradation
- Responsive design for all device sizes

## 💻 Technologies

- **Backend**: Kotlin, Ktor, Exposed, Logback
- **Database**: PostgreSQL/MySQL
- **Authentication**: JWT, OAuth 2.0
- **Frontend**: HTMX, TailwindCSS, JavaScript
- **Templating**: Kotlin HTML DSL
- **Build Tool**: Gradle

---

This project demonstrates proficiency in modern web development techniques, including server-side rendering with partial updates (HTMX), functional programming with Kotlin, and responsive design with TailwindCSS - all while maintaining clean architecture and separation of concerns. 