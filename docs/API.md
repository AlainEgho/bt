# Backend API Documentation

java  "-Dspring.profiles.active=prod" -jar .\backend-0.0.1-SNAPSHOT.jar
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"           
 .\mvnw.cmd install 

 npm run start
 npm run serve:prod    
  npx http-server  -p 4200 --fallback index.html

from front end redirect
deploy on prod remove ssl



Base URL: `http://localhost:8081` (or your deployed backend URL).

All JSON APIs use **Content-Type: application/json**.

---

## Response format

Successful responses use this wrapper:

```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... }
}
```

Error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## Authentication

Endpoints under `/api/auth/**` do **not** require a JWT. For protected endpoints, send:

```
Authorization: Bearer <accessToken>
```

---

## 1. Auth API

### 1.1 Sign up

Register a new user. Returns a JWT and user info. A verification email is sent if email is configured.

| Method | URL |
|--------|-----|
| POST   | `/api/auth/signup` |

**Request body**

| Field        | Type   | Required | Description                          |
|-------------|--------|----------|--------------------------------------|
| firstName   | string | yes      | Max 100 chars                        |
| lastName    | string | yes      | Max 100 chars                        |
| email       | string | yes      | Valid email, max 255                  |
| password    | string | yes      | Min 6, max 100                       |
| address     | string | no       | Max 500 chars                         |
| phoneNumber | string | no       | Max 30 chars                          |
| country     | string | no       | Max 100 chars                         |
| userType    | string | no       | `INDIVIDUAL` or `BUSINESS`; default `INDIVIDUAL` |

**Example**

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "password": "securePass123",
  "address": "456 Oak Ave",
  "phoneNumber": "+1 555 123 4567",
  "country": "United States",
  "userType": "INDIVIDUAL"
}
```

**Success (201 Created)**

```json
{
  "success": true,
  "message": "Registration successful. Please check your email to verify your account.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "id": 1,
    "email": "jane@example.com",
    "firstName": "Jane",
    "lastName": "Doe",
    "country": "United States",
    "userType": "INDIVIDUAL",
    "emailVerified": false,
    "roles": ["USER"]
  }
}
```

---

### 1.2 Login

Authenticate and receive a JWT.

| Method | URL |
|--------|-----|
| POST   | `/api/auth/login` |

**Request body**

| Field     | Type   | Required |
|----------|--------|----------|
| email    | string | yes      |
| password | string | yes      |

**Example**

```json
{
  "email": "jane@example.com",
  "password": "securePass123"
}
```

**Success (200 OK)**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "id": 1,
    "email": "jane@example.com",
    "firstName": "Jane",
    "lastName": "Doe",
    "country": "United States",
    "userType": "INDIVIDUAL",
    "emailVerified": true,
    "roles": ["USER"]
  }
}
```

---

### 1.3 Verify email

Mark the user's email as verified using the token sent by email.

| Method | URL                    |
|--------|------------------------|
| GET    | `/api/auth/verify-email?token=<token>` |
| POST   | `/api/auth/verify-email?token=<token>` |

**Success (200 OK)**

```json
{
  "success": true,
  "message": "Email verified successfully",
  "data": null
}
```

**Errors:** 400 if token is invalid, already used, or expired.

---

### 1.4 Validate token (for microservices)

Check whether a JWT is valid. Intended for other microservices that receive a JWT from the client and need to validate it with the auth service.

| Method | URL                |
|--------|--------------------|
| GET    | `/api/auth/validate-token` |

**Headers**

| Header          | Required | Description                    |
|-----------------|----------|--------------------------------|
| Authorization   | yes      | `Bearer <jwt>`                 |

**Success (200 OK) – valid token**

```json
{
  "success": true,
  "message": "Token validation result",
  "data": {
    "valid": true,
    "userId": 1,
    "email": "jane@example.com"
  }
}
```

**Success (200 OK) – invalid / missing / expired token**

```json
{
  "success": true,
  "message": "Token validation result",
  "data": {
    "valid": false,
    "userId": null,
    "email": null
  }
}
```

**Example call from another service**

```http
GET /api/auth/validate-token HTTP/1.1
Host: localhost:8081
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

No authentication is required to call this endpoint; the token to validate is provided in the header.

---

## 2. Online users and messaging

When a user signs in (login), they are marked as **signed in**. Their **last activity** is updated on every authenticated request. If there is **no activity for more than one hour**, the sign-in record is removed (they no longer appear as online). Other users can list **online users** and **send messages** to each other.

**Auth:** All endpoints require `Authorization: Bearer <token>`.

**How the second user (receiver) gets messages:** Both users use the same APIs. The receiver (second user) fetches the conversation by calling **GET `/api/messages/conversation/{userId}`** with the **sender’s user id**. They can also use **GET `/api/messages/conversations`** to list all threads (including the one with the sender); each item has `peerUserId`, last message preview, and `unreadCount`. So: User A sends to User B → User B calls `GET /api/messages/conversation/{userA_id}` (or opens the thread from `GET /api/messages/conversations`) to see the messages.

### 2.1 List online users

Returns users currently signed in (activity within the last hour). Excludes the current user. Use this so the frontend can show "who is online" and let the user start a conversation.

| Method | URL                 |
|--------|---------------------|
| GET    | `/api/users/online` |

**Success (200 OK)**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 2,
      "email": "jane@example.com",
      "firstName": "Jane",
      "lastName": "Doe",
      "country": "United States"
    }
  ]
}
```

### 2.2 Send a message

Send a message from the current user to another user.

| Method | URL             |
|--------|-----------------|
| POST   | `/api/messages` |

