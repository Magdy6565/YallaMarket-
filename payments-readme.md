# Payments System in YallaMarket

The Payments page is designed to help administrators view and manage all payment transactions in the YallaMarket platform.

## Features

- **Payment Listing**: View all payments with pagination
- **Filtering Options**:
  - By Payment ID
  - By Order ID
  - By User ID
  - By Amount Range (min/max)
  - By Payment Status (PENDING, COMPLETED, FAILED, REFUNDED)
- **Detailed View**: Click on a payment to see full details
- **Order Navigation**: Direct links to related orders
- **Responsive Design**: Works on desktop and mobile devices

## Technical Implementation

### Backend

- **Model**: `Payment.java` - Represents payment data with relationships
- **Repository**: `PaymentRepository.java` - Provides data access with JPA Specification for filtering
- **Service**: `PaymentService.java` - Business logic and filtering operations
- **Controllers**:
  - `PaymentController.java` - REST API endpoints for payment data
  - `PaymentViewController.java` - MVC controller for the payments HTML page

### Frontend

- **HTML**: `payments.html` - The payment management interface
- **CSS**: `payments.css` - Styling for the payment page
- **JavaScript**: `payments.js` - Client-side logic for filtering and pagination

## How to Use

1. Log in as an administrator
2. Navigate to the Payments page using the sidebar
3. Use filters at the top to narrow down payment results
4. Click on a payment to view detailed information
5. Use pagination controls at the bottom to navigate through results

## Security

Access to the payments page is restricted to users with administrative privileges. All API endpoints are secured against unauthorized access.
