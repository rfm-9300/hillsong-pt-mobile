# API Models Documentation

This document outlines all the data models used in the Hillsong PT Mobile API. Use this as a reference when implementing the mobile application.

## Table of Contents

- [User Models](#user-models)
- [Event Models](#event-models)
- [Service Models](#service-models)
- [Post Models](#post-models)
- [Kids Models](#kids-models)
- [Attendance Models](#attendance-models)
- [Image Models](#image-models)

---

## User Models

### User

Main user entity for authentication and basic user data.

```json
{
  "id": 1,
  "email": "user@example.com",
  "password": "hashed_password",
  "salt": "password_salt",
  "verified": true,
  "createdAt": "2024-01-15T10:30:00",
  "verificationToken": "token_string",
  "profile": UserProfile,
  "googleId": "google_oauth_id",
  "facebookId": "facebook_oauth_id",
  "authProvider": "LOCAL|GOOGLE|FACEBOOK",
  "resetToken": "reset_token_string",
  "resetTokenExpiresAt": 1705312200000
}
```

### UserProfile

Extended user information and profile data.

```json
{
  "id": 1,
  "userId": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+351912345678",
  "joinedAt": "2024-01-15T10:30:00",
  "hostedEvents": [Event],
  "attendedEvents": [Event],
  "waitingListEvents": [Event],
  "attendingEvents": [Event],
  "profileImagePath": "/uploads/profiles/user1.jpg",
  "isAdmin": false
}
```

### AuthProvider Enum

```
LOCAL, GOOGLE, FACEBOOK
```

---

## Event Models

### Event

Main event entity for church events and activities.

```json
{
  "id": 1,
  "title": "Sunday Service",
  "headerImagePath": "/uploads/events/event1.jpg",
  "description": "Weekly Sunday service",
  "date": "2024-01-21T10:00:00",
  "location": "Main Sanctuary",
  "attendees": [UserProfile],
  "organizerId": 1,
  "createdAt": "2024-01-15T10:30:00",
  "organizerName": "Pastor John",
  "maxAttendees": 100,
  "needsApproval": false,
  "waitingList": [EventWaitingList]
}
```

### EventAttendee

Represents a user attending an event.

```json
{
  "event": 1,
  "user": UserProfile,
  "joinedAt": "2024-01-16T14:30:00"
}
```

### EventWaitingList

Represents users on the waiting list for an event.

```json
{
  "eventId": 1,
  "user": UserProfile,
  "joinedAt": "2024-01-16T14:30:00"
}
```

---

## Service Models

### Service

Church service information.

```json
{
  "id": 1,
  "name": "Sunday Morning Service",
  "description": "Weekly worship service",
  "startTime": "2024-01-21T10:00:00",
  "endTime": "2024-01-21T11:30:00",
  "location": "Main Sanctuary",
  "serviceType": "REGULAR",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

### ServiceType Enum

```
REGULAR, SPECIAL, YOUTH, KIDS, PRAYER, WORSHIP
```

---

## Post Models

### Post

Blog posts and announcements.

```json
{
  "id": 1,
  "userId": 1,
  "title": "Welcome to Our Church",
  "content": "We are excited to welcome new members...",
  "date": "2024-01-15T10:30:00",
  "likes": 25,
  "headerImagePath": "/uploads/posts/post1.jpg"
}
```

---

## Kids Models

### Kid

Child information for kids services.

```json
{
  "id": 1,
  "familyId": 1,
  "firstName": "Emma",
  "lastName": "Smith",
  "dateOfBirth": "2018-05-15",
  "allergies": "Peanuts, dairy",
  "notes": "Needs inhaler for asthma"
}
```

### KidsService

Kids service information.

```json
{
  "id": 1,
  "serviceId": 1,
  "name": "Little Lights",
  "description": "Service for ages 3-6",
  "ageGroupMin": 3,
  "ageGroupMax": 6,
  "maxCapacity": 20,
  "location": "Kids Room A",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

### KidsCheckIn

Check-in/out tracking for kids.

```json
{
  "id": 1,
  "kidsServiceId": 1,
  "kidId": 1,
  "checkedInBy": 1,
  "checkInTime": "2024-01-21T09:45:00",
  "checkOutTime": "2024-01-21T11:45:00",
  "checkedOutBy": 1,
  "notes": "Had a great time",
  "status": "CHECKED_OUT"
}
```

### CheckInStatus Enum

```
CHECKED_IN, CHECKED_OUT, EMERGENCY
```

---

## Attendance Models

### Attendance

General attendance tracking for all event types.

```json
{
  "id": 1,
  "eventType": "SERVICE",
  "eventId": 1,
  "userId": 1,
  "kidId": null,
  "checkedInBy": 2,
  "checkInTime": "2024-01-21T09:55:00",
  "checkOutTime": "2024-01-21T11:35:00",
  "checkedOutBy": 2,
  "status": "CHECKED_OUT",
  "notes": "Regular attendance",
  "createdAt": "2024-01-21T09:55:00"
}
```

### AttendanceWithDetails

Attendance with additional context information.

```json
{
  "attendance": Attendance,
  "attendeeName": "John Doe",
  "eventName": "Sunday Service",
  "checkedInByName": "Volunteer Mary",
  "checkedOutByName": "Volunteer Mary"
}
```

### EventType Enum

```
EVENT, SERVICE, KIDS_SERVICE
```

### AttendanceStatus Enum

```
CHECKED_IN, CHECKED_OUT, EMERGENCY, NO_SHOW
```

---

## Image Models

### ImageHash

Image hash storage for duplicate detection.

```json
{
  "id": 1,
  "imagePath": "/uploads/images/image1.jpg",
  "hash": 1234567890
}
```

---

## API Endpoints & Responses

### Authentication Endpoints

#### POST `/api/auth/login`

**Request:**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "jwt_token_string"
  }
}
```

#### POST `/api/auth/google-login`

**Request:**

```json
{
  "googleToken": "google_oauth_token"
}
```

#### POST `/api/auth/facebook-login`

**Request:**

```json
{
  "facebookToken": "facebook_oauth_token"
}
```

#### POST `/api/auth/signup`

**Request:**

```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### POST `/api/auth/verify`

**Request:**

```json
{
  "token": "verification_token"
}
```

#### POST `/api/auth/request-password-reset`

**Request:**

```json
{
  "email": "user@example.com"
}
```

#### POST `/api/auth/reset-password`

**Request:**

```json
{
  "token": "reset_token",
  "newPassword": "new_password123"
}
```

---

### Event Endpoints

#### GET `/api/events`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "events": [Event]
  }
}
```

#### GET `/api/events/{id}`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "event": Event
  }
}
```

#### POST `/api/events`

**Request:**

```json
{
  "title": "New Event",
  "description": "Event description",
  "date": "2024-01-21T10:00:00",
  "location": "Main Hall",
  "maxAttendees": 100,
  "needsApproval": false
}
```

#### PUT `/api/events/update`

**Request:**

```json
{
  "id": 1,
  "title": "Updated Event",
  "description": "Updated description",
  "date": "2024-01-21T10:00:00",
  "location": "Main Hall",
  "maxAttendees": 150
}
```

#### DELETE `/api/events/delete`

**Request:**

```json
{
  "id": 1
}
```

#### POST `/api/events/join`

**Request:**

```json
{
  "eventId": 1
}
```

#### POST `/api/events/approve`

**Request:**

```json
{
  "eventId": 1,
  "userId": 2
}
```

#### POST `/api/events/remove-user`

**Request:**

```json
{
  "eventId": 1,
  "userId": 2
}
```

---

### Post Endpoints

#### GET `/api/posts`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "postList": [Post]
  }
}
```

#### GET `/api/posts/{id}`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "post": Post
  }
}
```

#### POST `/api/posts/create`

**Request:**

```json
{
  "title": "New Post",
  "content": "Post content here...",
  "headerImagePath": "/uploads/posts/image.jpg"
}
```

#### PUT `/api/posts/update`

**Request:**

```json
{
  "id": 1,
  "title": "Updated Post",
  "content": "Updated content..."
}
```

#### DELETE `/api/posts/delete`

**Request:**

```json
{
  "id": 1
}
```

---

### User & Profile Endpoints

#### GET `/api/users`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "users": [User]
  }
}
```

#### GET `/api/profile`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "profile": UserProfile
  }
}
```

