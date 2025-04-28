DROP DATABASE IF EXISTS FlightService;

CREATE DATABASE FlightService;

USE FlightService;

CREATE TABLE Clients(
    ClientId INT NOT NULL AUTO_INCREMENT,
    ClientsName VARCHAR(100) NOT NULL,
    Email VARCHAR(100) NOT NULL,
    DateofSubscription DATE NOT NULL,
    CONSTRAINT CheckEmail CHECK(Email LIKE "%@%.%"),
    PRIMARY KEY(ClientId)
);

CREATE TABLE Airports (
    AirportId INT NOT NULL AUTO_INCREMENT,
    AirportCode CHAR(3) NOT NULL,
    AirportName VARCHAR(100) NOT NULL,
    City VARCHAR(50) NOT NULL,
    Country VARCHAR(50) NOT NULL,
    PRIMARY KEY(AirportId),
    UNIQUE(AirportCode),
    FULLTEXT INDEX idx_airport_search (AirportName, City, Country)
);

CREATE TABLE Plane(
    PlaneId INT NOT NULL AUTO_INCREMENT,
    PlaneModel VARCHAR(50) NOT NULL,
    AvailableSeats INT NOT NULL,
    PRIMARY KEY(PlaneId)
);

CREATE TABLE Flights(
    FlightId INT NOT NULL AUTO_INCREMENT,
    FlightNumber VARCHAR(10) NOT NULL,
    PlaneId INT NOT NULL,
    OriginAirportId INT NOT NULL,
    DestinationAirportId INT NOT NULL,
    DepartureDay DATETIME NOT NULL,
    ArrivalDay DATETIME NOT NULL,
    BasePrice DECIMAL(10,2) NOT NULL,
    STATUS ENUM("Scheduled", "Delayed", "Cancelled") DEFAULT "Scheduled",
    PRIMARY KEY(FlightId),
    FOREIGN KEY(PlaneId) REFERENCES Plane(PlaneId),
    FOREIGN KEY(OriginAirportId) REFERENCES Airports(AirportId),
    FOREIGN KEY(DestinationAirportId) REFERENCES Airports(AirportId),
    CONSTRAINT CheckDate CHECK (ArrivalDay > DepartureDay)
);

CREATE TABLE Bookings(
    BookingId INT NOT NULL AUTO_INCREMENT,
    ClientId INT NOT NULL,
    FlightId INT NOT NULL,
    BookingDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    SeatNumber VARCHAR(10),
    STATUS ENUM("Confirmed", "Cancelled") DEFAULT "Confirmed",
    PassengerDetails JSON,
    PRIMARY KEY(BookingId),
    FOREIGN KEY(ClientId) REFERENCES Clients(ClientId),
    FOREIGN KEY(FlightId) REFERENCES Flights(FlightId),
    CONSTRAINT CHK_SEAT_FORMAT CHECK (SeatNumber REGEXP '^[0-9]{1,2}[A-F]$')
);

CREATE TABLE Rewards(
    RewardId INT NOT NULL AUTO_INCREMENT,
    ClientId INT NOT NULL,
    PointsEarned INT NOT NULL,
    PointsUsed INT DEFAULT 0,
    TransactionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Description VARCHAR(255),
    PRIMARY KEY(RewardId),
    FOREIGN KEY(ClientId) REFERENCES Clients(ClientId),
    CONSTRAINT CheckPoints CHECK (PointsEarned >= 0 AND PointsUsed >= 0)
);

-- Audit table for tracking important changes
CREATE TABLE AuditLog (
    LogId INT NOT NULL AUTO_INCREMENT,
    TableName VARCHAR(50) NOT NULL,
    RecordId INT NOT NULL,
    Action VARCHAR(10) NOT NULL,
    ChangedBy VARCHAR(100) NOT NULL,
    ChangeDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Details TEXT,
    PRIMARY KEY(LogId)
);

-- View to display client reward point totals
CREATE VIEW ClientRewardPoints AS
SELECT 
    c.ClientId,
    c.ClientsName,
    IFNULL(SUM(r.PointsEarned), 0) AS TotalPointsEarned,
    IFNULL(SUM(r.PointsUsed), 0) AS TotalPointsUsed,
    IFNULL(SUM(r.PointsEarned) - SUM(r.PointsUsed), 0) AS AvailablePoints