**Request body**

| Field       | Type   | Required | Description        |
|------------|--------|----------|--------------------|
| receiverId | number | yes      | User ID of recipient |
| content    | string | yes      | Message text (max 10000 chars) |

**Example**

```json
{
  "receiverId": 2,
  "content": "Hi, are you available?"
}
```

**Success (201 Created)**

```json
{
  "success": true,
  "message": "Message sent",
  "data": {
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "content": "Hi, are you available?",
    "createdAt": "2025-02-21T10:00:00Z",
    "readAt": null,
    "fromCurrentUser": true
  }
}
```

### 2.3 Get conversation with a user

Paginated messages between the current user and the given user (either direction).

| Method | URL                                |
|--------|------------------------------------|
| GET    | `/api/messages/conversation/{userId}` |

**Query params:** `page` (default 0), `size` (default 50).

**Success (200 OK):** `data` is a Spring Page of message objects (same shape as 2.2), ordered newest first.

### 2.4 List conversations

List all conversations (users the current user has exchanged messages with), with last message preview and unread count.

| Method | URL                      |
|--------|--------------------------|
| GET    | `/api/messages/conversations` |

**Success (200 OK)**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "peerUserId": 2,
      "peerFirstName": "Jane",
      "peerLastName": "Doe",
      "peerEmail": "jane@example.com",
      "lastMessagePreview": "Hi, are you available?",
      "lastMessageAt": "2025-02-21T10:00:00Z",
      "unreadCount": 0
    }
  ]
}
```

### 2.5 Mark message as read

Mark a message as read (receiver only). Sets `readAt` to the current time.

| Method | URL                     |
|--------|-------------------------|
| PATCH  | `/api/messages/{id}/read` |

**Success (200 OK):** `message`: "Message marked as read".

### 2.6 WebSocket – real-time new message (receiver)

The **receiver** can listen
**Endpoint (with SockJS fallback):** `GET /ws` (e.g. `ws://localhost:8081/ws` or `http://localhost:8081/ws` with SockJS).

**Authentication:** Pass the JWT as a query parameter: `ws://localhost:8081/ws?token=YOUR_JWT_ACCESS_TOKEN`

**STOMP:** Connect with the token above, then subscribe to `/user/queue/messages`. Incoming payload is the same shape as in 2.2 (id, senderId, receiverId, content, createdAt, readAt, fromCurrentUser). Only the receiver receives pushes.

---

## 3. Wall API

The **wall** is a global feed where **all authenticated users** can publish and read posts. Posts can be **text** and/or **image**. They are **automatically deleted after 1 month** (configurable). Listing supports **pagination and a maximum page size (limit)**.

**Auth:** All endpoints require `Authorization: Bearer <token>`.

### 3.1 Publish a post to the wall

| Method | URL               |
|--------|-------------------|
| POST   | `/api/wall/posts` |

**Request body**

| Field        | Type   | Required | Description                                                                 |
|--------------|--------|----------|-----------------------------------------------------------------------------|
| content      | string | no*      | Text content (max 10000 chars)                                              |
| imagePath    | string | no*      | Image URL or path (e.g. from `POST /api/image-uploads`). Ignored if `imageBase64` is set. |
| imageBase64  | string | no*      | Image as base64 (raw or data URL `data:image/png;base64,...`). Server uploads and saves it, then stores the resulting URL. |

\* At least one of `content`, `imagePath`, or `imageBase64` must be provided.

**Example (text only)**

```json
{
  "content": "Hello everyone! Here is my update."
}
```

**Example (text + image via URL)**  
Upload the image first via `POST /api/image-uploads`, then create the post with the returned URL:

```json
{
  "content": "Check out this photo",
  "imagePath": "http://localhost:8081/i/abc12"
}
```

**Example (text + image via base64)**  
Send the image as base64; the server saves it and stores the resulting URL:

```json
{
  "content": "Check out this photo",
  "imageBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
}
```

**Example (image only)**

```json
{
  "content": null,
  "imagePath": "http://localhost:8081/i/xyz"
}
```

Or image only with base64: `{ "imageBase64": "..." }`.

**Success (201 Created)**

```json
{
  "success": true,
  "message": "Post published",
  "data": {
    "id": 1,
    "authorId": 1,
    "authorFirstName": "Jane",
    "authorLastName": "Doe",
    "authorEmail": "jane@example.com",
    "content": "Hello everyone!",
    "imagePath": null,
    "createdAt": "2025-02-21T12:00:00Z"
  }
}
```

### 3.2 List wall posts (paginated)

All users can read the wall. Newest first. Posts older than 1 month are removed by a scheduled job.

| Method | URL               |
|--------|-------------------|
| GET    | `/api/wall/posts` |

**Query parameters**

| Param  | Type    | Default | Description                          |
|--------|---------|---------|--------------------------------------|
| page   | integer | 0       | Page number (0-based)                |
| size   | integer | 20      | Page size                            |
| limit  | integer | 100     | Maximum allowed page size (server caps `size` at this) |

**Success (200 OK):** `data` is a Spring Page of wall post objects (same shape as in 3.1), with pagination metadata (`totalElements`, `totalPages`, `number`, `size`, etc.).

**Note:** Posts are kept for 30 days (configurable via `app.wall.retention-days`), then deleted automatically.

---

## 4. Health for new messages over a WebSocket so they don’t have to poll. When someone sends them a message, the server pushes a `MessageResponse` to their subscribed queue.

| Method | URL             |
|--------|-----------------|
| GET    | `/api/health`   |

**Success (200 OK)**