#### PUT `/api/profile/update`

**Request:**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+351912345678",
  "profileImagePath": "/uploads/profiles/image.jpg"
}
```

---

### Service Endpoints

#### GET `/api/services`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "services": [Service]
  }
}
```

#### GET `/api/services/active`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "services": [Service]
  }
}
```

#### GET `/api/services/{id}`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "service": Service
  }
}
```

#### GET `/api/services/type/{type}`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "services": [Service]
  }
}
```

#### POST `/api/services`

**Request:**

```json
{
  "name": "Sunday Service",
  "description": "Weekly worship",
  "startTime": "2024-01-21T10:00:00",
  "endTime": "2024-01-21T11:30:00",
  "location": "Main Sanctuary",
  "serviceType": "REGULAR"
}
```

#### PUT `/api/services/{id}`

**Request:**

```json
{
  "name": "Updated Service",
  "description": "Updated description",
  "startTime": "2024-01-21T10:00:00",
  "endTime": "2024-01-21T11:30:00",
  "location": "Main Sanctuary"
}
```

#### DELETE `/api/services/{id}`

#### POST `/api/services/{id}/activate`

#### POST `/api/services/{id}/deactivate`

---

### Kids Service Endpoints

#### GET `/api/kids-services`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "kidsServices": [KidsService]
  }
}
```