FROM 
    Clients c
LEFT JOIN 
    Rewards r ON c.ClientId = r.ClientId
GROUP BY 
    c.ClientId, c.ClientsName;

-- View to display flight availability
CREATE VIEW FlightAvailability AS
SELECT 
    f.FlightId,
    f.FlightNumber,
    o.AirportCode AS Origin,
    d.AirportCode AS Destination,
    f.DepartureDay,
    f.ArrivalDay,
    p.AvailableSeats AS TotalSeats,
    (p.AvailableSeats - COUNT(CASE WHEN b.STATUS = 'Confirmed' THEN 1 ELSE NULL END)) AS AvailableSeats,
    f.BasePrice,
    f.STATUS
FROM 
    Flights f
JOIN 
    Airports o ON f.OriginAirportId = o.AirportId
JOIN 
    Airports d ON f.DestinationAirportId = d.AirportId
JOIN 
    Plane p ON f.PlaneId = p.PlaneId
LEFT JOIN 
    Bookings b ON f.FlightId = b.FlightId
GROUP BY 
    f.FlightId, f.FlightNumber, Origin, Destination, f.DepartureDay, f.ArrivalDay, TotalSeats, f.BasePrice, f.STATUS;

-- View for flight statistics and analytics
CREATE VIEW FlightStatistics AS
SELECT 
    f.FlightNumber,
    o.City AS Origin,
    d.City AS Destination,
    COUNT(b.BookingId) AS TotalBookings,
    AVG(f.BasePrice) AS AveragePrice,
    SUM(CASE WHEN b.STATUS = 'Confirmed' THEN 1 ELSE 0 END) / COUNT(b.BookingId) * 100 AS OccupancyRate,
    (SELECT COUNT(*) FROM Bookings WHERE FlightId = f.FlightId AND STATUS = 'Cancelled') AS CancellationCount,
    MONTHNAME(f.DepartureDay) AS TravelMonth,
    YEAR(f.DepartureDay) AS TravelYear
FROM 
    Flights f
JOIN 
    Airports o ON f.OriginAirportId = o.AirportId
JOIN 
    Airports d ON f.DestinationAirportId = d.AirportId
LEFT JOIN 
    Bookings b ON f.FlightId = b.FlightId
GROUP BY 
    f.FlightNumber, Origin, Destination, TravelMonth, TravelYear;

-- STORED PROCEDURES

-- 1. Search available flights
DELIMITER //
CREATE PROCEDURE GetAvailableFlights(
    IN originCode CHAR(3),
    IN destCode CHAR(3),
    IN departureDate DATE
)
BEGIN
    SELECT 
        f.FlightId, 
        f.FlightNumber, 
        o.AirportCode AS OriginCode,
        o.City AS OriginCity,
        d.AirportCode AS DestinationCode,
        d.City AS DestinationCity,
        f.DepartureDay, 
        f.ArrivalDay, 
        f.BasePrice,
        fa.AvailableSeats
    FROM 
        FlightAvailability fa
    JOIN 
        Flights f ON fa.FlightId = f.FlightId
    JOIN 
        Airports o ON f.OriginAirportId = o.AirportId
    JOIN 
        Airports d ON f.DestinationAirportId = d.AirportId
    WHERE 
        o.AirportCode = originCode
        AND d.AirportCode = destCode
        AND DATE(f.DepartureDay) = departureDate
        AND f.STATUS = 'Scheduled'
        AND fa.AvailableSeats > 0
    ORDER BY 
        f.DepartureDay;
END //
DELIMITER ;