```json
{
  "status": "UP"
}
```

(Note: This endpoint returns a plain object, not the standard `ApiResponse` wrapper.)

---

## 4. Invoices API

All invoice endpoints require authentication: `Authorization: Bearer <token>`.

Base path: `/api/invoices`.

### 3.1 List invoices

| Method | URL              |
|--------|------------------|
| GET    | `/api/invoices`  |

**Success (200 OK)**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "invoiceNumber": "INV-20250101-1",
      "userId": 1,
      "issueDate": "2025-01-01",
      "dueDate": "2025-01-31",
      "status": "DRAFT",
      "subtotal": 100.00,
      "taxAmount": 0,
      "total": 100.00,
      "notes": null,
      "createdAt": "2025-01-01T12:00:00Z",
      "updatedAt": null,
      "details": []
    }
  ]
}
```

---

### 3.2 Get invoice by ID

| Method | URL                   |
|--------|-----------------------|
| GET    | `/api/invoices/{id}`   |

**Success (200 OK):** `data` is a single invoice object (same shape as in the list).  
**Error (400):** Invoice not found or not owned by the current user.

---

### 3.3 Create invoice

| Method | URL              |
|--------|------------------|
| POST   | `/api/invoices`  |

**Request body**

| Field         | Type   | Required | Description                          |
|---------------|--------|----------|--------------------------------------|
| invoiceNumber | string | no       | Auto-generated if blank              |
| issueDate     | string | yes      | ISO date (yyyy-MM-dd)                |
| dueDate       | string | yes      | ISO date                             |
| taxAmount     | number | no       | Default 0                             |
| notes         | string | no       |                                      |
| details       | array  | no       | Line items (see below)                |

**Detail item**

| Field       | Type   | Required | Description        |
|------------|--------|----------|--------------------|
| description| string | yes      |                    |
| quantity   | number | no       | Default 1, > 0      |
| unitPrice  | number | no       | Default 0, ≥ 0      |
| sortOrder  | number | no       | Display order      |

**Example**

```json
{
  "issueDate": "2025-02-01",
  "dueDate": "2025-02-28",
  "taxAmount": 10,
  "notes": "Thank you",
  "details": [
    {
      "description": "Item A",
      "quantity": 2,
      "unitPrice": 50.00,
      "sortOrder": 0
    }
  ]
}
```

**Success (201 Created):** `data` is the created invoice.  
**Errors:** 400 if user not found or invoice number already exists.

---

### 3.4 Update invoice

| Method | URL                   |
|--------|-----------------------|
| PUT    | `/api/invoices/{id}`  |

**Request body**

| Field     | Type   | Required | Description                    |
|----------|--------|----------|--------------------------------|
| issueDate| string | yes      | ISO date                       |
| dueDate  | string | yes      | ISO date                       |
| status   | string | no       | DRAFT, SENT, PAID, CANCELLED   |
| taxAmount| number | no       |                                |
| notes    | string | no       |                                |
| details  | array  | no       | If provided, replaces all lines|

**Success (200 OK):** `data` is the updated invoice.  
**Error (400):** Invoice not found or not owned by the current user.

---

### 3.5 Delete invoice

| Method | URL                   |
|--------|-----------------------|
| DELETE | `/api/invoices/{id}`  |

**Success (200 OK):** `message`: "Invoice deleted", `data`: null.  
**Error (400):** Invoice not found or not owned by the current user.

---

## 5. Shorteners API

All shortener endpoints require authentication: `Authorization: Bearer <token>`.

Base path: `/api/shorteners`.

### 4.1 List my QR codes (short links)

Lists all short links (QR codes) generated by the current user. Use this or the equivalent shorteners list below.

| Method | URL                 |
|--------|---------------------|
| GET    | `/api/qr-codes`     |

**Success (200 OK):** Same shape as the list in 4.2. Each item has `id`, `shortCode`, `fullUrl`, `userId`, `clickCount`, `createdAt`, `expiresAt`, `active`. The frontend can render each as a QR code pointing to `{baseUrl}/s/{shortCode}`.

---

### 4.2 List short links (alternate path)

| Method | URL                 |
|--------|---------------------|
| GET    | `/api/shorteners`   |

**Success (200 OK)**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "shortCode": "abc12345",
      "fullUrl": "https://example.com/page",
      "userId": 1,
      "clickCount": 0,
      "createdAt": "2025-01-01T12:00:00Z",
      "expiresAt": null,
      "active": true
    }
  ]
}
```

---

### 4.3 Get short link by ID

| Method | URL                      |
|--------|--------------------------|
| GET    | `/api/shorteners/{id}`   |

**Success (200 OK):** `data` is a single shortener object.  
**Error (400):** Short link not found or not owned by the current user.

---

### 4.4 Create short link

| Method | URL                 |
|--------|---------------------|
| POST   | `/api/shorteners`   |

**Request body**

| Field     | Type   | Required | Description                                  |
|----------|--------|----------|----------------------------------------------|
| fullUrl  | string | yes      | Max 2048 chars; https:// added if missing   |
| shortCode| string | no       | 3–32 chars; generated if blank              |
| expiresAt| string| no       | ISO instant (UTC); optional expiry           |

**Request example (minimal – short code auto-generated)**

```json
{
  "fullUrl": "https://example.com/long-page"
}
```

**Request example (with optional shortCode and expiresAt)**

