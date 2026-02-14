# Auth API – Frontend integration

Base URL: `http://localhost:8080` (or your deployed backend URL).

All JSON request/response use **Content-Type: application/json**.

---

## 1. Sign up

Register a new user. Returns a JWT and user info. The user receives an email with a verification link (if email is configured).

**Request**

- **Method:** `POST`
- **URL:** `/api/auth/signup`
- **Headers:** `Content-Type: application/json`
- **Body (JSON):**

| Field       | Type   | Required | Constraints        | Description        |
|------------|--------|----------|--------------------|--------------------|
| firstName  | string | yes      | max 100 chars      | User's first name  |
| lastName   | string | yes      | max 100 chars      | User's last name   |
| email      | string | yes      | valid email, max 255 | Login identifier   |
| password   | string | yes      | min 6, max 100     | Password           |
| address    | string | no       | max 500 chars      | Postal/street      |
| phoneNumber| string | no       | max 30 chars       | Phone number       |

**Example request body**

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "password": "securePass123",
  "address": "456 Oak Ave",
  "phoneNumber": "+1 555 123 4567"
}
```

**Success response**

- **Status:** `201 Created`
- **Body:**

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
    "emailVerified": false,
    "roles": ["USER"]
  }
}
```

**Error responses**

- **400** – Validation error or email already registered.  
  Body: `{ "success": false, "message": "<error detail>", "data": null }`  
  Example: `"Email already registered"` or `"First name is required; Password is required"`.

**Example (fetch)**

```javascript
const response = await fetch('http://localhost:8080/api/auth/signup', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    firstName: 'Jane',
    lastName: 'Doe',
    email: 'jane@example.com',
    password: 'securePass123',
    address: '456 Oak Ave',
    phoneNumber: '+1 555 123 4567'
  })
});
const json = await response.json();
// Store json.data.accessToken for authenticated requests
```

---

## 2. Login

Authenticate and get a JWT.

**Request**

- **Method:** `POST`
- **URL:** `/api/auth/login`
- **Headers:** `Content-Type: application/json`
- **Body (JSON):**

| Field    | Type   | Required | Description |
|----------|--------|----------|-------------|
| email    | string | yes      | User email  |
| password | string | yes      | Password    |

**Example request body**

```json
{
  "email": "jane@example.com",
  "password": "securePass123"
}
```

**Success response**

- **Status:** `200 OK`
- **Body:** Same shape as signup `data` (includes `accessToken`, user fields, `roles`).

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
    "emailVerified": true,
    "roles": ["USER"]
  }
}
```

**Error responses**

- **400** – Validation (e.g. missing email/password).  
  Body: `{ "success": false, "message": "<detail>", "data": null }`
- **401** – Invalid email or password.  
  Body: `{ "success": false, "message": "Invalid email or password", "data": null }`

**Example (fetch)**

```javascript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'jane@example.com', password: 'securePass123' })
});
const json = await response.json();
// Store json.data.accessToken
```

---

## 3. Verify email

Confirm email using the token from the verification link (sent after signup). Callable with GET (e.g. redirect from link) or POST.

**Request**

- **Method:** `GET` or `POST`
- **URL:** `/api/auth/verify-email?token=<verification_token>`
- **Query parameter:** `token` (string) – token from the email link.

**Success response**

- **Status:** `200 OK`
- **Body:** `{ "success": true, "message": "Email verified successfully", "data": null }`

**Error responses**

- **400** – Invalid, expired, or already-used token.  
  Body: `{ "success": false, "message": "<detail>", "data": null }`  
  Examples: `"Invalid or expired verification token"`, `"Verification token already used"`, `"Verification token has expired"`.

**Example (fetch)**

```javascript
const token = new URLSearchParams(window.location.search).get('token');
const response = await fetch(`http://localhost:8080/api/auth/verify-email?token=${encodeURIComponent(token)}`, {
  method: 'GET'
});
const json = await response.json();
```

---

## 4. Using the JWT (protected endpoints)

After signup or login, use the returned `accessToken` for any protected API (e.g. `/api/admin/**` or other authenticated routes).

**Header**

- **Name:** `Authorization`
- **Value:** `Bearer <accessToken>`

**Example (fetch)**

```javascript
const response = await fetch('http://localhost:8080/api/admin/dashboard', {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});
```

Without a valid token or with an expired/invalid token, protected endpoints return **401 Unauthorized**.

---

## 5. Generic response shape

All auth endpoints wrap the payload in this structure when applicable:

```ts
{
  success: boolean;   // true on success, false on error
  message: string;    // human-readable message
  data: T | null;     // response payload (e.g. AuthResponse) or null on error
}
```

Validation and business errors use `success: false` and put the reason in `message`.