-- 2. Book a flight
DELIMITER //
CREATE PROCEDURE BookFlight(
    IN clientId INT,
    IN flightId INT,
    IN seatNumber VARCHAR(10),
    OUT bookingId INT
)
BEGIN
    DECLARE availableSeats INT;
    DECLARE isSeatTaken BOOLEAN;
    
    -- Check if the flight has available seats
    SELECT fa.AvailableSeats INTO availableSeats
    FROM FlightAvailability fa
    WHERE fa.FlightId = flightId;
    
    -- Check if the seat is already taken
    SELECT COUNT(*) > 0 INTO isSeatTaken
    FROM Bookings
    WHERE FlightId = flightId AND SeatNumber = seatNumber AND STATUS = 'Confirmed';
    
    IF availableSeats <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No available seats on this flight';
    ELSEIF isSeatTaken THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The selected seat is already taken';
    ELSE
        INSERT INTO Bookings(ClientId, FlightId, SeatNumber)
        VALUES(clientId, flightId, seatNumber);
        
        SET bookingId = LAST_INSERT_ID();
        
        -- Add reward points (10% of the base price)
        INSERT INTO Rewards(ClientId, PointsEarned, Description)
        SELECT clientId, FLOOR(BasePrice * 0.1), CONCAT('Points for booking flight ', FlightNumber)
        FROM Flights
        WHERE FlightId = flightId;
    END IF;
END //
DELIMITER ;

-- 3. Cancel booking
DELIMITER //
CREATE PROCEDURE CancelBooking(
    IN bookingIdToCancel INT,
    OUT success BOOLEAN
)
BEGIN
    DECLARE bookingStatus VARCHAR(20);
    DECLARE clientIdFromBooking INT;
    DECLARE flightIdFromBooking INT;
    DECLARE departureTime DATETIME;
    DECLARE currentTime DATETIME;
    DECLARE hoursDifference INT;
    
    SET success = FALSE;
    SET currentTime = NOW();
    
    -- Get booking information
    SELECT STATUS, ClientId, FlightId INTO bookingStatus, clientIdFromBooking, flightIdFromBooking
    FROM Bookings
    WHERE BookingId = bookingIdToCancel;
    
    -- Get flight departure time
    SELECT DepartureDay INTO departureTime
    FROM Flights
    WHERE FlightId = flightIdFromBooking;
    
    -- Calculate hours until departure
    SET hoursDifference = TIMESTAMPDIFF(HOUR, currentTime, departureTime);
    
    -- Check if booking exists and is not already cancelled
    IF bookingStatus IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking not found';
    ELSEIF bookingStatus = 'Cancelled' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking is already cancelled';
    ELSEIF hoursDifference < 24 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot cancel bookings less than 24 hours before departure';
    ELSE
        -- Update booking status
        UPDATE Bookings
        SET STATUS = 'Cancelled'
        WHERE BookingId = bookingIdToCancel;
        
        -- If cancellation is more than 72 hours before departure, refund 50% of the points
        IF hoursDifference >= 72 THEN
            INSERT INTO Rewards(ClientId, PointsEarned, Description)
            SELECT b.ClientId, FLOOR((f.BasePrice * 0.1) * 0.5), CONCAT('Refund for cancelling flight ', f.FlightNumber)
            FROM Bookings b
            JOIN Flights f ON b.FlightId = f.FlightId
            WHERE b.BookingId = bookingIdToCancel;
        END IF;
        
        SET success = TRUE;
    END IF;
END //
DELIMITER ;

-- 4. Get Client Bookings
DELIMITER //
CREATE PROCEDURE GetClientBookings(
    IN clientIdParam INT
)
BEGIN
    SELECT 
        b.BookingId,
        f.FlightNumber,
        o.AirportCode AS Origin,
        o.City AS OriginCity,
        d.AirportCode AS Destination,
        d.City AS DestinationCity,
        f.DepartureDay,
        f.ArrivalDay,
        b.SeatNumber,
        f.BasePrice,
        b.STATUS AS BookingStatus,
        f.STATUS AS FlightStatus
    FROM 
        Bookings b
    JOIN 
        Flights f ON b.FlightId = f.FlightId
    JOIN 
        Airports o ON f.OriginAirportId = o.AirportId
    JOIN 
        Airports d ON f.DestinationAirportId = d.AirportId
    WHERE 
        b.ClientId = clientIdParam
    ORDER BY 
        f.DepartureDay;
END //
DELIMITER ;

