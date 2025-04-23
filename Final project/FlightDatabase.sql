DROP DATABASE IF EXISTS FlightService;

CREATE DATABASE FlightService;

USE FlightService;

CREATE TABLE Clients(
ClientId INT NOT NULL,
ClientsName VARCHAR(100) NOT NULL,
email VARCHAR(100) NOT NULL,
DateofSubscription DATE NOT NULL,
RewardPoint INT DEFAULT 0,
CONSTRAINT CheckEmail CHECK(email LIKE "%@%.%"),
PRIMARY KEY(ClientId)
);

CREATE TABLE Plane(
PlaneId INT NOT NULL,
AvailableSeats INT NOT NULL,
PRIMARY KEY(PlaneId)
);

CREATE TABLE Flights(
FlightId INT NOT NULL,
PlaneId INT NOT NULL,
AvailableSeats INT NOT NULL,
DepartureDay DATETIME NOT NULL,
ArrivalDay DATETIME NOT NULL,
STATUS ENUM ("Scheduled", "Delayed", "Cancelled") DEFAULT "Scheduled",
FOREIGN KEY(PlaneId) REFERENCES Plane(PlaneId),
CONSTRAINT CheckDate CHECK (ArrivalDay > DepartureDay)
);

CREATE TABLE Bookings(
BookingId INT NOT NULL,
ClientId INT NOT NULL,
FlightId INT NOT NULL,
BookingDates TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
SeatNumber VARCHAR(10),
STATUS ENUM("Confirmed", "Cancelled") DEFAULT "Confirmed",
FOREIGN KEY(ClientId) REFERENCES Clients(ClientId),
FOREIGN KEY(FlightId) REFERENCES Flights(FlightId),
CONSTRAINT chk_seat_format CHECK (seat_number REGEXP '^[0-9]{1,2}[A-F]$')
);

CREATE TABLE Rewards(
RewardId INT NOT NULL,
ClientId INT NOT NULL,
PointsEarned INT NOT NULL,
PointsUsed INT DEFAULT 0,
FOREIGN KEY(ClientId) REFERENCES Clients(ClientId),
CONSTRAINT CheckPoints CHECK (PointsEarned >= 0 AND PointsUsed >= 0)
);


