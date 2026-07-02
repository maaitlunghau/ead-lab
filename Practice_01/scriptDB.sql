USE master;
GO

IF DB_ID(N'EAD_Practice01') IS NULL
BEGIN
    CREATE DATABASE EAD_Practice01;
END;
GO

USE EAD_Practice01;
GO

IF OBJECT_ID(N'dbo.Employee', N'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.Employee;
END;
GO

CREATE TABLE dbo.Employee (
    EmpCode  VARCHAR(10) NOT NULL,
    Password VARCHAR(20) NOT NULL,
    Name     VARCHAR(40) NOT NULL,
    Age      INT         NOT NULL,
    CONSTRAINT PK_Employee PRIMARY KEY (EmpCode),
    CONSTRAINT CK_Employee_Age CHECK (Age >= 18)
);
GO

INSERT INTO dbo.Employee (EmpCode, Password, Name, Age) VALUES
    ('E01', '123456', 'Bill Gates',        60),
    ('E02', '123456', 'Barack Obama',      55),
    ('E03', '123456', 'Rafael Nadal',      30),
    ('E04', '123456', 'Taylor Swift',      36),
    ('E05', '123456', 'Cristiano Ronaldo', 41);
GO
