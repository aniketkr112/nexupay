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

```java shouldNotFilter
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

Automatically recover payment attempts that were not completed because the application could not determine the final result of the bank operation.

Payment Recovery handles cases where a payment attempt exists in the database but the final payment status was not successfully recorded.

---

## Why Payment Recovery Is Needed

Consider this scenario:

```text
Customer
   │
   ▼
Payment Attempt
   │
   ▼
NexuPay → Bank
   │
   ▼
Bank processes payment
   │
   ▼
Response is lost
   │
   ▼
NexuPay cannot determine final result

```
The payment attempt may remain unfinished.

Instead of creating a new payment attempt, the recovery process checks the existing payment attempt with the bank and determines its actual status.

---
## Recovery Flow

```text
Scheduler
    │
    ▼
Find unfinished payment attempts
    │
    ▼
Process payment attempt
    │
    ▼
Check bank status
    │
    ├───────────────┬────────────────┐
    │               │                │
    ▼               ▼                ▼
 SUCCESS          FAILED          UNKNOWN
    │               │                │
    ▼               ▼                ▼
Update            Update          Keep attempt
Payment           Payment         eligible for
Status            Status          recovery
    │               │
    └───────┬───────┘
            ▼
     Finalize PaymentAttempt
```
---
## Scheduler

The recovery process runs automatically using a scheduled job.

The scheduler:

Finds unfinished payment attempts.
Processes them in batches.
Checks the status of each payment with the bank.
Finalizes the existing PaymentAttempt.
Updates the related Payment.
Important Principle

Payment Recovery does not blindly create a new payment attempt.

The existing payment attempt is recovered by checking the bank/payment provider for its actual status.

This prevents creating unnecessary duplicate payment attempts when the original bank request may already have been processed.

---
# Recovery Scenarios
## 1. Bank Status = SUCCESS
```text
Unfinished PaymentAttempt
        │
        ▼
Bank Status Lookup
        │
        ▼
SUCCESS
        │
        ▼
Finalize PaymentAttempt
        │
        ▼
Payment = SUCCESS
```
The existing payment attempt is finalized as successful and the payment is updated accordingly.

## 2. Bank Status = FAILED
```text
Unfinished PaymentAttempt
        │
        ▼
Bank Status Lookup
        │
        ▼
FAILED
        │
        ▼
Finalize PaymentAttempt
        │
        ▼
Payment = FAILED
```

The existing payment attempt is finalized as failed and the payment is updated accordingly.

## 3. Bank Status Is Still Unknown
```text
Unfinished PaymentAttempt
        │
        ▼
Bank Status Lookup
        │
        ▼
Still Unresolved
        │
        ▼
Keep PaymentAttempt
eligible for recovery
```


The system does not incorrectly mark the payment as SUCCESS or FAILED when the bank still cannot provide a final result.

The attempt remains available for a future recovery cycle.

Existing PaymentAttempt

The recovery process works with the payment attempt that was already created during the original payment flow.

Payment
   │
   └── PaymentAttempt
            │
            ├── Existing attempt
            │
            └── Recover existing attempt

This is important because the original bank operation and its database record remain associated with the same payment attempt.

Database Tables Used
payment
payment_attempt
Transaction Boundary

Payment recovery uses the existing payment-processing transaction boundaries.

The recovery operation finalizes the existing PaymentAttempt and updates the corresponding Payment in a database transaction.

The external bank status check is kept separate from the database finalization transaction.

Example

Suppose:

Payment ID = PAY_123
PaymentAttempt ID = ATT_456

The original payment attempt reaches an uncertain state:

Payment
    status = PENDING

PaymentAttempt
    status = PROCESSING

The recovery scheduler later checks the bank:

Bank → SUCCESS

NexuPay then updates:

Payment
    status = SUCCESS

PaymentAttempt
    status = SUCCESS

The original payment attempt is finalized instead of creating another payment attempt.

Status

✅ Completed