```json
{
  "fullUrl": "https://example.com/long-page",
  "shortCode": "mylink",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

**cURL example**

```bash
curl -X POST "http://localhost:8081/api/shorteners" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"fullUrl":"https://example.com/long-page","shortCode":"mylink"}'
```

**Success (201 Created) – response example**

```json
{
  "success": true,
  "message": "Short link created",
  "data": {
    "id": 1,
    "shortCode": "mylink",
    "fullUrl": "https://example.com/long-page",
    "userId": 1,
    "clickCount": 0,
    "createdAt": "2025-02-20T14:30:00Z",
    "expiresAt": null,
    "active": true
  }
}
```

The short link is then available at: `https://your-api-host/s/{shortCode}` (e.g. `https://your-api-host/s/mylink`).

**Errors:** 400 if user not found or short code already in use.

---

### 4.5 Update short link

| Method | URL                      |
|--------|--------------------------|
| PUT    | `/api/shorteners/{id}`   |

**Request body**

| Field     | Type    | Required | Description           |
|----------|---------|----------|-----------------------|
| fullUrl  | string  | no       | Max 2048              |
| expiresAt| string  | no       | ISO instant           |
| active   | boolean | no       | Enable/disable link   |

**Success (200 OK):** `data` is the updated short link.  
**Error (400):** Short link not found or not owned by the current user.

---

### 4.6 Delete short link

| Method | URL                      |
|--------|--------------------------|
| DELETE | `/api/shorteners/{id}`   |

**Success (200 OK):** `message`: "Short link deleted", `data`: null.  
**Error (400):** Short link not found or not owned by the current user.

---

## 6. Image uploads API (Base64)

Upload an image as Base64; it is stored on the server and accessible via a short link. Use the returned `imageUrl` in a QR code or shortener. **New feature:** separate from shorteners; uses its own entity and `/i/{code}` path.

All endpoints under `/api/image-uploads` require authentication: `Authorization: Bearer <token>`.

### 5.1 Upload image (Base64)

| Method | URL                     |
|--------|-------------------------|
| POST   | `/api/image-uploads`    |

**Request body**

| Field             | Type   | Required | Description                                      |
|-------------------|--------|----------|--------------------------------------------------|
| base64            | string | yes      | Raw Base64 string or data URL (data:image/png;base64,...) |
| contentType       | string | no       | e.g. image/png, image/jpeg; optional if base64 is a data URL |
| originalFileName  | string | no       | Optional filename                                |

**Example**

```json
{
  "base64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
  "contentType": "image/png",
  "originalFileName": "logo.png"
}
```

**Success (201 Created)**

```json
{
  "success": true,
  "message": "Image uploaded. Use imageUrl or /i/{shortCode} to view.",
  "data": {
    "id": 1,
    "shortCode": "a1b2c3d4",
    "imageUrl": "http://localhost:8081/i/a1b2c3d4",
    "contentType": "image/png",
    "originalFileName": "logo.png",
    "userId": 1,
    "createdAt": "2025-02-01T12:00:00Z"
  }
}
```

Use `imageUrl` (or `http://localhost:8081/i/{shortCode}`) in a QR code; scanning it will open the image.

### 5.2 List my image uploads

| Method | URL                     |
|--------|-------------------------|
| GET    | `/api/image-uploads`    |

**Success (200 OK):** `data` is an array of image upload responses (same shape as 5.1).

### 5.3 Get image upload by ID

| Method | URL                          |
|--------|------------------------------|
| GET    | `/api/image-uploads/{id}`    |

**Success (200 OK):** `data` is a single image upload response.  
**Error (400):** Not found or not owned by the current user.

### 5.4 Serve image by short code (public)

Returns the image file. No authentication. Use this URL in a QR code or short link.

| Method | URL        |
|--------|------------|
| GET    | `/i/{code}`|

**Success (200 OK):** Response body is the image binary; `Content-Type` is the stored type (e.g. image/png).  
**Error (404):** Short code not found or file missing.

---

## 7. Categories API

Base path: `/api/categories`. Categories use **UUID** as the identifier (not Long).

**Note:** `GET /api/categories` is **public** (no authentication). POST, PUT, DELETE require JWT. Categories have an **active** flag; list endpoints return **only active** categories (admin list returns all).

### 6.1 List all categories (public)

| Method | URL                 |
|--------|---------------------|
| GET    | `/api/categories`   |

**No authentication required.** Returns **active** categories from all users.

