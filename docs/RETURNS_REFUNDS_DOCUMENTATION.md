# Returns and Refunds Management System Documentation

## Overview

The Returns and Refunds Management System provides a comprehensive self-service portal for customers to initiate returns, track return status, and receive automated refunds. The system includes automated approval processing, RMA tracking, exchange processing, and analytics.

## API Documentation

### Customer-Facing Endpoints

#### Create Return Request
- **Endpoint:** `POST /api/returns`
- **Description:** Creates a new return request with RMA number generation
- **Request Body:**
```json
{
  "orderId": "string",
  "userId": "string",
  "items": [
    {
      "orderItemId": 123,
      "quantity": 1,
      "returnReason": "DEFECTIVE",
      "condition": "string (optional)",
      "comments": "string (optional)"
    }
  ],
  "returnType": "REFUND_TO_PAYMENT | STORE_CREDIT | EXCHANGE",
  "comments": "string (optional)"
}
```
- **Response:** `ReturnResponse` with RMA number and return details

#### Get Return by RMA Number
- **Endpoint:** `GET /api/returns/rma/{rmaNumber}`
- **Description:** Retrieves return details using RMA number (accessible without login)
- **Response:** `ReturnResponse`

#### Get User Returns
- **Endpoint:** `GET /api/returns/user/{userId}`
- **Description:** Retrieves all returns for a user
- **Response:** `List<ReturnResponse>`

#### Get Return Tracking
- **Endpoint:** `GET /api/returns/{returnId}/tracking`
- **Description:** Retrieves comprehensive tracking information for a return
- **Response:** `ReturnTrackingResponse`

#### Get Return Policy
- **Endpoint:** `GET /api/returns/policy`
- **Description:** Retrieves the current return policy configuration
- **Response:** `ReturnPolicyResponse`

#### Create Exchange Request
- **Endpoint:** `POST /api/returns/{returnId}/exchange`
- **Description:** Creates an exchange order for a return
- **Request Body:**
```json
{
  "exchangeProductId": "string",
  "quantity": 1,
  "notes": "string (optional)"
}
```
- **Response:** `OrderResponse` (new exchange order)

### Admin Endpoints

#### List Returns (with filters)
- **Endpoint:** `GET /api/admin/returns`
- **Query Parameters:**
  - `status` (optional): Filter by return status
  - `userId` (optional): Filter by user ID
  - `orderId` (optional): Filter by order ID
  - `rmaNumber` (optional): Search by RMA number
- **Response:** `List<ReturnResponse>`

#### Get Return Details
- **Endpoint:** `GET /api/admin/returns/{returnId}`
- **Response:** `ReturnResponse`

#### Approve Return
- **Endpoint:** `POST /api/admin/returns/{returnId}/approve`
- **Query Parameters:**
  - `approvedBy` (optional, default: "ADMIN")
- **Request Body (optional):**
```json
{
  "notes": "string"
}
```

#### Reject Return
- **Endpoint:** `POST /api/admin/returns/{returnId}/reject`
- **Query Parameters:**
  - `reason` (required): Rejection reason
  - `rejectedBy` (optional, default: "ADMIN")

#### Update Return Status
- **Endpoint:** `PUT /api/admin/returns/{returnId}/status`
- **Query Parameters:**
  - `updatedBy` (optional, default: "ADMIN")
- **Request Body:**
```json
{
  "status": "PENDING_APPROVAL | APPROVED | REJECTED | IN_TRANSIT | RECEIVED | PROCESSING_REFUND | REFUNDED | COMPLETED",
  "notes": "string (optional)"
}
```

#### Mark Return as Received
- **Endpoint:** `POST /api/admin/returns/{returnId}/received`
- **Query Parameters:**
  - `notes` (optional)
  - `receivedBy` (optional, default: "ADMIN")
- **Description:** Marks return as received and triggers automatic refund processing

#### Process Refund Manually
- **Endpoint:** `POST /api/admin/returns/{returnId}/refund`
- **Query Parameters:**
  - `processedBy` (optional, default: "ADMIN")
- **Description:** Manually triggers refund processing

#### Get Return Analytics
- **Endpoint:** `GET /api/admin/returns/analytics`
- **Response:** `ReturnAnalyticsResponse` with comprehensive metrics

## Return Status Flow

