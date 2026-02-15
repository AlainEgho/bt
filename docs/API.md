# Backend API Documentation

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

## 2. Health

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

## 3. Invoices API

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

## 4. Shorteners API

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

**Example**

```json
{
  "fullUrl": "https://example.com/long-page",
  "shortCode": "mylink",
  "expiresAt": null
}
```

**Success (201 Created):** `data` is the created short link.  
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

## 5. Short link redirect (public)

Resolve a short code and redirect to the full URL. No authentication. Increments the click count.

| Method | URL        |
|--------|------------|
| GET    | `/s/{code}`|

**Success (302 Found):** Redirect to the stored full URL.  
**Error (400):** Short link not found, disabled, or expired. Response body contains an error message.

---

## 6. Admin API

Requires a user with role **ADMIN** and a valid JWT.

### 6.1 Dashboard

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

### 6.2 List all QR codes (admin)

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

## Summary table

| Area        | Endpoint                    | Method | Auth    |
|------------|-----------------------------|--------|---------|
| Auth       | /api/auth/signup            | POST   | No      |
| Auth       | /api/auth/login             | POST   | No      |
| Auth       | /api/auth/verify-email      | GET/POST | No    |
| Auth       | /api/auth/validate-token    | GET    | No (send Bearer token to validate) |
| Health     | /api/health                 | GET    | No      |
| Invoices   | /api/invoices               | GET, POST | JWT   |
| Invoices   | /api/invoices/{id}          | GET, PUT, DELETE | JWT |
| QR codes (user) | /api/qr-codes                | GET    | JWT   |
| Shorteners | /api/shorteners             | GET, POST | JWT   |
| Shorteners | /api/shorteners/{id}        | GET, PUT, DELETE | JWT |
| Redirect   | /s/{code}                   | GET    | No      |
| Admin      | /api/admin/dashboard        | GET    | JWT (ADMIN) |
| Admin      | /api/admin/qr-codes         | GET    | JWT (ADMIN) |
