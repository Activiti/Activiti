-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: liquibase/activiti-history-sql-5.12.xml
-- Ran at: 2/7/13 4:46 PM
-- Against: alfresco@jdbc:sqlserver://172.30.40.192:1433;authenticationScheme=nativeAuthentication;xopenStates=false;sendTimeAsDatetime=true;trustServerCertificate=false;sendStringParametersAsUnicode=true;selectMethod=direct;responseBuffering=adaptive;packetSize=8000;multiSubnetFailover=false;loginTimeout=15;lockTimeout=-1;lastUpdateCount=true;encrypt=false;disableStatementPooling=true;databaseName=activiti;applicationName=Microsoft JDBC Driver for SQL Server;applicationIntent=readwrite;
-- Liquibase version: 2.0.3
-- *********************************************************************

-- Lock Database
-- Changeset liquibase/activiti-history-5.12.xml::2 Activiti 5.12 Update ACT_HI_TASKINST::trademakers::(Checksum: 3:319900036855ef42da46e29d4d01c05f)
ALTER TABLE [dbo].[ACT_HI_TASKINST] ADD [CLAIM_TIME_] datetime
GO


-- Changeset liquibase/activiti-history-5.12.xml::3 Activiti 5.12 Update ACT_HI_ACTINST assignee column size::fheremans::(Checksum: 3:7fce6782a51bfd36d2232adb3d386d14)
ALTER TABLE [dbo].[ACT_RU_TASK] ALTER COLUMN [OWNER_] NVARCHAR(255)
GO


-- Release Database Lock
-- Release Database Lock
