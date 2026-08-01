# NexuPay API Flow

This document describes how each API request flows through the NexuPay system.

---

# Table of Contents

- [x] Merchant Registration
- [x] Merchant Authentication
- [x] Payment Creation
- [x] Payment Attempt
- [ ] Payment Status
- [ ] Refund
- [ ] Webhook

---

# 1. Merchant Registration

## Endpoint

POST /api/v1/merchants

---

## Request Flow

```
Client (Postman / Frontend)
        │
        ▼
HTTP Request
        │
        ▼
@Valid Validation
        │
        ▼
GlobalExceptionHandler
        │
   ┌────┴─────┐
   │          │
Invalid     Valid
   │          │
   ▼          ▼
400       MerchantController
               │
               ▼
        MerchantService
        (@Transactional)
               │
               ├── Check duplicate email
               ├── Generate Merchant ID
               ├── Save Merchant
               ├── Generate API Key
               ├── Generate Secret Key
               ├── Hash Secret Key
               ├── Save ApiCredential
               └── Return Response
               │
               ▼
          PostgreSQL
```

---

## Success Response

**HTTP Status**

```
201 Created
```

**Response**

```json
{
  "merchantId": "MER_01K...",
  "apiKey": "pk_test_01K...",
  "secretKey": "sk_test_01K..."
}
```

---

## Validation Errors

Validation is performed before the request reaches the service layer.

Example:

```json
{
  "businessName": "",
  "email": "abc",
  "phone": "123"
}
```

Response:

```json
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "errors": [
    {
      "field": "businessName",
      "message": "Business name is required"
    }
  ]
}
```

---

## Database Tables Used

- merchant
- api_credential

---

## Status

✅ Completed

---

# 2. API Authentication

## Purpose

Authenticate every protected API request before it reaches the controller.

Public endpoints (e.g. `POST /api/v1/merchants`) bypass authentication.

---

## Protected Request Flow

```text
Merchant
    │
    │  X-API-Key
    │  X-API-Secret
    ▼
HTTP Request
    │
    ▼
Tomcat
    │
    ▼
ApiAuthenticationFilter
    │
    ▼
shouldNotFilter()
    │
┌───┴───────────────┐
│                   │
Yes                 No
│                   │
▼                   ▼
Skip Filter     Read Headers
│                   │
│                   ▼
│           Headers Present?
│                   │
│          ┌────────┴────────┐
│          │                 │
│         No                Yes
│          │                 │
│        401                 ▼
│                  Find ApiCredential
│                  using API Key
│                          │
│                ┌─────────┴─────────┐
│                │                   │
│            Not Found            Found
│                │                   │
│              401                  ▼
│                    Verify Secret Key
│                     (BCrypt.matches)
│                          │
│                 ┌────────┴────────┐
│                 │                 │
│              Invalid            Valid
│                 │                 │
│               401                ▼
│                      Credential ACTIVE?
│                          │
│                 ┌────────┴────────┐
│                 │                 │
│            INACTIVE            ACTIVE
│                 │                 │
│               401                ▼
└──────────────────────────────────►
                     filterChain.doFilter()
                              │
                              ▼
                     DispatcherServlet
                              │
                              ▼
                         Controller
                              │
                              ▼
                            Service
                              │
                              ▼
                          PostgreSQL
```

---

## Authentication Result

### Success

- Authentication succeeds.
- Request continues to the Controller.
- Business logic is executed.

### Failure

Returns:

```
401 Unauthorized
```

The request never reaches:

- Controller
- Service
- Repository

---

## Authentication Order

```
1. Read X-API-Key
2. Read X-API-Secret
3. Validate headers
4. Find ApiCredential by API Key
5. Verify Secret using BCrypt
6. Verify Credential Status (ACTIVE)
7. Continue request using filterChain.doFilter()
```

---

## Public Endpoints

Authentication is skipped for:

```
POST /api/v1/merchants
```

using:

```java
shouldNotFilter()
```

---

## Status

✅ Completed

# 3. Payment Creation

## Purpose

Create a new payment on behalf of a merchant.

## Endpoint

POST /api/v1/payments

## Request Flow

```text
Merchant
    │
    ▼
Authentication Filter
    │
    ▼
Controller
    │
    ▼
PaymentService
    │
    ├── Validate Request
    ├── Check Idempotency
    ├── Create Payment
    ├── Save Payment
    └── Return Payment URL
    │
    ▼
PostgreSQL
```

## Database Tables Used

- payment

## Transaction Boundary

Single transaction.

## Status

✅ Completed

---

# 4. Payment Attempt

## Purpose

Execute a payment by communicating with the bank.

## Endpoint

POST /payments/{paymentId}/attempts

## Request Flow

```text
Customer
    │
    ▼
PaymentAttemptController
    │
    ▼
PaymentAttemptService
    │
    ├─────────────────────────────┐
    │ Transaction 1               │
    │-----------------------------│
    │ Lock Payment                │
    │ Validate Payment            │
    │ Create PaymentAttempt       │
    │ Commit                      │
    └─────────────────────────────┘
                │
                ▼
           Call Bank
                │
                ▼
    ┌─────────────────────────────┐
    │ Transaction 2               │
    │-----------------------------│
    │ Reload Payment (FOR UPDATE) │
    │ Finalize PaymentAttempt     │
    │ Update Payment              │
    │ Commit                      │
    └─────────────────────────────┘
                │
                ▼
            Response
```

## Database Tables Used

- payment
- payment_attempt

## Transaction Boundary

Two independent transactions.

## Status

✅ Completed

---

# 5. Payment Recovery

## Purpose

Automatically recover unfinished payment attempts.

## Status

🚧 In Progress

Implementation planned:

- Scheduler
- Batch Processing
- Bank Status Check
- Finalize Existing PaymentAttempt

---

# 6. Payment Status

Status: ⏳ Planned

---

# 7. Refund

Status: ⏳ Planned

---

# 8. Webhook

Status: ⏳ Planned
