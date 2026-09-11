Gym Supplements Wallet Management System

A Spring Boot backend for managing a gym's supplement sales using a prepaid wallet system instead of per-purchase payments. Admins create customer accounts and deposit money (collected as cash) into customer wallets; customers spend from their wallet balance to purchase supplements. Built as a learning project focused on Spring Security (JWT), transactional wallet logic, and REST API design.

How It Works
Only Admins can create customer accounts (no public self-signup)
Admin deposits cash into a customer's wallet, logged in a deposit history
Customers purchase products; the price is deducted from their wallet balance
Purchases are blocked if the wallet balance is insufficient
Every purchase and deposit is logged with a timestamp for audit purposes
Leftover wallet balance carries forward indefinitely (works like a gift card — no rounding, no cash-back)
Tech Stack
Java 17, Spring Boot 3.3.4
Spring Security + JWT (jjwt) for stateless authentication
Spring Data JPA + Hibernate for persistence
MySQL as the database
BCrypt for password hashing
Lombok for boilerplate reduction
Maven for build/dependency management
Architecture

Layered architecture: Controller → Service → Repository, with a global exception handler and a JWT authentication filter sitting in front of every request.

entity/       → JPA entities (User, Wallet, Product, DepositHistory, PurchaseHistory)
repository/   → Spring Data JPA repositories
service/      → Business logic (AuthService, AdminService, CustomerService)
controller/   → REST endpoints
security/     → JWT utilities, filter, and Spring Security integration
dto/          → Request/response objects (never expose entities directly)
exception/    → Custom exceptions + global exception handler
config/       → Spring Security configuration
Key Design Decisions
BigDecimal for all money fields — avoids floating-point rounding errors that double/float can introduce.
Price/product name snapshotted at purchase time — PurchaseHistory stores productName and priceAtPurchase directly instead of just a foreign key, so historical records stay accurate even if a product's price changes later.
DTOs everywhere, entities never exposed — request/response shapes are decoupled from database entities, so sensitive fields (like password hashes) never leak into API responses.
@Transactional on wallet-affecting operations — deposit and purchase operations update the wallet balance and write an audit record together, so a partial failure never leaves the wallet and the audit trail out of sync.
API Endpoints
Auth
Method	Endpoint	Description
POST	/api/auth/login	Login for both Admin and Customer
Admin
Method	Endpoint	Description
POST	/api/admin/customers	Create a customer account
POST	/api/admin/customers/{id}/deposit	Deposit money into a customer's wallet
GET	/api/admin/customers	List all customers
GET	/api/admin/customers/{id}/wallet	View a customer's wallet balance
GET	/api/admin/customers/{id}/deposits	View a customer's deposit history
GET	/api/admin/customers/{id}/purchases	View a customer's purchase history
POST	/api/admin/products	Create a product
PUT	/api/admin/products/{id}	Update a product
DELETE	/api/admin/products/{id}	Delete a product
GET	/api/admin/products	List all products
Customer
Method	Endpoint	Description
GET	/api/customer/wallet	View own wallet balance
GET	/api/customer/products	Browse available products
POST	/api/customer/purchase/{productId}	Purchase a product
GET	/api/customer/purchases	View own purchase history
Running Locally
Clone the repo and open in your IDE
Create a MySQL database (or let the app auto-create it via createDatabaseIfNotExist=true)
Update src/main/resources/application.properties with your local MySQL username/password
Run WalletSystemApplication.java
Since there's no public signup, manually insert the first Admin user directly into the users table with a BCrypt-hashed password (any online BCrypt generator, or use a throwaway script, to produce the hash)
Test endpoints using Postman, starting with /api/auth/login
Possible Future Improvements
Optimistic locking (@Version) on the Wallet entity to prevent race conditions on concurrent purchases
Simple HTML/JS frontend for Admin and Customer dashboards
Pagination on history endpoints for customers with many transactions