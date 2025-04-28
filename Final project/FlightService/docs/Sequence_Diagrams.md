# Flight Service Sequence Diagrams

This document contains sequence diagrams for the five main workflows in the Flight Service application.

## Workflow 1: Search Flights

```mermaid
sequenceDiagram
    actor User
    participant UI as FlightServiceApp
    participant FS as FlightService
    participant ADAO as AirportDAO
    participant FDAO as FlightDAO
    participant DB as Database
    
    User->>UI: Enter flight search criteria
    UI->>FS: searchFlights(originCode, destCode, departureDate)
    FS->>FS: Validate inputs
    FS->>ADAO: getAirportByCode(originCode)
    ADAO->>DB: SELECT query
    DB-->>ADAO: Airport data
    ADAO-->>FS: Origin Airport
    
    FS->>ADAO: getAirportByCode(destCode)
    ADAO->>DB: SELECT query
    DB-->>ADAO: Airport data
    ADAO-->>FS: Destination Airport
    
    FS->>FDAO: getAvailableFlights(originCode, destCode, departureDate)
    FDAO->>DB: CALL GetAvailableFlights stored procedure
    DB-->>FDAO: Available flights
    FDAO-->>FS: List<Flight>
    FS-->>UI: List<Flight>
    UI-->>User: Display available flights
```

## Workflow 2: Book Flight

```mermaid
sequenceDiagram
    actor User
    participant UI as FlightServiceApp
    participant BS as BookingService
    participant CDAO as ClientDAO
    participant FDAO as FlightDAO
    participant BDAO as BookingDAO
    participant DB as Database
    
    User->>UI: Select client and flight
    User->>UI: Enter seat number
    UI->>BS: bookFlight(clientId, flightId, seatNumber)
    BS->>BS: Validate inputs
    
    BS->>CDAO: getClientById(clientId)
    CDAO->>DB: SELECT query
    DB-->>CDAO: Client data
    CDAO-->>BS: Client
    
    BS->>FDAO: getFlightById(flightId)
    FDAO->>DB: SELECT query
    DB-->>FDAO: Flight data
    FDAO-->>BS: Flight
    
    BS->>BDAO: bookFlight(clientId, flightId, seatNumber)
    BDAO->>DB: CALL BookFlight stored procedure
    DB-->>BDAO: Booking ID
    BDAO-->>BS: Booking ID
    BS-->>UI: Booking ID
    UI-->>User: Display booking confirmation
    
    User->>UI: Enter passenger details
    UI->>BS: updatePassengerDetails(bookingId, firstName, lastName, ...)
    BS->>BS: Validate inputs
    BS->>BDAO: getBookingById(bookingId)
    BDAO->>DB: SELECT query
    DB-->>BDAO: Booking data
    BDAO-->>BS: Booking
    
    BS->>BS: Create JSON passenger details
    BS->>BDAO: updatePassengerDetails(bookingId, passengerDetails)
    BDAO->>DB: CALL UpdatePassengerDetails stored procedure
    DB-->>BDAO: Success status
    BDAO-->>BS: Success status
    BS-->>UI: Success status
    UI-->>User: Display passenger details confirmation
```

## Workflow 3: View Bookings

```mermaid
sequenceDiagram
    actor User
    participant UI as FlightServiceApp
    participant BS as BookingService
    participant CS as ClientService
    participant CDAO as ClientDAO
    participant BDAO as BookingDAO
    participant DB as Database
    
    User->>UI: Select client
    UI->>BS: getClientBookings(clientId)
    BS->>BS: Validate inputs
    
    BS->>CDAO: getClientById(clientId)
    CDAO->>DB: SELECT query
    DB-->>CDAO: Client data
    CDAO-->>BS: Client
    
    BS->>BDAO: getClientBookings(clientId)
    BDAO->>DB: CALL GetClientBookings stored procedure
    DB-->>BDAO: Bookings data
    BDAO-->>BS: List<Booking>
    BS-->>UI: List<Booking>
    
    UI->>CS: getClientRewardPoints(clientId)
    CS->>CS: Validate inputs
    
    CS->>CDAO: getClientById(clientId)
    CDAO->>DB: SELECT query
    DB-->>CDAO: Client data
    CDAO-->>CS: Client
    
    CS->>CDAO: getClientRewardPoints(clientId)
    CDAO->>DB: SELECT from ClientRewardPoints view
    DB-->>CDAO: Reward points
    CDAO-->>CS: Reward points
    CS-->>UI: Reward points
    
    UI-->>User: Display bookings and points
```

## Workflow 4: Cancel Booking

```mermaid
sequenceDiagram
    actor User
    participant UI as FlightServiceApp
    participant BS as BookingService
    participant BDAO as BookingDAO
    participant DB as Database
    
    User->>UI: Select booking to cancel
    UI->>BS: cancelBooking(bookingId)
    BS->>BS: Validate inputs
    
    BS->>BDAO: getBookingById(bookingId)
    BDAO->>DB: SELECT query
    DB-->>BDAO: Booking data
    BDAO-->>BS: Booking
    
    BS->>BDAO: cancelBooking(bookingId)
    BDAO->>DB: CALL CancelBooking stored procedure
    Note right of DB: SP checks departure time
    Note right of DB: SP handles refund points if eligible
    DB-->>BDAO: Success status
    BDAO-->>BS: Success status
    BS-->>UI: Success status
    UI-->>User: Display cancellation confirmation
```

## Workflow 5: Redeem Points for Discount