**Success (200 OK)**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "description": "Work",
      "imageUrl": "http://localhost:8081/api/categories/images/550e8400-e29b-41d4-a716-446655440000",
      "active": true,
      "userId": 1,
      "createdAt": "2025-02-01T12:00:00Z",
      "updatedAt": null
    }
  ]
}
```

**Note:** `imageUrl` is `null` if the category has no image. Only **active** categories are returned.

---

### 6.2 Get category by ID

| Method | URL                      |
|--------|--------------------------|
| GET    | `/api/categories/{id}`   |

**Path parameter:** `id` is a UUID (e.g. `550e8400-e29b-41d4-a716-446655440000`).  
**Auth:** JWT required.

**Success (200 OK):** `data` is a single category object.  
**Error (400):** Category not found or not owned by the current user.

---

### 6.3 Create category

| Method | URL                 |
|--------|---------------------|
| POST   | `/api/categories`   |

**Auth:** JWT required.

**Request body**

| Field             | Type    | Required | Description                                      |
|-------------------|--------|----------|--------------------------------------------------|
| description       | string  | yes      | Max 500 characters                               |
| imageBase64       | string  | no       | Base64 image data (raw or data URL format)       |
| imageContentType  | string  | no       | e.g. image/png, image/jpeg; required if imageBase64 is not a data URL |
| active            | boolean | no       | Default true. If false, category is inactive and excluded from list endpoints. |

**Example**

```json
{
  "description": "Personal",
  "imageBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
  "imageContentType": "image/png",
  "active": true
}
```

**Success (201 Created)**

```json
{
  "success": true,
  "message": "Category created",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "description": "Personal",
    "imageUrl": "http://localhost:8081/api/categories/images/550e8400-e29b-41d4-a716-446655440000",
    "active": true,
    "userId": 1,
    "createdAt": "2025-02-01T12:00:00Z",
    "updatedAt": null
  }
}
```

**Errors:** 
- 400 if user not found or validation fails.
- 500 if image save fails (e.g. invalid Base64, disk error).

---

### 6.4 Update category

| Method | URL                      |
|--------|--------------------------|
| PUT    | `/api/categories/{id}`   |

**Auth:** JWT required.

**Request body**

| Field             | Type    | Required | Description                                      |
|-------------------|--------|----------|--------------------------------------------------|
| description       | string  | yes      | Max 500 characters                               |
| imageBase64       | string  | no       | Base64 image data (raw or data URL format). If provided, replaces existing image. |
| imageContentType  | string  | no       | e.g. image/png, image/jpeg; required if imageBase64 is not a data URL |
| active            | boolean | no       | If provided, sets active/inactive. Inactive categories are excluded from list endpoints. |

**Example**

```json
{
  "description": "Personal Updated",
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD...",
  "active": false
}
```

**Success (200 OK):** `data` is the updated category (includes `imageUrl` if image was provided).  
**Errors:** 
- 400 if category not found or not owned by the current user.
- 500 if image save fails.

---

### 6.5 Delete category

| Method | URL                      |
|--------|--------------------------|
| DELETE | `/api/categories/{id}`   |

**Auth:** JWT required.

**Success (200 OK):** `message`: "Category deleted", `data`: null.  
**Errors:** 
- 400 if category not found or not owned by the current user.
- 500 if image deletion fails.

---

### 6.6 Serve category image (public)

Returns the image file for a category. No authentication required.

| Method | URL                                    |
|--------|----------------------------------------|
| GET    | `/api/categories/images/{categoryId}` |

**Path parameter:** `categoryId` is a UUID.

**Success (200 OK):** Response body is the image binary; `Content-Type` is the stored type (e.g. image/png).  
**Error (404):** Category not found or image missing.

---

## 8. Items API

Base path: `/api/items`. Items have String id (UUID string), same image/active pattern as categories, and belong to a **category**. They can have optional **detail** (quantity, price), **address** (name, longitude, latitude), and **contact** (firstName, lastName, phone).

- **GET /api/items** is **public** (active items only). POST, PUT, DELETE require JWT.
- **GET /api/items/images/** is public for serving item images.

### 7.1 List all items (public)

| Method | URL           |
|--------|---------------|
| GET    | `/api/items`  |

Returns **active** items from all users. Each item includes `id`, `description`, `imageUrl`, `active`, `userId`, `categoryId`, `detail` (quantity, price), `address` (addressName, longitude, latitude), `contact` (firstName, lastName, phone), `createdAt`, `updatedAt`.

**Success (200 OK) – response example**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "description": "Wedding cake",
      "imageUrl": "http://localhost:8081/api/items/images/1/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "active": true,
      "userId": 1,
      "categoryId": "f454e28a-00ec-4ea9-bd44-2729b947b6bf",
      "detail": {
        "id": 1,
        "quantity": 2,
        "price": 450.5000
      },
      "address": {
        "id": 1,
        "addressName": "Grand Ballroom",
        "longitude": -73.9857,
        "latitude": 40.7484
      },
      "contact": {
        "id": 1,
        "firstName": "Jane",
        "lastName": "Doe",
        "phone": "+1 555 123 4567"
      },
      "createdAt": "2025-02-16T12:00:00Z",
      "updatedAt": null
    },
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "description": "Floral centerpieces",
      "imageUrl": null,
      "active": true,
      "userId": 1,
      "categoryId": "f454e28a-00ec-4ea9-bd44-2729b947b6bf",
      "detail": null,
      "address": null,
      "contact": null,
      "createdAt": "2025-02-16T11:30:00Z",
      "updatedAt": null
    }
  ]
}
```

`detail`, `address`, and `contact` are optional; they are `null` when not set. `imageUrl` is `null` when the item has no image.

### 7.2 Get items by category (public)

| Method | URL                            |
|--------|--------------------------------|
| GET    | `/api/items/category/{categoryId}` |

Returns **active** items that belong to the given category. No authentication required.

**Success (200 OK):** `data` is an array of item responses (same shape as 7.1).

### 7.3 List buyers of my items (item owner)

| Method | URL                 |
|--------|---------------------|
| GET    | `/api/items/buyers` |

**Auth:** JWT required. For the **logged-in user** (item owner), returns the list of **carts** that contain any of his items. Each entry includes the cart owner (buyer): `id`, `email`, `firstName`, `lastName`, plus **cart** fields: `cartId`, `cartEventDate`, `cartCreatedAt`.

