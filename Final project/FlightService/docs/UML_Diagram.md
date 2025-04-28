# Flight Service Java Application UML Diagram

## Class Diagram

```mermaid
classDiagram
    %% Presentation Layer
    class FlightServiceApp {
        -Scanner scanner
        -FlightService flightService
        -BookingService bookingService  
        -ClientService clientService
        +main(String[] args)
        +run()
        -displayMainMenu()
        -searchFlights()
        -bookFlight()
        -viewBookings()
        -cancelBooking()
        -redeemPoints()
        -getStringInput(String prompt)
        -getIntInput(String prompt)
    }
    
    %% Business Logic Layer
    class FlightService {
        -FlightDAO flightDAO
        -AirportDAO airportDAO
        +FlightService(FlightDAO, AirportDAO)
        +searchFlights(String, String, LocalDate) List~Flight~
        +getFlightDetails(int) Flight
        +delayFlight(int, int) void
    }
    
    class BookingService {
        -BookingDAO bookingDAO
        -ClientDAO clientDAO
        -FlightDAO flightDAO
        -ObjectMapper objectMapper
        +BookingService(BookingDAO, ClientDAO, FlightDAO)
        +bookFlight(int, int, String) int
        +updatePassengerDetails(int, String, String, String, String, String) void
        +cancelBooking(int) void
        +getClientBookings(int) List~Booking~
        +getBookingDetails(int) Booking
        +redeemPointsForDiscount(int, int, int) double
    }
    
    class ClientService {
        -ClientDAO clientDAO
        +ClientService(ClientDAO)
        +getAllClients() List~Client~
        +getClientDetails(int) Client
        +getClientRewardPoints(int) int
    }
    
    class ServiceException {
        +ServiceException(String)
        +ServiceException(String, Throwable)
        +ServiceException(Throwable)
    }
    
    %% Data Access Layer
    class DatabaseConnector {
        -String dbUrl
        -String dbUsername
        -String dbPassword
        -String dbDriver
        -loadDatabaseProperties() void
        +getConnection() Connection
        +closeConnection(Connection) void
    }
    
    class AirportDAO {
        +getAllAirports() List~Airport~
        +searchAirports(String) List~Airport~
        +getAirportById(int) Airport
        +getAirportByCode(String) Airport
        -closeResources(Connection, CallableStatement, ResultSet) void
    }
    
    class FlightDAO {
        +getAvailableFlights(String, String, LocalDate) List~Flight~
        +getFlightById(int) Flight
        +delayFlight(int, int) boolean
        -closeResources(Connection, CallableStatement, ResultSet) void
    }
    
    class BookingDAO {
        -ObjectMapper objectMapper
        +bookFlight(int, int, String) int
        +updatePassengerDetails(int, JsonNode) boolean
        +cancelBooking(int) boolean
        +getClientBookings(int) List~Booking~
        +getBookingById(int) Booking
        +redeemPointsForDiscount(int, int, int) double
        -closeResources(Connection, CallableStatement, ResultSet) void
    }
    
    class ClientDAO {
        +getAllClients() List~Client~
        +getClientById(int) Client
        +getClientRewardPoints(int) int
        -closeResources(Connection, CallableStatement, ResultSet) void
    }
    
    %% Model Layer
    class Airport {
        -int airportId
        -String airportCode
        -String airportName
        -String city
        -String country
        +getters()
        +setters()
        +toString()
    }
    
    class Booking {
        -int bookingId
        -int clientId
        -int flightId
        -LocalDateTime bookingDate
        -String seatNumber
        -String status
        -JsonNode passengerDetails
        -String clientName
        -String flightNumber
        -String originAirportCode
        -String destinationAirportCode
        -LocalDateTime departureDay
        -LocalDateTime arrivalDay
        +getters()
        +setters()
        +toString()
    }
    
    class Client {
        -int clientId
        -String clientName
        -String email
        -LocalDate dateOfSubscription
        -int rewardPoints
        +getters()
        +setters()
        +toString()
    }
    
    class Flight {
        -int flightId
        -String flightNumber
        -int planeId
        -int originAirportId
        -int destinationAirportId
        -LocalDateTime departureDay
        -LocalDateTime arrivalDay
        -BigDecimal basePrice
        -String status
        -String originAirportCode
        -String originCity
        -String destinationAirportCode
        -String destinationCity
        -int availableSeats
        +getters()
        +setters()
        +toString()
    }
    
    %% Relationships - Presentation to Business
    FlightServiceApp --> FlightService : uses
    FlightServiceApp --> BookingService : uses
    FlightServiceApp --> ClientService : uses
    
    %% Relationships - Business to DAL
    FlightService --> FlightDAO : uses
    FlightService --> AirportDAO : uses
    BookingService --> BookingDAO : uses
    BookingService --> ClientDAO : uses
    BookingService --> FlightDAO : uses
    ClientService --> ClientDAO : uses
    
    %% Relationships - DAL to Database
    AirportDAO --> DatabaseConnector : uses
    FlightDAO --> DatabaseConnector : uses
    BookingDAO --> DatabaseConnector : uses
    ClientDAO --> DatabaseConnector : uses
    
    %% Relationships - DAL to Model
    AirportDAO --> Airport : uses
    FlightDAO --> Flight : uses
    BookingDAO --> Booking : uses
    ClientDAO --> Client : uses
    
    %% Exception Handling
    FlightService --> ServiceException : throws
    BookingService --> ServiceException : throws
    ClientService --> ServiceException : throws
```

## Application Architecture

The Flight Service Java application follows a layered architecture with clear separation of concerns:

### 1. Presentation Layer
- **FlightServiceApp**: Console-based user interface that handles user input and display of results.
- Responsibilities:
  - Providing a menu-based interface for users
  - Collecting input parameters for operations
  - Displaying results in a user-friendly format
  - Basic input validation

### 2. Business Logic Layer
- **FlightService**: Contains business logic related to flights and airports.
- **BookingService**: Contains business logic related to bookings and passenger details.
- **ClientService**: Contains business logic related to clients and reward points.
- **ServiceException**: Custom exception for business logic errors.
- Responsibilities:
  - Input validation
  - Enforcing business rules
  - Coordinating transactions across multiple DAOs
  - Transforming data between presentation and data access layers

### 3. Data Access Layer (DAL)
- **DatabaseConnector**: Handles database connection management.
- **AirportDAO**: Data access for Airport entity.
- **FlightDAO**: Data access for Flight entity.
- **BookingDAO**: Data access for Booking entity.
- **ClientDAO**: Data access for Client entity.
- Responsibilities:
  - Executing SQL queries and stored procedures
  - Converting resultsets to model objects
  - Managing database resources
  - Handling low-level database exceptions

### 4. Model Layer
- **Airport**: Represents airport data.
- **Flight**: Represents flight data.
- **Booking**: Represents booking data.
- **Client**: Represents client data.
- Responsibilities:
  - Storing data
  - Providing getters and setters
  - Basic data validation

## Design Patterns Used

1. **Data Access Object (DAO) Pattern**: Separates database access logic from business logic.
2. **Dependency Injection**: Services are injected with their dependencies via constructors.
3. **Exception Handling Pattern**: Custom exceptions for business logic errors.
4. **Model-View-Controller (MVC)**: Though adapted for a console application, the design follows MVC principles.

## Flow of Control

1. User interacts with the presentation layer (FlightServiceApp)
2. The presentation layer calls appropriate methods in the business logic layer
3. The business logic layer validates input and applies business rules
4. The business logic layer calls appropriate methods in the data access layer
5. The data access layer interacts with the database via stored procedures
6. Results flow back through the layers to be presented to the user 