-- 5. Redeem Points for Discount
DELIMITER //
CREATE PROCEDURE RedeemPointsForDiscount(
    IN clientIdParam INT,
    IN pointsToRedeem INT,
    IN bookingIdParam INT,
    OUT discountAmount DECIMAL(10,2),
    OUT remainingPoints INT
)
BEGIN
    DECLARE availablePoints INT;
    DECLARE bookingExists BOOLEAN;
    DECLARE flightCost DECIMAL(10,2);
    DECLARE maxDiscount DECIMAL(10,2);
    
    -- Check if client has enough points
    SELECT IFNULL(SUM(PointsEarned) - SUM(PointsUsed), 0) INTO availablePoints
    FROM Rewards
    WHERE ClientId = clientIdParam;
    
    -- Check if booking exists and belongs to client
    SELECT COUNT(*) > 0, f.BasePrice INTO bookingExists, flightCost
    FROM Bookings b
    JOIN Flights f ON b.FlightId = f.FlightId
    WHERE b.BookingId = bookingIdParam AND b.ClientId = clientIdParam AND b.STATUS = 'Confirmed';
    
    -- Maximum discount is 50% of flight cost
    SET maxDiscount = flightCost * 0.5;
    
    IF pointsToRedeem > availablePoints THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Not enough points available';
    ELSEIF NOT bookingExists THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking not found or not eligible for discount';
    ELSE
        -- Calculate discount (1 point = $0.01)
        SET discountAmount = LEAST(pointsToRedeem * 0.01, maxDiscount);
        
        -- Record points usage
        INSERT INTO Rewards(ClientId, PointsUsed, Description)
        VALUES(clientIdParam, pointsToRedeem, CONCAT('Points redeemed for discount on booking #', bookingIdParam));
        
        -- Calculate remaining points
        SET remainingPoints = availablePoints - pointsToRedeem;
    END IF;
END //
DELIMITER ;

-- 6. Update Passenger Details with JSON
DELIMITER //
CREATE PROCEDURE UpdatePassengerDetails(
    IN bookingIdParam INT,
    IN passengerDetailsJSON JSON
)
BEGIN
    -- Validate JSON structure
    IF JSON_VALID(passengerDetailsJSON) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid JSON format for passenger details';
    END IF;
    
    -- Update the booking with passenger details
    UPDATE Bookings
    SET PassengerDetails = passengerDetailsJSON
    WHERE BookingId = bookingIdParam;
    
    -- Log the update
    INSERT INTO AuditLog (TableName, RecordId, Action, ChangedBy, Details)
    VALUES ('Bookings', bookingIdParam, 'UPDATE', USER(), 'Updated passenger details');
END //
DELIMITER ;

-- 7. Flight Delay Handling
DELIMITER //
CREATE PROCEDURE DelayFlight(
    IN flightIdParam INT,
    IN delayMinutes INT
)
BEGIN
    DECLARE oldDeparture DATETIME;
    DECLARE oldArrival DATETIME;
    DECLARE flightNum VARCHAR(10);
    
    -- Get current schedule
    SELECT DepartureDay, ArrivalDay, FlightNumber
    INTO oldDeparture, oldArrival, flightNum
    FROM Flights
    WHERE FlightId = flightIdParam;
    
    -- Update with delay
    UPDATE Flights
    SET 
        DepartureDay = DATE_ADD(oldDeparture, INTERVAL delayMinutes MINUTE),
        ArrivalDay = DATE_ADD(oldArrival, INTERVAL delayMinutes MINUTE),
        STATUS = 'Delayed'
    WHERE FlightId = flightIdParam;
    
    -- Notify affected bookings
    INSERT INTO AuditLog (TableName, RecordId, Action, ChangedBy, Details)
    VALUES ('Flights', flightIdParam, 'DELAY', USER(), 
            CONCAT('Flight ', flightNum, ' delayed by ', delayMinutes, ' minutes'));
    
    -- Add compensation points for delays over 60 minutes
    IF delayMinutes > 60 THEN
        INSERT INTO Rewards (ClientId, PointsEarned, Description)
        SELECT 
            b.ClientId, 
            FLOOR(f.BasePrice * 0.05), -- 5% compensation in points
            CONCAT('Compensation for delayed flight ', f.FlightNumber)
        FROM 
            Bookings b
        JOIN 
            Flights f ON b.FlightId = f.FlightId
        WHERE 
            f.FlightId = flightIdParam
            AND b.STATUS = 'Confirmed';
    END IF;
END //
DELIMITER ;

