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

```text
Payment
   │
   └── PaymentAttempt
            │
            ├── Existing attempt
            │
            └── Recover existing attempt

```
This is important because the original bank operation and its database record remain associated with the same payment attempt.

Database Tables Used
```text
payment
payment_attempt
Transaction Boundary
```

Payment recovery uses the existing payment-processing transaction boundaries.

The recovery operation finalizes the existing PaymentAttempt and updates the corresponding Payment in a database transaction.

The external bank status check is kept separate from the database finalization transaction.

Example

Suppose:

```text
Payment ID = PAY_123
PaymentAttempt ID = ATT_456

```
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

## Status

✅ Completed

---

# 6. Payment Status

## Purpose

Provides the current status of a payment.

The Payment Status API allows the merchant to check whether a payment is currently pending, successful, or failed.

---

## Endpoint
GET /api/v1/payments/{paymentId}

## Request Flow
```text
Merchant
    │
    ▼
GET /api/v1/payments/{paymentId}
    │
    ▼
Authentication Filter
    │
    ├── Invalid credentials ──► 401 Unauthorized
    │
    ▼
PaymentController
    │
    ▼
PaymentService
    │
    ▼
Find Payment by Payment ID
    │
    ├── Payment not found ──► 404 Not Found
    │
    ▼
Build Payment Response
    │
    ▼
Return Payment Status
```
## Authentication

The Payment Status API is a protected merchant API.

The merchant must provide the required authentication credentials:
```text

X-API-Key
X-API-Secret

```
The authentication filter validates the credentials before allowing the request to reach the controller.

## Payment Lookup

The payment is retrieved using its unique payment ID.

```text
Example:

GET /api/v1/payments/PAY_01M19W9V5YEMKYGPD92F06FHHD
```

The service loads the payment from the database and returns its current persisted status.

## Payment Status

The payment status represents the current state of the payment.

Typical lifecycle:

```text
PENDING
│
├───────────────┐
▼               ▼
SUCCESS          FAILED
```

A payment can remain PENDING while the payment attempt is still being processed or while the system is recovering an uncertain bank response.
```text
Example: Pending Payment
{
"paymentId": "PAY_01M19W9V5YEMKYGPD92F06FHHD",
"status": "PENDING"
}
```
This means the payment does not yet have a final result.

```text
Example: Successful Payment
{
"paymentId": "PAY_01M19W9V5YEMKYGPD92F06FHHD",
"status": "SUCCESS"
}
```

This means the payment has been successfully completed.

```text
Example: Failed Payment
{
"paymentId": "PAY_01M19W9V5YEMKYGPD92F06FHHD",
"status": "FAILED"
}
```

This means the payment attempt ultimately failed.

## Payment Status and Recovery

Payment Status represents the currently persisted payment state.

For example, if a bank response is lost:

```text
Customer
│
▼
Payment Attempt
│
▼
Bank
│
▼
Response Lost
│
▼
Payment remains PENDING
```
---
The payment recovery process can later determine the actual bank result.
```text
If recovery finds:

Bank → SUCCESS

then:

Payment
PENDING
│
▼
SUCCESS
```
---
```text
If recovery finds:

Bank → FAILED

then:

Payment
PENDING
│
▼
FAILED
```
---
Therefore, the Payment Status API always exposes the latest status stored by NexuPay.

## Payment Status vs Payment Attempt

A payment can have one or more payment attempts during its lifecycle.

Conceptually:

```text
Payment
│
├── PaymentAttempt 1
│
├── PaymentAttempt 2
│
└── Current Payment Status
```

The Payment Status API returns the status of the payment itself, not merely the transient state of an individual payment attempt.

## Payment Not Found

If the supplied payment ID does not exist:

```text
GET /api/v1/payments/{paymentId}
│
▼
Payment not found
│
▼
404 Not Found
```
## Authentication Failure

If the merchant credentials are invalid:

```text
Invalid X-API-Key / X-API-Secret
│
▼
401 Unauthorized
```

The request is rejected by the authentication layer.

## Database Table

The Payment Status API reads from:

payment

No new payment or payment attempt is created by this API.

Important Principle

The Payment Status API is a read operation.

It does not:

create a payment
create a payment attempt
submit a payment to the bank
retry a payment
modify the payment status

It simply returns the current persisted payment state.

## Status

✅ Completed

--- 


# 7. Refund

## Purpose

Allows a merchant to create a refund for a previously completed payment.

The refund flow validates the payment, protects against concurrent refund requests, calculates the remaining refundable amount, and creates a refund request with `PENDING` status.

The actual submission of the refund to the bank is handled asynchronously by the refund processing scheduler.

---

## Endpoint