**Success (200 OK) – response example**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 2,
      "email": "buyer@example.com",
      "firstName": "John",
      "lastName": "Buyer",
      "cartId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "cartEventDate": "2025-06-15",
      "cartCreatedAt": "2025-02-16T14:30:00Z"
    },
    {
      "id": 5,
      "email": "another@example.com",
      "firstName": "Alice",
      "lastName": "Shopper",
      "cartId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "cartEventDate": "2025-07-20",
      "cartCreatedAt": "2025-02-17T09:00:00Z"
    }
  ]
}
```

**Note:** One row per cart that contains the seller's items (same buyer may appear for multiple carts). Empty array if no one has added the logged-in user's items to a cart.

### 7.4 Get item by ID

| Method | URL                |
|--------|--------------------|
| GET    | `/api/items/{id}`  |

**Auth:** JWT required. **Success (200 OK):** single item with detail, address, contact. **Error (400):** Item not found or not owned by user.

### 7.5 Create item

| Method | URL           |
|--------|---------------|
| POST   | `/api/items`  |

**Auth:** JWT required.

**Request body:** `description` (required), `categoryId` (required), `imageBase64`, `imageContentType`, `active`, `detail` (optional: `quantity`, `price`), `address` (optional: `addressName`, `longitude`, `latitude`), `contact` (optional: `firstName`, `lastName`, `phone`).

**Request body example (full – with detail, address, contact, image):**

```json
{
  "description": "Wedding cake - 3 tiers",
  "categoryId": "f454e28a-00ec-4ea9-bd44-2729b947b6bf",
  "imageBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
  "imageContentType": "image/png",
  "active": true,
  "detail": {
    "quantity": 2,
    "price": 450.50
  },
  "address": {
    "addressName": "Grand Ballroom, 5th Ave",
    "longitude": -73.9857,
    "latitude": 40.7484
  },
  "contact": {
    "firstName": "Jane",
    "lastName": "Doe",
    "phone": "+1 555 123 4567"
  }
}
```

**Minimal request (only required fields):**

```json
{
  "description": "Floral centerpieces",
  "categoryId": "f454e28a-00ec-4ea9-bd44-2729b947b6bf"
}
```

**Success (201 Created):** Item with image saved first, then entity persisted (same flow as categories).

### 7.6 Update item

| Method | URL                |
|--------|--------------------|
| PUT    | `/api/items/{id}`  |

**Auth:** JWT required. Same fields as create (except `categoryId`). Image/detail/address/contact can be updated or added.

### 7.7 Delete item

| Method | URL                |
|--------|--------------------|
| DELETE | `/api/items/{id}`  |

**Auth:** JWT required.

### 7.8 Serve item image (public)

| Method | URL                                  |
|--------|--------------------------------------|
| GET    | `/api/items/images/{userId}/{itemId}` |
| GET    | `/api/items/images/{itemId}`         |

Returns the image binary. **Error (404):** Item not found or image missing.

---

## 9. Carts API

Base path: `/api/carts`. A user can have multiple carts. Each cart contains multiple items with quantities. Cart has a **status** (PENDING, PAID, CANCELLED, COMPLETED), optional **payment method** (ONLINE, OFFLINE), and an optional event date. Payment is processed via **Process payment** (creates a **transaction** and updates cart status). Transaction history is available per cart and globally for the user.

**All cart endpoints require authentication:** `Authorization: Bearer <token>`.

### 8.1 List my carts

| Method | URL           |
|--------|---------------|
| GET    | `/api/carts`  |

**Query parameter (optional):** `status` (PENDING, PAID, CANCELLED, COMPLETED) to filter by status.

**Success (200 OK) – response example:**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": "c1d2e3f4-a5b6-7890-cdef-123456789012",
      "userId": 1,
      "status": "PENDING",
      "paymentMethod": "ONLINE",
      "eventDate": "2025-06-15",
      "items": [
        {
          "id": 1,
          "itemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
          "quantity": 2
        },
        {
          "id": 2,
          "itemId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
          "quantity": 1
        }
      ],
      "createdAt": "2025-02-16T10:00:00Z",
      "updatedAt": null
    }
  ]
}
```

### 8.2 Get cart by ID

| Method | URL                |
|--------|--------------------|
| GET    | `/api/carts/{id}`  |

**Success (200 OK):** Single cart with items. **Error (400):** Cart not found or not owned by user.

### 8.3 Create cart

| Method | URL           |
|--------|---------------|
| POST   | `/api/carts`  |

**Request body example:**

```json
{
  "status": "PENDING",
  "paymentMethod": "ONLINE",
  "eventDate": "2025-06-15",
  "items": [
    {
      "itemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "quantity": 2
    },
    {
      "itemId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "quantity": 1
    }
  ]
}
```

**Fields:**
- `status` (optional): PENDING (default), PAID, CANCELLED, COMPLETED
- `paymentMethod` (optional): ONLINE or OFFLINE
- `eventDate` (optional): Date in YYYY-MM-DD format
- `items` (required): Array of `{itemId, quantity}` (quantity min 1)

**Success (201 Created):** Cart with id, status, eventDate, and items.

### 8.4 Update cart

| Method | URL                |
|--------|--------------------|
| PUT    | `/api/carts/{id}`  |

**Request body:** Same as create (all fields optional): `status`, `paymentMethod`, `eventDate`, `items`. If `items` is provided, it replaces all existing items in the cart.

**Success (200 OK):** Updated cart.

### 8.5 Delete cart

| Method | URL                |
|--------|--------------------|
| DELETE | `/api/carts/{id}`  |

**Success (200 OK):** `message`: "Cart deleted", `data`: null.

### 8.6 Get cart total

Returns the total amount for the cart (sum of item price × quantity from item details). Use before checkout to display total.

| Method | URL                     |
|--------|-------------------------|
| GET    | `/api/carts/{id}/total` |

**Success (200 OK):** `data` is a number (decimal), e.g. `"data": 99.50`. **Error (400):** Cart not found or not owned by user.

### 8.7 Process payment (pay)