-- 8. Search Airports using Full-Text Search
DELIMITER //
CREATE PROCEDURE SearchAirports(
    IN searchTerm VARCHAR(100)
)
BEGIN
    SELECT 
        AirportId,
        AirportCode,
        AirportName,
        City,
        Country,
        MATCH(AirportName, City, Country) AGAINST(searchTerm IN NATURAL LANGUAGE MODE) AS Relevance
    FROM 
        Airports
    WHERE 
        MATCH(AirportName, City, Country) AGAINST(searchTerm IN NATURAL LANGUAGE MODE)
    ORDER BY 
        Relevance DESC;
END //
DELIMITER ;

-- TRIGGERS

-- 1. Update seat availability when flight status changes
DELIMITER //
CREATE TRIGGER after_flight_status_update
AFTER UPDATE ON Flights
FOR EACH ROW
BEGIN
    IF NEW.STATUS = 'Cancelled' AND OLD.STATUS != 'Cancelled' THEN
        -- Add to audit log
        INSERT INTO AuditLog (TableName, RecordId, Action, ChangedBy, Details)
        VALUES ('Flights', NEW.FlightId, 'UPDATE', USER(), 
                CONCAT('Flight status changed from ', OLD.STATUS, ' to ', NEW.STATUS));
        
        -- Update all associated bookings to cancelled
        UPDATE Bookings
        SET STATUS = 'Cancelled'
        WHERE FlightId = NEW.FlightId AND STATUS = 'Confirmed';
        
        -- Issue compensation points to affected clients
        INSERT INTO Rewards (ClientId, PointsEarned, Description)
        SELECT 
            b.ClientId, 
            FLOOR(f.BasePrice * 0.2), -- 20% compensation in points
            CONCAT('Compensation for cancelled flight ', f.FlightNumber)
        FROM 
            Bookings b
        JOIN 
            Flights f ON b.FlightId = f.FlightId
        WHERE 
            f.FlightId = NEW.FlightId;
    END IF;
END //
DELIMITER ;

