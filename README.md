# Flight Service Database System

## Overview
This is a comprehensive flight service database system designed for a database college course final project. The system manages flights, bookings, clients, rewards, and includes advanced features such as JSON data storage, fulltext search, and complex business logic.

## Database Schema
The system implements a well-normalized database design (3NF) with the following tables:

1. **Clients** - Stores client information
2. **Airports** - Contains airport details with fulltext search capability
3. **Plane** - Lists airplane models and capacities
4. **Flights** - Stores flight details including routes and pricing
5. **Bookings** - Manages flight reservations with JSON passenger details
6. **Rewards** - Tracks reward points earned and redeemed
7. **AuditLog** - Records important system changes for auditing

## Advanced Features

### 1. JSON Data Storage
- Passenger details are stored as JSON, allowing flexible data structure
- JSON querying and manipulation capabilities demonstrate advanced SQL features

### 2. Fulltext Search
- Airport search using MySQL's fulltext search capabilities
- Allows natural language searching across airport names, cities, and countries

### 3. Business Intelligence Views
- `FlightAvailability` - Shows real-time seat availability
- `ClientRewardPoints` - Calculates client point balances
- `FlightStatistics` - Provides analytics on flight occupancy and performance

### 4. Complex Business Logic
- Flight booking with seat validation
- Reward points system with earning and redemption
- Flight delay handling with automatic compensation
- Booking cancellation policies based on time to departure

## Workflows (Required Project Features)

1. **Flight Search** - Search available flights between airports on specific dates
2. **Booking Management** - Create and cancel bookings with business rules
3. **Passenger Information** - Store and retrieve detailed passenger information
4. **Reward System** - Earn and redeem points with flight bookings
5. **Flight Status Management** - Handle flight delays and cancellations with cascading effects

## Testing the Database

### Setup
1. Run the entire `FlightDatabase.sql` script to create the database and sample data
2. Use `TestFlightDatabase.sql` to test individual features

### Testing Individual Features
The test script is organized into sections for testing different aspects of the system:

- **Schema Verification** - Validates the database structure
- **Data Verification** - Checks sample data integrity
- **Workflow Tests** - Tests each of the five required workflows
- **Error Handling Tests** - Validates the system's error handling capabilities

## Implementation Notes

### Performance Considerations
- Appropriate indexes on foreign keys
- Use of views for commonly accessed data combinations
- Fulltext indexes for efficient searching

### Security Features
- Audit logging for sensitive operations
- Input validation in stored procedures
- Business rule enforcement via triggers and stored procedures

## Future Enhancements
- Implement partitioning for improved performance on large datasets
- Add user roles and row-level security
- Implement more advanced analytics and reporting 