```mermaid
sequenceDiagram
    actor User
    participant UI as FlightServiceApp
    participant BS as BookingService
    participant CS as ClientService
    participant CDAO as ClientDAO
    participant BDAO as BookingDAO
    participant DB as Database
    
    User->>UI: Select client
    UI->>CS: getClientRewardPoints(clientId)
    CS->>CS: Validate inputs
    CS->>CDAO: getClientById(clientId)
    CDAO->>DB: SELECT query
    DB-->>CDAO: Client data
    CDAO-->>CS: Client
    
    CS->>CDAO: getClientRewardPoints(clientId)
    CDAO->>DB: SELECT from ClientRewardPoints view
    DB-->>CDAO: Reward points
    CDAO-->>CS: Reward points
    CS-->>UI: Reward points
    UI-->>User: Display available points
    
    User->>UI: Select booking and points to redeem
    UI->>BS: redeemPointsForDiscount(clientId, pointsToRedeem, bookingId)
    BS->>BS: Validate inputs
    
    BS->>CDAO: getClientById(clientId)
    CDAO->>DB: SELECT query
    DB-->>CDAO: Client data
    CDAO-->>BS: Client
    
    BS->>CDAO: getClientRewardPoints(clientId)
    CDAO->>DB: SELECT from ClientRewardPoints view
    DB-->>CDAO: Available points
    CDAO-->>BS: Available points
    
    BS->>BDAO: getBookingById(bookingId)
    BDAO->>DB: SELECT query
    DB-->>BDAO: Booking data
    BDAO-->>BS: Booking
    
    BS->>BDAO: redeemPointsForDiscount(clientId, pointsToRedeem, bookingId)
    BDAO->>DB: CALL RedeemPointsForDiscount stored procedure
    Note right of DB: SP validates eligibility
    Note right of DB: SP calculates discount
    Note right of DB: SP records points usage
    DB-->>BDAO: Discount amount and remaining points
    BDAO-->>BS: Discount amount
    BS-->>UI: Discount amount
    
    UI->>CS: getClientRewardPoints(clientId)
    CS->>CDAO: getClientRewardPoints(clientId)
    CDAO->>DB: SELECT from ClientRewardPoints view
    DB-->>CDAO: Updated points
    CDAO-->>CS: Updated points
    CS-->>UI: Updated points
    
    UI-->>User: Display discount and remaining points
```

## Activity Diagram: Overall System Flow

```mermaid
graph TD
    Start([Start Application]) --> MainMenu[Display Main Menu]
    MainMenu --> Choice{User Choice}
    
    Choice -->|1| SearchFlights[Search Flights]
    SearchFlights --> GetSearchCriteria[Get Search Criteria]
    GetSearchCriteria --> ValidateSearchCriteria{Validate Criteria}
    ValidateSearchCriteria -->|Valid| ExecuteSearch[Execute Flight Search]
    ValidateSearchCriteria -->|Invalid| ShowSearchError[Show Error Message]
    ExecuteSearch --> DisplayResults[Display Flight Results]
    ShowSearchError --> MainMenu
    DisplayResults --> MainMenu
    
    Choice -->|2| BookFlight[Book a Flight]
    BookFlight --> SelectClient[Select Client]
    SelectClient --> SearchFlightToBook[Search for Flight]
    SearchFlightToBook --> SelectFlight[Select Flight]
    SelectFlight --> EnterSeatNumber[Enter Seat Number]
    EnterSeatNumber --> ValidateBookingInputs{Validate Inputs}
    ValidateBookingInputs -->|Valid| ExecuteBooking[Execute Booking]
    ValidateBookingInputs -->|Invalid| ShowBookingError[Show Error Message]
    ExecuteBooking --> EnterPassengerDetails[Enter Passenger Details]
    EnterPassengerDetails --> SavePassengerDetails[Save Passenger Details]
    SavePassengerDetails --> MainMenu
    ShowBookingError --> MainMenu
    
    Choice -->|3| ViewBookings[View Bookings]
    ViewBookings --> SelectClientForBookings[Select Client]
    SelectClientForBookings --> FetchBookings[Fetch Client's Bookings]
    FetchBookings --> DisplayBookings[Display Bookings]
    DisplayBookings --> MainMenu
    
    Choice -->|4| CancelBooking[Cancel Booking]
    CancelBooking --> SelectClientForCancel[Select Client]
    SelectClientForCancel --> FetchBookingsForCancel[Fetch Client's Bookings]
    FetchBookingsForCancel --> SelectBookingToCancel[Select Booking to Cancel]
    SelectBookingToCancel --> ConfirmCancellation{Confirm Cancellation}
    ConfirmCancellation -->|Yes| ExecuteCancellation[Execute Cancellation]
    ConfirmCancellation -->|No| AbortCancellation[Abort Cancellation]
    ExecuteCancellation --> ShowCancellationResult[Show Cancellation Result]
    AbortCancellation --> MainMenu
    ShowCancellationResult --> MainMenu
    
    Choice -->|5| RedeemPoints[Redeem Points]
    RedeemPoints --> SelectClientForRedemption[Select Client]
    SelectClientForRedemption --> FetchClientPoints[Fetch Client's Points]
    FetchClientPoints --> DisplayAvailablePoints[Display Available Points]
    DisplayAvailablePoints --> SelectBookingForDiscount[Select Booking for Discount]
    SelectBookingForDiscount --> EnterPointsToRedeem[Enter Points to Redeem]
    EnterPointsToRedeem --> ValidatePointsRedemption{Validate Redemption}
    ValidatePointsRedemption -->|Valid| ExecuteRedemption[Execute Redemption]
    ValidatePointsRedemption -->|Invalid| ShowRedemptionError[Show Error Message]
    ExecuteRedemption --> DisplayDiscountAndRemainingPoints[Display Discount and Remaining Points]
    ShowRedemptionError --> MainMenu
    DisplayDiscountAndRemainingPoints --> MainMenu
    
    Choice -->|0| Exit[Exit Application]
    Exit --> End([End Application])
``` 