# YallaMarket

YallaMarket is a comprehensive Business-to-Business (B2B) e-commerce platform that facilitates seamless transactions between vendors and supermarkets. Built with modern Java technologies, the platform provides role-based access control, comprehensive order management, and integrated payment processing.

## 🚀 Features

### Multi-Role User Management
- **Vendors**: Product listing, inventory management, order processing
- **Supermarkets**: Product browsing, order placement, order tracking  
- **Administrators**: Complete system oversight and user management

### Core Functionality
- **Product Management**: Full CRUD operations with categorization and vendor filtering
- **Order Processing**: Complete order lifecycle from placement to fulfillment
- **Payment System**: Integrated payment processing with refund capabilities 
- **Email Notifications**: Automated order updates and refund notifications
- **Security**: JWT-based authentication with role-based authorization

## 🛠 Technology Stack

### Backend
- **Spring Boot 3.4.5** - Application framework 
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer 
- **PostgreSQL** - Primary database 
- **Thymeleaf** - Server-side templating 

### Frontend
- **HTML5/CSS3** - Modern responsive design
- **JavaScript** - Dynamic client-side functionality
- **Bootstrap** - UI component framework

## 📋 Prerequisites

- Java 17 or higher [6](#0-5) 
- PostgreSQL database
- Maven 3.6+

## 🏗 Architecture

The application follows a layered architecture pattern:

- **Presentation Layer**: Thymeleaf templates and REST controllers
- **Business Logic Layer**: Service classes handling core business operations
- **Data Access Layer**: JPA repositories with PostgreSQL integration
- **Security Layer**: JWT-based authentication with role-based access control

## 👥 User Roles

| Role | Access Level | Key Features |
|------|-------------|--------------|
| **Admin** | Full system access | User management, system oversight  |
| **Vendor** | Own products/orders | Product listing, order fulfillment |
| **Supermarket** | Public products, own orders | Product browsing, order placement |

## 🔒 Security Features

- JWT token-based authentication
- BCrypt password encryption
- Role-based access control
- Secure API endpoints 

## 📊 Payment System

The platform includes a comprehensive payment management system with:
- Payment transaction tracking
- Multiple filtering options (by ID, amount, status)
- Refund processing capabilities
- Administrative oversight tools 
