-- Flight Database Test Script
-- This file contains all test queries to validate the functionality of the FlightService database
-- Run each section individually to test specific features

USE FlightService;

-- ======================================
-- 1. SCHEMA VERIFICATION TESTS
-- ======================================

-- Show all tables
SHOW TABLES;

-- Verify each table structure
DESCRIBE Clients;
DESCRIBE Airports;
DESCRIBE Plane;
DESCRIBE Flights;
DESCRIBE Bookings;
DESCRIBE Rewards;
DESCRIBE AuditLog;

-- Verify views
SHOW FULL TABLES WHERE TABLE_TYPE LIKE 'VIEW';

-- ======================================
-- 2. BASIC DATA VERIFICATION TESTS
-- ======================================

-- Check airports data
SELECT * FROM Airports;

-- Check flights data
SELECT * FROM Flights;

-- Check clients data
SELECT * FROM Clients;

-- Check bookings data
SELECT * FROM Bookings;

-- ======================================
-- 3. AIRPORT SEARCH TESTS
-- ======================================

-- Test airport search by city
CALL SearchAirports('New York');

-- Test airport search by country
CALL SearchAirports('UK');

-- Test airport search by name
CALL SearchAirports('International');

-- ======================================
-- 4. FLIGHT AVAILABILITY TESTS
-- ======================================

-- Test flight availability search
CALL GetAvailableFlights('JFK', 'LAX', '2023-06-15');
CALL GetAvailableFlights('LHR', 'CDG', '2023-06-17');

-- View flight availability directly
SELECT * FROM FlightAvailability;

-- ======================================
-- 5. BOOKING TESTS
-- ======================================

-- Test booking a new flight
SET @new_booking_id = 0;
CALL BookFlight(4, 1, '16C', @new_booking_id);
SELECT @new_booking_id as 'New Booking ID';

-- Verify the booking was created
SELECT * FROM Bookings WHERE BookingId = @new_booking_id;

-- Verify reward points were added
SELECT * FROM Rewards WHERE Description LIKE '%Points for booking flight%' ORDER BY RewardId DESC LIMIT 1;

-- ======================================
-- 6. PASSENGER DETAILS TESTS
-- ======================================

-- Test updating passenger details
CALL UpdatePassengerDetails(@new_booking_id, JSON_OBJECT(
    'firstName', 'Test',
    'lastName', 'User',
    'passportNumber', 'XY9876543',
    'nationality', 'Australia',
    'dateOfBirth', '1995-05-20',
    'specialRequirements', JSON_ARRAY('Extra legroom', 'Vegan meal')
));

-- Verify passenger details were updated
SELECT BookingId, JSON_EXTRACT(PassengerDetails, '$.firstName') as FirstName, 
       JSON_EXTRACT(PassengerDetails, '$.lastName') as LastName,
       JSON_EXTRACT(PassengerDetails, '$.specialRequirements') as SpecialRequirements 
FROM Bookings 
WHERE BookingId = @new_booking_id;

-- Test JSON extraction
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

-- ======================================
-- 7. FLIGHT DELAY TESTS
-- ======================================

-- Check flight status before delay
SELECT FlightId, FlightNumber, DepartureDay, ArrivalDay, STATUS FROM Flights WHERE FlightId = 3;

-- Test flight delay
CALL DelayFlight(3, 120);

-- Verify flight was delayed
SELECT FlightId, FlightNumber, DepartureDay, ArrivalDay, STATUS FROM Flights WHERE FlightId = 3;

-- Verify audit log entry
SELECT * FROM AuditLog WHERE TableName = 'Flights' AND Action = 'DELAY';

-- Verify compensation points
SELECT * FROM Rewards WHERE Description LIKE '%Compensation for delayed flight%';

-- ======================================
-- 8. BOOKING CANCELLATION TESTS
-- ======================================

-- Test cancellation
SET @cancel_success = FALSE;
CALL CancelBooking(@new_booking_id, @cancel_success);
SELECT @cancel_success as 'Cancellation Success';

-- Verify booking was cancelled
SELECT * FROM Bookings WHERE BookingId = @new_booking_id;

-- Verify audit log
SELECT * FROM AuditLog WHERE TableName = 'Bookings' AND Action = 'UPDATE';

-- ======================================
-- 9. REWARDS AND POINTS TESTS
-- ======================================

-- Test rewards redemption
SET @discount_amount = 0;
SET @remaining_points = 0;

-- First check current points
SELECT * FROM ClientRewardPoints WHERE ClientId = 2;

-- Redeem points
CALL RedeemPointsForDiscount(2, 20, 2, @discount_amount, @remaining_points);
SELECT @discount_amount as 'Discount Amount', @remaining_points as 'Remaining Points';

-- Verify points were used
SELECT * FROM Rewards WHERE Description LIKE '%Points redeemed for discount%' ORDER BY RewardId DESC LIMIT 1;

-- Verify updated points
SELECT * FROM ClientRewardPoints WHERE ClientId = 2;

-- ======================================
-- 10. ANALYTICS TESTS
-- ======================================

-- View flight statistics
SELECT * FROM FlightStatistics;

-- View client flight history
CALL GetClientBookings(1);

-- ======================================
-- 11. ERROR HANDLING TESTS
-- ======================================

-- Test booking duplicate seat (should fail)
SET @error_booking_id = 0;
-- This should generate an error since seat 12A on flight 1 is already taken
CALL BookFlight(3, 1, '12A', @error_booking_id);

-- Test cancellation too close to departure (modify date to test)
SET @cancel_fail = FALSE;
-- This may not fail in testing since we're using future dates
CALL CancelBooking(4, @cancel_fail);

-- Test redeeming too many points
SET @disc = 0;
SET @rem = 0;
-- This should fail as the client likely doesn't have 10000 points
CALL RedeemPointsForDiscount(1, 10000, 5, @disc, @rem); 