```text
POST /api/v1/refunds
Request Flow
Merchant
    │
    ▼
POST /api/v1/refunds
    │
    ▼
Authentication Filter
    │
    ├── Invalid credentials ──► 401 Unauthorized
    │
    ▼
RefundController
    │
    ▼
RefundService
    │
    ▼
Validate Payment
    │
    ▼
Lock Payment
    │
    ▼
Check Existing Refunds
    │
    ▼
Calculate Already Refunded Amount
    │
    ▼
Calculate Remaining Refundable Amount
    │
    ▼
Validate Requested Refund Amount
    │
    ▼
Create Refund
    │
    ▼
Save Refund
    │
    ▼
Return Refund Response
Authentication
```
--- 
The Refund API is a protected merchant API.

The merchant must provide:

```text
X-API-Key
X-API-Secret
```

The authentication filter validates the credentials before the request reaches the refund controller.

## Payment Validation

Before creating a refund, the service validates the associated payment.

The refund must be associated with an eligible payment.

Conceptually:

```text
Refund Request
      │
      ▼
Payment
      │
      ├── Invalid / not eligible
      │          │
      │          ▼
      │        Reject
      │
      ▼
Continue Refund Creation
Payment Locking
```

The payment is locked while the refund amount is calculated and the refund is created.

This is important for concurrent refund requests.

Example:

Payment Amount = ₹1000

Two requests arrive at approximately the same time:

```
Instance 1 ──► Refund ₹700
Instance 2 ──► Refund ₹500
```

Without proper concurrency control, both requests could calculate the refundable amount using the same initial state.

The payment locking mechanism ensures that the calculation and creation are performed safely.

## Refundable Amount Calculation

The service calculates how much of the payment has already been refunded.

Conceptually:

```
Remaining Refundable Amount
    =
Payment Amount
    -
Already Refunded Amount```

Example:

```
Payment Amount          = ₹1000
Already Refunded        = ₹300
Remaining Refundable   = ₹700
```
Therefore:

Requested Refund ≤ Remaining Refundable Amount

must be satisfied.

## Refund Amount Validation

If the requested refund amount is greater than the remaining refundable amount, the refund request is rejected.

Example:

```text
Payment Amount        = ₹1000
Already Refunded      = ₹700
Remaining Refundable  = ₹300

Requested Refund      = ₹500

Result:

₹500 > ₹300
     │
     ▼
Refund rejected
Refund Creation
```

After all validations succeed, a new refund record is created.

The newly created refund starts in:

PENDING

status.

```text
Example:

Refund
├── refundId = REF_...
├── paymentId = PAY_...
├── amount = ₹300
├── status = PENDING
└── processing_started_at = NULL

```
At this point the refund has been created, but it has not yet been submitted to the bank.

## Asynchronous Refund Processing

```text
Refund creation and bank submission are intentionally separated.

Create Refund
      │
      ▼
Refund = PENDING
      │
      ▼
Return response to Merchant
      │
      │
      │ asynchronous
      ▼
Refund Scheduler
      │
      ▼
Submit Refund to Bank
```

This prevents the refund creation API from having to wait for the complete bank-processing lifecycle.

Refund Processing States

The basic lifecycle is:

```text
PENDING
   │
   ▼
Refund Scheduler
   │
   ▼
Bank Processing
   │
   ├───────────────┐
   ▼               ▼
SUCCESS          FAILED
```
A refund can also temporarily remain PENDING when the bank response is uncertain.

Database Tables

The refund creation flow uses:

payment
refund

The payment is used for validation and concurrency protection, while the refund table stores the refund request and its processing state.

## Transaction Boundary

```
Refund creation runs inside a database transaction.

The payment lock, refundable amount calculation, validation, and refund creation are performed within the appropriate transaction boundary.

This ensures that concurrent refund requests cannot independently calculate and consume the same refundable balance.

Duplicate / Concurrent Refund Protection

The implementation also protects against concurrent duplicate refund creation using the database uniqueness constraint and corresponding application handling.

This provides an additional database-level safeguard against duplicate refund records.
```
Example

Suppose:

```
Payment ID      = PAY_123
Payment Amount  = ₹1000
Already Refunded = ₹200
```

A merchant requests:

Refund Amount = ₹500

The service calculates:

Remaining Refundable
= ₹1000 - ₹200
= ₹800

Since:

₹500 ≤ ₹800

the refund is created:
```
Refund ID             = REF_123
Amount                = ₹500
Status                = PENDING
processing_started_at = NULL
```

The API returns the newly created refund information.

Later, the refund scheduler picks up REF_123 and submits it to the bank.

Important Principle

Creating a refund does not mean that the money has already been refunded.

The creation API only creates the refund request:

Refund Created
      │
      ▼
PENDING

The asynchronous refund processor is responsible for communicating with the bank and eventually changing the refund to:

SUCCESS

or:

FAILED
## Status

✅ Completed