#### GET `/api/kids-services/active`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "kidsServices": [KidsService]
  }
}
```

#### GET `/api/kids-services/{id}`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "kidsService": KidsService
  }
}
```

#### GET `/api/kids-services/service/{serviceId}`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "kidsServices": [KidsService]
  }
}
```

#### GET `/api/kids-services/{id}/capacity`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "currentCount": 15,
    "maxCapacity": 20,
    "kidsServiceId": 1
  }
}
```

#### POST `/api/kids-services`

**Request:**

```json
{
  "serviceId": 1,
  "name": "Little Lights",
  "description": "Ages 3-6",
  "ageGroupMin": 3,
  "ageGroupMax": 6,
  "maxCapacity": 20,
  "location": "Kids Room A"
}
```

#### PUT `/api/kids-services/{id}`

**Request:**

```json
{
  "name": "Updated Kids Service",
  "description": "Updated description",
  "ageGroupMin": 3,
  "ageGroupMax": 7,
  "maxCapacity": 25
}
```

#### DELETE `/api/kids-services/{id}`

#### POST `/api/kids-services/check-in`

**Request:**

```json
{
  "kidsServiceId": 1,
  "kidId": 1,
  "notes": "First time attending"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Check-in successful",
  "data": {
    "checkIn": KidsCheckIn
  }
}
```

#### POST `/api/kids-services/check-out`

**Request:**

```json
{
  "checkInId": 1,
  "notes": "Had a great time"
}
```

#### GET `/api/kids-services/{id}/active-checkins`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "checkIns": [KidsCheckIn]
  }
}
```

#### GET `/api/kids-services/checkin-history/{kidId}`

**Response:**

```json
{
  "success": true,
  "message": "",
  "data": {
    "checkIns": [KidsCheckIn]
  }
}
```

#### PUT `/api/kids-services/checkin/{id}/status`

**Request:**

```json
{
  "status": "EMERGENCY",
  "notes": "Parent called for pickup"
}
```

---

## Standard API Response Format

All API responses follow this structure:

### Success Response

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": <ApiResponseData>
}
```

### Error Response

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

### Attendance Endpoints

Based on the ApiResponse.kt file, there are attendance-related endpoints that would follow these patterns:

#### Attendance Response Types

```json
{
  "success": true,
  "message": "",
  "data": {
    "attendance": Attendance
  }
}
```

#### Attendance List Response

```json
{
  "success": true,
  "message": "",
  "data": {
    "attendances": [AttendanceWithDetails]
  }
}
```

#### Attendance Stats Response

```json
{
  "success": true,
  "message": "",
  "data": {
    "stats": AttendanceStats
  }
}
```

---

## Common Field Types

- **ID Fields**: Integer, nullable for creation requests
- **DateTime Fields**: ISO 8601 format string (e.g., "2024-01-15T10:30:00")
- **Boolean Fields**: true/false
- **Text Fields**: String with specified max lengths
- **Enum Fields**: String values from predefined sets
- **Reference Fields**: Integer IDs referencing other models

---

## Notes for Mobile Development

1. **Authentication**: Use JWT tokens for API authentication (Bearer token in Authorization header)
2. **Image Paths**: All image paths are relative to the server's upload directory
3. **DateTime Handling**: All timestamps are in server timezone, convert as needed
4. **Nullable Fields**: Check for null values, especially in optional fields
5. **Enums**: Validate enum values on the client side before sending requests
6. **Relationships**: Some models include nested objects (like User with UserProfile)
7. **Standard Response Format**: All API responses follow the ApiResponse structure with success, message, and data fields
8. **Error Handling**: Always check the `success` field before processing `data`
9. **Content-Type**: Use `application/json` for all POST/PUT requests
10. **Path Parameters**: Replace `{id}`, `{eventId}`, etc. with actual values in URLs
11. **HTTP Methods**:
    - GET for retrieving data
    - POST for creating new resources
    - PUT for updating existing resources
    - DELETE for removing resources
12. **Kids Service Check-ins**: Use specific endpoints for kids check-in/out operations
13. **Event Management**: Events support approval workflows and waiting lists
14. **Service Types**: Services can be filtered by type (REGULAR, SPECIAL, YOUTH, KIDS, PRAYER, WORSHIP)