-- 2. Validate booking against available seats
DELIMITER //
CREATE TRIGGER before_booking_insert
BEFORE INSERT ON Bookings
FOR EACH ROW
BEGIN
    DECLARE availableSeats INT;
    
    -- Get available seats for this flight
    SELECT (p.AvailableSeats - COUNT(CASE WHEN b.STATUS = 'Confirmed' THEN 1 ELSE NULL END))
    INTO availableSeats
    FROM Flights f
    JOIN Plane p ON f.PlaneId = p.PlaneId
    LEFT JOIN Bookings b ON f.FlightId = b.FlightId AND b.STATUS = 'Confirmed'
    WHERE f.FlightId = NEW.FlightId
    GROUP BY p.AvailableSeats;
    
    -- Check if there are available seats
    IF availableSeats <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No available seats on this flight';
    END IF;
    
    -- Check if the seat is already taken
    IF EXISTS (
        SELECT 1 FROM Bookings 
        WHERE FlightId = NEW.FlightId 
        AND SeatNumber = NEW.SeatNumber 
        AND STATUS = 'Confirmed'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The selected seat is already taken';
    END IF;
END //
DELIMITER ;

-- 3. After booking insert trigger (for logging and reward points)
DELIMITER //
CREATE TRIGGER after_booking_insert
AFTER INSERT ON Bookings
FOR EACH ROW
BEGIN
    -- Log the booking
    INSERT INTO AuditLog (TableName, RecordId, Action, ChangedBy, Details)
    VALUES ('Bookings', NEW.BookingId, 'INSERT', USER(), 
            CONCAT('New booking created for flight ID ', NEW.FlightId));
            
    -- Add entry to audit log
    INSERT INTO AuditLog (TableName, RecordId, Action, ChangedBy, Details)
    SELECT 'Rewards', LAST_INSERT_ID(), 'INSERT', USER(), 
           CONCAT('Reward points added for booking ID ', NEW.BookingId)
    FROM Flights f
    WHERE f.FlightId = NEW.FlightId;
END //
DELIMITER ;

-- 4. After booking cancellation trigger
DELIMITER //
CREATE TRIGGER after_booking_update
AFTER UPDATE ON Bookings
FOR EACH ROW
BEGIN
    IF NEW.STATUS = 'Cancelled' AND OLD.STATUS = 'Confirmed' THEN
        -- Log the cancellation
        INSERT INTO AuditLog (TableName, RecordId, Action, ChangedBy, Details)
        VALUES ('Bookings', NEW.BookingId, 'UPDATE', USER(), 'Booking cancelled');
    END IF;
END //
DELIMITER ;

-- 5. Client deletion protection trigger
DELIMITER //
CREATE TRIGGER before_client_delete
BEFORE DELETE ON Clients
FOR EACH ROW
BEGIN
    DECLARE bookingCount INT;
    
    -- Check if client has bookings
    SELECT COUNT(*) INTO bookingCount
    FROM Bookings
    WHERE ClientId = OLD.ClientId AND STATUS = 'Confirmed';
    
    IF bookingCount > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Cannot delete client with active bookings';
    END IF;
    
    -- Log the deletion
    INSERT INTO AuditLog (TableName, RecordId, Action, ChangedBy, Details)
    VALUES ('Clients', OLD.ClientId, 'DELETE', USER(), 
            CONCAT('Client ', OLD.ClientsName, ' deleted'));
END //
DELIMITER ;

-- Sample Data Insertion

-- Insert sample airports
INSERT INTO Airports (AirportCode, AirportName, City, Country) VALUES
('JFK', 'John F. Kennedy International Airport', 'New York', 'USA'),
('LAX', 'Los Angeles International Airport', 'Los Angeles', 'USA'),
('ORD', 'O\'Hare International Airport', 'Chicago', 'USA'),
('ATL', 'Hartsfield-Jackson Atlanta International Airport', 'Atlanta', 'USA'),
('LHR', 'Heathrow Airport', 'London', 'UK'),
('CDG', 'Charles de Gaulle Airport', 'Paris', 'France'),
('DXB', 'Dubai International Airport', 'Dubai', 'UAE'),
('SYD', 'Sydney Airport', 'Sydney', 'Australia');

-- Insert sample planes
INSERT INTO Plane (PlaneModel, AvailableSeats) VALUES
('Boeing 737-800', 180),
('Airbus A320', 150),
('Boeing 777-300ER', 350),
('Airbus A380', 525),
('Boeing 787-9', 290);

-- Insert sample clients
INSERT INTO Clients (ClientsName, Email, DateofSubscription) VALUES
('John Smith', 'john.smith@example.com', '2022-01-15'),
('Emily Johnson', 'emily.j@example.com', '2022-02-20'),
('Michael Brown', 'michael.brown@example.com', '2022-03-10'),
('Sarah Williams', 'sarah.w@example.com', '2022-04-05'),
('David Miller', 'david.miller@example.com', '2022-05-12');

-- Insert sample flights
INSERT INTO Flights (FlightNumber, PlaneId, OriginAirportId, DestinationAirportId, DepartureDay, ArrivalDay, BasePrice, STATUS) VALUES
('FL001', 1, 1, 2, '2023-06-15 08:00:00', '2023-06-15 11:30:00', 350.00, 'Scheduled'),
('FL002', 2, 3, 4, '2023-06-16 09:30:00', '2023-06-16 11:45:00', 280.50, 'Scheduled'),
('FL003', 3, 5, 6, '2023-06-17 14:00:00', '2023-06-17 17:15:00', 420.75, 'Scheduled'),
('FL004', 4, 7, 8, '2023-06-18 22:00:00', '2023-06-19 10:30:00', 850.00, 'Scheduled'),
('FL005', 5, 2, 1, '2023-06-19 16:45:00', '2023-06-19 20:15:00', 360.25, 'Scheduled'),
('FL006', 1, 4, 3, '2023-06-20 07:30:00', '2023-06-20 09:45:00', 295.00, 'Scheduled'),
('FL007', 2, 6, 5, '2023-06-21 11:20:00', '2023-06-21 14:50:00', 400.00, 'Scheduled'),
('FL008', 3, 8, 7, '2023-06-22 23:45:00', '2023-06-23 12:15:00', 880.50, 'Scheduled');

-- Insert sample bookings
INSERT INTO Bookings (ClientId, FlightId, SeatNumber) VALUES
(1, 1, '12A'),
(2, 1, '14B'),
(3, 2, '5C'),
(4, 3, '20D'),
(5, 4, '30E'),
(1, 5, '8A'),
(2, 6, '10B'),
(3, 7, '15C');

-- INSERT SAMPLE PASSENGER DETAILS
UPDATE Bookings 
SET PassengerDetails = JSON_OBJECT(
    'firstName', 'John',
    'lastName', 'Smith',
    'passportNumber', 'AB1234567',
    'nationality', 'USA',
    'dateOfBirth', '1985-03-15',
    'specialRequirements', JSON_ARRAY('Vegetarian meal', 'Window seat')
)
WHERE BookingId = 1;

UPDATE Bookings 
SET PassengerDetails = JSON_OBJECT(
    'firstName', 'Emily',
    'lastName', 'Johnson',
    'passportNumber', 'CD7654321',
    'nationality', 'UK',
    'dateOfBirth', '1990-07-22',
    'specialRequirements', JSON_ARRAY('Wheelchair assistance')
)
WHERE BookingId = 2;

-- TEST SCRIPTS

-- NOTE: Uncomment and run these sections one at a time to test various features

-- Test #1: Testing the airport search functionality
/*
CALL SearchAirports('New York');
CALL SearchAirports('London');
CALL SearchAirports('International');
*/

-- Test #2: Testing flight availability search
/*
CALL GetAvailableFlights('JFK', 'LAX', '2023-06-15');
CALL GetAvailableFlights('LHR', 'CDG', '2023-06-17');
*/

-- Test #3: Testing booking a flight
/*
SET @new_booking_id = 0;
CALL BookFlight(4, 1, '16C', @new_booking_id);
SELECT @new_booking_id as 'New Booking ID';
*/

-- Test #4: Testing passenger details update
/*
CALL UpdatePassengerDetails(3, JSON_OBJECT(
    'firstName', 'Michael',
    'lastName', 'Brown',
    'passportNumber', 'EF9876543',
    'nationality', 'Canada',
    'dateOfBirth', '1982-11-30',
    'specialRequirements', JSON_ARRAY('Aisle seat', 'Lactose-free meal')
));
SELECT BookingId, JSON_EXTRACT(PassengerDetails, '$.firstName') as FirstName, 
       JSON_EXTRACT(PassengerDetails, '$.specialRequirements') as SpecialRequirements 
FROM Bookings 
WHERE BookingId = 3;
*/

-- Test #5: Testing flight delay procedure
/*
SELECT FlightId, FlightNumber, DepartureDay, ArrivalDay, STATUS FROM Flights WHERE FlightId = 3;
CALL DelayFlight(3, 120);
SELECT FlightId, FlightNumber, DepartureDay, ArrivalDay, STATUS FROM Flights WHERE FlightId = 3;
SELECT * FROM AuditLog WHERE TableName = 'Flights' AND Action = 'DELAY';
SELECT * FROM Rewards WHERE Description LIKE '%Compensation for delayed flight%';
*/

-- Test #6: Testing booking cancellation
/*
SET @cancel_success = FALSE;
CALL CancelBooking(1, @cancel_success);
SELECT @cancel_success as 'Cancellation Success';
SELECT * FROM Bookings WHERE BookingId = 1;
SELECT * FROM AuditLog WHERE TableName = 'Bookings' AND Action = 'UPDATE';
*/

-- Test #7: Testing reward points redemption
/*
SET @discount_amount = 0;
SET @remaining_points = 0;
CALL RedeemPointsForDiscount(2, 50, 2, @discount_amount, @remaining_points);
SELECT @discount_amount as 'Discount Amount', @remaining_points as 'Remaining Points';
SELECT * FROM Rewards WHERE Description LIKE '%Points redeemed for discount%';
*/

-- Test #8: Viewing flight statistics
/*
SELECT * FROM FlightStatistics;
*/

-- Test #9: Extracting specific JSON data
/*
SELECT 
    b.BookingId,
    f.FlightNumber,
    JSON_EXTRACT(b.PassengerDetails, '$.firstName') as FirstName,
    JSON_EXTRACT(b.PassengerDetails, '$.lastName') as LastName,
    JSON_EXTRACT(b.PassengerDetails, '$.passportNumber') as Passport,
    b.SeatNumber
FROM 
    Bookings b
JOIN 
    Flights f ON b.FlightId = f.FlightId
WHERE 
    b.PassengerDetails IS NOT NULL;
*/

-- Test #10: Viewing client reward points
/*
SELECT * FROM ClientRewardPoints;
*/


