/*
 * EAD - Set 02
 * Database script for Microsoft SQL Server
 */

USE master;
GO

IF DB_ID(N'EAD_Set02') IS NULL
BEGIN
    CREATE DATABASE EAD_Set02;
END;
GO

USE EAD_Set02;
GO

IF OBJECT_ID(N'dbo.accounts', N'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.accounts;
END;
GO

CREATE TABLE dbo.accounts
(
    AccId      INT IDENTITY(1,1) NOT NULL,
    Username   VARCHAR(10) NOT NULL,
    [Password] VARCHAR(20) NOT NULL,
    [Role]     VARCHAR(10) NOT NULL CONSTRAINT DF_accounts_Role DEFAULT ('admin'),
    [Image]    VARCHAR(200) NOT NULL,

    CONSTRAINT PK_accounts PRIMARY KEY (AccId),
    CONSTRAINT UQ_accounts_Username UNIQUE (Username),
    CONSTRAINT CK_accounts_Role CHECK ([Role] IN ('admin', 'user'))
);
GO

INSERT INTO dbo.accounts (Username, [Password], [Role], [Image])
VALUES
    ('admin', '123456', 'admin', 'images/admin.jpg'),
    ('john', '123456', 'user', 'images/john.jpg'),
    ('mary', '123456', 'user', 'images/mary.jpg'),
    ('peter', '123456', 'user', 'images/peter.jpg'),
    ('sarah', '123456', 'user', 'images/sarah.jpg');
GO

SELECT AccId, Username, [Password], [Role], [Image]
FROM dbo.accounts
ORDER BY AccId;
GO
