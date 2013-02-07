-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: liquibase/activiti-identity-master.xml
-- Ran at: 2/7/13 4:45 PM
-- Against: alfresco@jdbc:sqlserver://172.30.40.192:1433;authenticationScheme=nativeAuthentication;xopenStates=false;sendTimeAsDatetime=true;trustServerCertificate=false;sendStringParametersAsUnicode=true;selectMethod=direct;responseBuffering=adaptive;packetSize=8000;multiSubnetFailover=false;loginTimeout=15;lockTimeout=-1;lastUpdateCount=true;encrypt=false;disableStatementPooling=true;databaseName=activiti;applicationName=Microsoft JDBC Driver for SQL Server;applicationIntent=readwrite;
-- Liquibase version: 2.0.3
-- *********************************************************************

-- Lock Database
-- Changeset liquibase/activiti-identity-5.7.xml::1 ACT_ID_GROUP::trademakers::(Checksum: 3:4c40eec5a6a2abb9742c20a844fa8138)
CREATE TABLE [dbo].[ACT_ID_GROUP] ([ID_] NVARCHAR(64) NOT NULL, [REV_] int, [NAME_] NVARCHAR(255), [TYPE_] NVARCHAR(255), CONSTRAINT [PK_ACT_ID_GROUP] PRIMARY KEY ([ID_]))
GO


-- Changeset liquibase/activiti-identity-5.7.xml::2 ACT_ID_MEMBERSHIP::trademakers::(Checksum: 3:5bb3528593b2b1cee09d48da843dfb68)
CREATE TABLE [dbo].[ACT_ID_MEMBERSHIP] ([USER_ID_] NVARCHAR(64) NOT NULL, [GROUP_ID_] NVARCHAR(64) NOT NULL)
GO


-- Changeset liquibase/activiti-identity-5.7.xml::2.1 ACT_ID_MEMBERSHIP::trademakers::(Checksum: 3:47476712c18a3f34a48e8f5b449c44d2)
ALTER TABLE [dbo].[ACT_ID_MEMBERSHIP] ADD PRIMARY KEY ([USER_ID_], [GROUP_ID_])
GO


-- Changeset liquibase/activiti-identity-5.7.xml::3 ACT_ID_USER::trademakers::(Checksum: 3:115f22651f83dd85aeaea3b0e10b608b)
CREATE TABLE [dbo].[ACT_ID_USER] ([ID_] NVARCHAR(64) NOT NULL, [REV_] int, [FIRST_] NVARCHAR(255), [LAST_] NVARCHAR(255), [EMAIL_] NVARCHAR(255), [PWD_] NVARCHAR(255), [PICTURE_ID_] NVARCHAR(64), CONSTRAINT [PK_ACT_ID_USER] PRIMARY KEY ([ID_]))
GO


-- Changeset liquibase/activiti-identity-5.7.xml::4 Activiti 5.4 Create ACT_ID_INFO::trademakers::(Checksum: 3:7fa9dc3f228d207d1ba6c36318d5f7d4)
CREATE TABLE [dbo].[ACT_ID_INFO] ([ID_] NVARCHAR(64) NOT NULL, [REV_] int, [USER_ID_] NVARCHAR(64), [TYPE_] NVARCHAR(64), [KEY_] NVARCHAR(255), [VALUE_] NVARCHAR(255), [PASSWORD_] image, [PARENT_ID_] NVARCHAR(255), CONSTRAINT [PK_ACT_ID_INFO] PRIMARY KEY ([ID_]))
GO


-- Changeset liquibase/activiti-identity-5.7.xml::50 ACT_FK_MEMB_GROUP::trademakers::(Checksum: 3:7ae6a2ecb54b8a8d816b56c053c1e355)
ALTER TABLE [dbo].[ACT_ID_MEMBERSHIP] ADD CONSTRAINT [ACT_FK_MEMB_GROUP] FOREIGN KEY ([GROUP_ID_]) REFERENCES [dbo].[ACT_ID_GROUP] ([ID_])
GO


-- Changeset liquibase/activiti-identity-5.7.xml::51 ACT_FK_MEMB_USER::trademakers::(Checksum: 3:70550c10ebba2294eea7dbe20766ae95)
ALTER TABLE [dbo].[ACT_ID_MEMBERSHIP] ADD CONSTRAINT [ACT_FK_MEMB_USER] FOREIGN KEY ([USER_ID_]) REFERENCES [dbo].[ACT_ID_USER] ([ID_])
GO


-- Release Database Lock
-- Release Database Lock