Process payment for the cart using the chosen payment method (online or offline). Creates a **transaction** record and, on success, sets cart status to PAID and stores the payment method on the cart.

| Method | URL                 |
|--------|---------------------|
| POST   | `/api/carts/{id}/pay` |

**Request body:**

| Field              | Type   | Required | Description                                      |
|--------------------|--------|----------|--------------------------------------------------|
| paymentMethod     | string | yes      | `ONLINE` or `OFFLINE`                            |
| externalReference | string | no       | e.g. payment gateway id, cheque number            |

**Example:**

```json
{
  "paymentMethod": "ONLINE",
  "externalReference": "gateway-txn-12345"
}
```

**Success (201 Created):** `data` is the created transaction:

```json
{
  "success": true,
  "message": "Payment processed",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "userId": 1,
    "cartId": "c1d2e3f4-a5b6-7890-cdef-123456789012",
    "paymentMethod": "ONLINE",
    "amount": 99.50,
    "status": "SUCCESS",
    "externalReference": "gateway-txn-12345",
    "createdAt": "2025-02-20T12:00:00Z"
  }
}
```

**Errors:** 400 if cart not found or not owned by user; 400 if cart already PAID/COMPLETED or total is zero; 400 if payment method unsupported.

### 8.8 List transactions for a cart

List all payment transactions for a specific cart (payment history for that cart).

| Method | URL                          |
|--------|------------------------------|
| GET    | `/api/carts/{id}/transactions` |

**Success (200 OK):** `data` is an array of transactions (same shape as in 8.7). **Error (400):** Cart not found or not owned by user.

---

## 10. Transactions API

Base path: `/api/transactions`. Lists payment transactions for the **current user** (all carts). Uses `user_id` for fast retrieval.

**Authentication:** `Authorization: Bearer <token>`.

### 9.1 List my transactions

| Method | URL                 |
|--------|---------------------|
| GET    | `/api/transactions` |

**Success (200 OK):** Array of transactions, newest first. Each has `id` (UUID string), `userId`, `cartId`, `paymentMethod`, `amount`, `status` (PENDING, SUCCESS, FAILED, REFUNDED), `externalReference`, `createdAt`.

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "userId": 1,
      "cartId": "c1d2e3f4-a5b6-7890-cdef-123456789012",
      "paymentMethod": "ONLINE",
      "amount": 99.50,
      "status": "SUCCESS",
      "externalReference": "gateway-txn-12345",
      "createdAt": "2025-02-20T12:00:00Z"
    }
  ]
}
```

---

## 11. Ratings API

Base path: `/api/ratings`. Users can rate and comment on items. Rating is 1-5 stars with optional description/comment.

- **GET `/api/ratings/item/{itemId}`** is **public** (no authentication). Other endpoints require JWT.

### 10.1 Get ratings for an item (public)

| Method | URL                            |
|--------|--------------------------------|
| GET    | `/api/ratings/item/{itemId}`   |

Returns all ratings for a specific item. No authentication required.

**Success (200 OK) – response example:**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "itemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "rating": 5,
      "description": "Excellent quality! Highly recommend.",
      "createdAt": "2025-02-16T14:00:00Z",
      "updatedAt": null
    },
    {
      "id": 2,
      "userId": 3,
      "itemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "rating": 4,
      "description": "Good product, fast delivery.",
      "createdAt": "2025-02-16T13:30:00Z",
      "updatedAt": null
    }
  ]
}
```

### 10.2 Get my ratings

| Method | URL           |
|--------|---------------|
| GET    | `/api/ratings` |

**Auth:** JWT required. Returns all ratings created by the current user.

**Success (200 OK):** Array of rating responses.

### 10.3 Get rating by ID

| Method | URL                |
|--------|--------------------|
| GET    | `/api/ratings/{id}` |

**Auth:** JWT required. **Success (200 OK):** Single rating. **Error (400):** Rating not found or not owned by user.

### 10.4 Create rating

| Method | URL           |
|--------|---------------|
| POST   | `/api/ratings` |

**Auth:** JWT required.

**Request body example:**

```json
{
  "itemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "rating": 5,
  "description": "Amazing product! Exceeded my expectations."
}
```

**Fields:**
- `itemId` (required): The item being rated
- `rating` (required): Integer 1-5
- `description` (optional): Comment/description, max 1000 characters

**Success (201 Created):** Rating with id, userId, itemId, rating, description, createdAt.

### 10.5 Update rating

| Method | URL                |
|--------|--------------------|
| PUT    | `/api/ratings/{id}` |

**Auth:** JWT required.

**Request body:** `rating` (optional, 1-5), `description` (optional, max 1000 chars).

**Success (200 OK):** Updated rating.

### 10.6 Delete rating

| Method | URL                |
|--------|--------------------|
| DELETE | `/api/ratings/{id}` |

**Auth:** JWT required.

**Success (200 OK):** `message`: "Rating deleted", `data`: null.

---

## 12. Short link redirect (public)

Resolve a short code and redirect to the full URL. No authentication. Increments the click count.

| Method | URL        |
|--------|------------|
| GET    | `/s/{code}`|

**Success (302 Found):** Redirect to the stored full URL.  
**Error (400):** Short link not found, disabled, or expired. Response body contains an error message.

---

## 13. Admin API

Requires a user with role **ADMIN** and a valid JWT.

### 11.1 Dashboard (admin)

| Method | URL                     |
|--------|-------------------------|
| GET    | `/api/admin/dashboard`  |

**Headers:** `Authorization: Bearer <token>` (admin user).

**Success (200 OK)**

