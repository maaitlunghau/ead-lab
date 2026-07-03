-- EAD Exam - Set 13
-- Database script for Microsoft SQL Server

USE master;
GO

IF DB_ID(N'FlightDB') IS NULL
BEGIN
    CREATE DATABASE FlightDB;
END;
GO

USE FlightDB;
GO

-- Drop tables if exist (order matters due to FK)
IF OBJECT_ID(N'dbo.Booking', N'U') IS NOT NULL DROP TABLE dbo.Booking;
IF OBJECT_ID(N'dbo.Flight',  N'U') IS NOT NULL DROP TABLE dbo.Flight;
GO

CREATE TABLE dbo.Flight
(
    FlightNo       VARCHAR(10)   NOT NULL,
    Departure      NVARCHAR(50)  NOT NULL,
    Destination    NVARCHAR(50)  NOT NULL,
    DepartureTime  DATETIME      NOT NULL,
    AvailableSeats INT           NOT NULL,
    Price          DECIMAL(15,2) NOT NULL,

    CONSTRAINT PK_Flight PRIMARY KEY (FlightNo)
);
GO

CREATE TABLE dbo.Booking
(
    BookingId    INT           IDENTITY(1,1) NOT NULL,
    FlightNo     VARCHAR(10)   NOT NULL,
    CustomerName NVARCHAR(100) NOT NULL,
    SeatsBooked  INT           NOT NULL,
    TotalPrice   DECIMAL(15,2) NOT NULL,
    BookingDate  DATETIME      NOT NULL,

    CONSTRAINT PK_Booking  PRIMARY KEY (BookingId),
    CONSTRAINT FK_Booking_Flight FOREIGN KEY (FlightNo) REFERENCES dbo.Flight(FlightNo)
);
GO

-- Seed: at least 3 flights
INSERT INTO dbo.Flight (FlightNo, Departure, Destination, DepartureTime, AvailableSeats, Price)
VALUES
    ('VN-102', N'Ha Noi', N'Ho Chi Minh', '2026-06-28 06:00:00', 45,  120.00),
    ('VN-214', N'Ha Noi', N'Ho Chi Minh', '2026-06-29 14:30:00', 12,  115.00),
    ('VN-305', N'Ha Noi', N'Da Nang',     '2026-06-28 09:00:00', 30,   95.00),
    ('VN-410', N'Da Nang', N'Ho Chi Minh','2026-06-29 11:00:00', 20,   85.00);
GO

-- Seed: at least 3 bookings
INSERT INTO dbo.Booking (FlightNo, CustomerName, SeatsBooked, TotalPrice, BookingDate)
VALUES
    ('VN-102', N'Nguyen Van A', 2, 240.00, '2026-06-20 10:00:00'),
    ('VN-214', N'Tran Thi B',   1, 115.00, '2026-06-21 08:30:00'),
    ('VN-305', N'Le Van C',     3, 285.00, '2026-06-22 15:00:00');
GO

SELECT * FROM dbo.Flight   ORDER BY FlightNo;
SELECT * FROM dbo.Booking  ORDER BY BookingId;
GO