1. **PENDING_APPROVAL** - Return request submitted, awaiting approval
2. **APPROVED** - Return approved, return label generated
3. **REJECTED** - Return rejected (outside policy, invalid, etc.)
4. **IN_TRANSIT** - Return package in transit back to warehouse
5. **RECEIVED** - Return package received at warehouse
6. **PROCESSING_REFUND** - Refund being processed
7. **REFUNDED** - Refund completed
8. **COMPLETED** - Return process fully completed

## Return Types

- **REFUND_TO_PAYMENT** - Refund to original payment method
- **STORE_CREDIT** - Refund as store credit
- **EXCHANGE** - Exchange for different item

## Return Reasons

- **DEFECTIVE** - Item is defective or damaged
- **WRONG_ITEM** - Wrong item received
- **NOT_AS_DESCRIBED** - Item not as described
- **CHANGED_MIND** - Customer changed mind
- **SIZE_COLOR_ISSUE** - Size or color issue
- **OTHER** - Other reason

## Configuration

Return policy configuration is managed in `application.yml`:

```yaml
return:
  enabled: true
  policy:
    return-window-days: 30
    restocking-fee-percentage: 0
    free-return-threshold: 0
    auto-approve-threshold: 100.00
  email:
    enabled: true
  shipping:
    label-generation:
      enabled: true
      carrier: ECOMPOC
  refund:
    processing-days: 1
    auto-process-on-received: true
```

## Automated Processing

### Auto-Approval
- Returns under the `auto-approve-threshold` (default: $100) are automatically approved
- Returns over the threshold require manual admin review
- Auto-approval triggers return label generation

### Auto-Refund
- When a return is marked as "RECEIVED", refund processing is automatically triggered
- Refunds are processed within 1 business day (configurable)
- Refund amount is calculated: original price - restocking fees

## RMA Number Format

RMA numbers follow the format: `RMA-YYYYMMDD-XXXXX`
- `RMA` - Prefix
- `YYYYMMDD` - Date (e.g., 20241217)
- `XXXXX` - 5-digit random sequence

## User Guide

### For Customers

1. **Initiating a Return:**
   - Navigate to "Returns" in the main menu
   - Select the order you want to return
   - Select items to return and provide return reason
   - Choose return type (Refund, Store Credit, or Exchange)
   - Submit the return request
   - You'll receive an RMA number via email

2. **Tracking a Return:**
   - Navigate to "Track Return"
   - Enter your RMA number
   - View return status, timeline, and refund information

3. **Exchanges:**
   - When creating a return, select "Exchange" as return type
   - After return is approved, you can select the exchange item
   - Price differences are automatically calculated

### For Administrators

1. **Managing Returns:**
   - Navigate to "Admin - Returns"
   - View return queue with filters
   - Review return details
   - Approve or reject returns
   - Update return status manually
   - Mark returns as received
   - Process refunds manually if needed

2. **Analytics:**
   - View comprehensive return analytics
   - Monitor return rates, reasons, and trends
   - Identify product quality issues
   - Track return processing times

## Integration Points

### Payment Gateway Integration
The refund service includes a stub for payment gateway integration. To integrate:
1. Update `RefundService.processRefundThroughGateway()` method
2. Add payment gateway SDK dependency
3. Configure payment gateway credentials
4. Implement actual refund API calls

### Shipping Carrier Integration
The return shipping service includes a stub for carrier API integration. To integrate:
1. Update `ReturnShippingService.generateReturnLabel()` method
2. Add carrier SDK dependency (USPS, FedEx, UPS)
3. Configure carrier API credentials
4. Implement label generation API calls

### Email Service Integration
The return email service follows the existing email service pattern. To integrate:
1. Update `ReturnEmailService` methods
2. Add email service SDK dependency (SendGrid, AWS SES, etc.)
3. Configure email service credentials
4. Implement email sending logic

## Testing

### Backend Tests
- Unit tests for all services (>80% coverage required)
- Integration tests for return submission workflow
- Integration tests for approval and refund processing

### Frontend Tests
- Component tests for return forms and tracking
- Page tests for return workflows
- Service tests for API integration

### E2E Tests
- Selenium tests for complete return flow:
  - Return submission
  - Auto-approval
  - Return tracking
  - Refund processing

## Notes

- All email functionality is stubbed initially (logs only, ready for integration)
- Payment gateway integration is stubbed (ready for Stripe, PayPal, etc.)
- Shipping carrier integration is stubbed (ready for USPS, FedEx, UPS)
- Return policy is configurable via database and application.yml
- Mobile-optimized UI components follow responsive design patterns

