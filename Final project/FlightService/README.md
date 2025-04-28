# Flight Service Management System

## Overview

The Flight Service Management System is a comprehensive application that allows users to manage flights, bookings, passenger details, and reward points. This system was developed as a final project for CSC3810 (Database Management Systems).

## Project Team Members

- [Student 1 Name]
- [Student 2 Name]
- [Student 3 Name]

## Features

This system implements five main workflows:

1. **Flight Search**: Search for available flights between airports on specific dates.
2. **Flight Booking**: Book flights for clients with seat selection and passenger details.
3. **View Bookings**: View a client's bookings and reward point balance.
4. **Cancel Booking**: Cancel existing bookings with potential refund of reward points.
5. **Redeem Points**: Apply reward points for discounts on bookings.

## Technology Stack

- **Database**: MySQL 8.0
- **Backend**: Java 11
- **Build Tool**: Maven
- **Testing Framework**: JUnit 5, Mockito

## System Architecture

The application follows a layered architecture with clear separation of concerns:

1. **Presentation Layer**: Console-based user interface for user interaction.
2. **Business Logic Layer**: Implements business rules and coordinates workflows.
3. **Data Access Layer (DAL)**: Handles database operations via stored procedures.
4. **Model Layer**: Represents data entities.

## Database Design

The database is designed in Third Normal Form (3NF) and includes the following entities:

- Clients
- Airports
- Plane
- Flights
- Bookings
- Rewards
- AuditLog

The database includes views, triggers, and stored procedures to implement complex business logic.

## Advanced Features

- **JSON Storage**: Passenger details are stored as JSON, allowing flexible data structure.
- **Full-text Search**: Airport search using MySQL's fulltext search capabilities.
- **Business Analytics**: Flight statistics including occupancy rates and cancellation patterns.
- **Audit Logging**: Tracking of changes to important data.
- **Reward Point System**: Points earned for bookings and redeemable for discounts.

## Getting Started

### Prerequisites

- JDK 11 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### Database Setup

1. Run MySQL server
2. Execute the `FlightDatabase.sql` script to create the database schema and sample data:

```
mysql -u [username] -p < Final\ project/FlightDatabase.sql
```

### Configure Database Connection

Edit the `database.properties` file in `src/main/resources` to match your MySQL settings:

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/FlightService
db.username=[your_username]
db.password=[your_password]
db.poolSize=10
```

### Build and Run

On Windows, simply run the `run.bat` file.

Or manually:

1. Build the project:
```
mvn clean package
```

2. Run the application:
```
java -jar target/flightservice-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Testing

The application includes unit tests for the business layer. To run the tests:

```
mvn test
```

You can also test database functionality directly using the `TestFlightDatabase.sql` script:

```
mysql -u [username] -p FlightService < Final\ project/TestFlightDatabase.sql
```

## Documentation

Additional documentation is available in the `docs` folder:

- `ER_Diagram.md`: Entity-Relationship diagram and database schema description
- `UML_Diagram.md`: UML class diagram and application architecture
- `Sequence_Diagrams.md`: Sequence diagrams for the five main workflows 