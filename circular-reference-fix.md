# Circular Reference Fix for Payment System

## Issue Fixed
Fixed the circular reference issue in entity relationships that was causing the error:
`Document nesting depth (1001) exceeds the maximum allowed (1000)`

## Changes Made

### 1. Added JSON Annotations to Break Circular References
- Added `@JsonIdentityInfo` to entity classes to ensure proper object identification during serialization
- Used `@JsonIgnoreProperties` to break bidirectional relationships in the model
- Fixed relationship annotations (changing from `@OneToOne` to `@ManyToOne` for Invoice-Payment)

### 2. Implemented DTO Pattern
- Created `PaymentDTO` to transfer data without exposing complex entity relationships
- Modified `PaymentService` to convert entities to DTOs
- Updated `PaymentController` to use DTOs instead of entities

### 3. Proper Collections Initialization
- Initialized collections with empty ArrayLists to prevent null pointer exceptions

## Technical Details
The main issue was caused by circular references between:
- `Payment` ↔ `Invoice` 
- `Order` ↔ `Invoice`
- `OrderItem` ↔ `Order` and `Refund`

These bidirectional relationships created infinite recursion during JSON serialization, which reached the maximum nesting limit. By implementing proper Jackson annotations and DTO pattern, we've broken these cycles while maintaining the data relationships.

## Testing
The payments page should now load correctly without the nesting depth error. All filtering and display functionality should work as expected.