```json
{
  "success": true,
  "message": "Welcome, Admin",
  "data": {
    "message": "Admin only area"
  }
}
```

**Error (403):** User is not an admin.

---

### 11.2 List all QR codes (admin)

Returns all short links (QR codes) from all users. Admin only.

| Method | URL                     |
|--------|-------------------------|
| GET    | `/api/admin/qr-codes`   |

**Headers:** `Authorization: Bearer <token>` (admin user).

**Success (200 OK)**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "shortCode": "abc12345",
      "fullUrl": "https://example.com/page",
      "userId": 1,
      "clickCount": 5,
      "createdAt": "2025-01-01T12:00:00Z",
      "expiresAt": null,
      "active": true
    }
  ]
}
```

Each item includes `userId` so the admin can see which user owns the link. **Error (403):** User is not an admin.

---

### 11.3 List all image uploads (admin)

Returns all Base64-uploaded images from all users. Admin only.

| Method | URL                             |
|--------|----------------------------------|
| GET    | `/api/admin/image-uploads`       |

**Headers:** `Authorization: Bearer <token>` (admin user).

**Success (200 OK):** `data` is an array of image upload responses (id, shortCode, imageUrl, contentType, userId, createdAt, etc.). **Error (403):** User is not an admin.

---

### 11.4 List all categories (admin)

Returns **all** categories from all users (active and inactive). Admin only.

| Method | URL                          |
|--------|------------------------------|
| GET    | `/api/admin/categories`      |

**Headers:** `Authorization: Bearer <token>` (admin user).

**Success (200 OK):** `data` is an array of category responses (id, description, imageUrl, active, userId, createdAt, updatedAt). **Error (403):** User is not an admin.

---

### 11.5 List all items (admin)

Returns **all** items from all users (active and inactive). Admin only.

| Method | URL                     |
|--------|-------------------------|
| GET    | `/api/admin/items`      |

**Headers:** `Authorization: Bearer <token>` (admin user).

**Success (200 OK):** `data` is an array of item responses (id, description, imageUrl, active, userId, categoryId, detail, address, contact, createdAt, updatedAt). **Error (403):** User is not an admin.

---

## Summary table

| Area        | Endpoint                    | Method | Auth    |
|------------|-----------------------------|--------|---------|
| Auth       | /api/auth/signup            | POST   | No      |
| Auth       | /api/auth/login             | POST   | No      |
| Auth       | /api/auth/verify-email      | GET/POST | No    |
| Auth       | /api/auth/validate-token    | GET    | No (send Bearer token to validate) |
| Users      | /api/users/online            | GET    | JWT (list signed-in users) |
| Messages   | /api/messages               | POST   | JWT (send message) |
| Messages   | /api/messages/conversation/{userId} | GET | JWT |
| Messages   | /api/messages/conversations | GET    | JWT |
| Messages   | /api/messages/{id}/read     | PATCH  | JWT |
| WebSocket  | /ws?token=JWT               | GET (WS) | JWT (receiver subscribes to /user/queue/messages) |
| Wall       | /api/wall/posts             | GET, POST | JWT (all users can read and publish) |
| Health     | /api/health                 | GET    | No      |
| Invoices   | /api/invoices               | GET, POST | JWT   |
| Invoices   | /api/invoices/{id}          | GET, PUT, DELETE | JWT |
| QR codes (user) | /api/qr-codes                | GET    | JWT   |
| Shorteners | /api/shorteners             | GET, POST | JWT   |
| Shorteners | /api/shorteners/{id}        | GET, PUT, DELETE | JWT |
| Image uploads | /api/image-uploads       | GET, POST | JWT   |
| Image uploads | /api/image-uploads/{id}   | GET    | JWT   |
| Categories | /api/categories            | GET | No (public) |
| Categories | /api/categories            | POST | JWT   |
| Categories | /api/categories/{id}       | GET, PUT, DELETE | JWT |
| Categories | /api/categories/images/{categoryId} | GET | No (public) |
| Items      | /api/items                 | GET | No (public) |
| Items      | /api/items/category/{categoryId} | GET | No (public) |
| Items      | /api/items/buyers          | GET | JWT (item owner: users who added his items to their carts) |
| Items      | /api/items                 | POST | JWT   |
| Items      | /api/items/{id}            | GET, PUT, DELETE | JWT |
| Items      | /api/items/images/{userId}/{itemId} or /{itemId} | GET | No (public) |
| Carts      | /api/carts                 | GET, POST | JWT   |
| Carts      | /api/carts/{id}            | GET, PUT, DELETE | JWT |
| Carts      | /api/carts/{id}/total      | GET | JWT   |
| Carts      | /api/carts/{id}/pay        | POST | JWT   |
| Carts      | /api/carts/{id}/transactions | GET | JWT   |
| Transactions | /api/transactions         | GET | JWT   |
| Ratings    | /api/ratings/item/{itemId} | GET | No (public) |
| Ratings    | /api/ratings               | GET, POST | JWT   |
| Ratings    | /api/ratings/{id}          | GET, PUT, DELETE | JWT |
| Serve image   | /i/{code}                  | GET    | No      |
| Redirect   | /s/{code}                   | GET    | No      |
| Admin      | /api/admin/dashboard        | GET    | JWT (ADMIN) |
| Admin      | /api/admin/qr-codes         | GET    | JWT (ADMIN) |
| Admin      | /api/admin/image-uploads   | GET    | JWT (ADMIN) |
| Admin      | /api/admin/categories      | GET    | JWT (ADMIN) |
| Admin      | /api/admin/items            | GET    | JWT (ADMIN) |
