-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: liquibase/activiti-identity-master.xml
-- Ran at: 2/7/13 4:45 PM
-- Against: activiti@localhost@jdbc:mysql://localhost:3306/activiti?autoReconnect=true
-- Liquibase version: 2.0.3
-- *********************************************************************

-- Lock Database
-- Changeset liquibase/activiti-identity-5.7.xml::1 ACT_ID_GROUP::trademakers::(Checksum: 3:49011e8ffce79db6a9f1cb90a0836493)
CREATE TABLE `ACT_ID_GROUP` (`ID_` VARCHAR(64) NOT NULL, `REV_` integer, `NAME_` VARCHAR(255), `TYPE_` VARCHAR(255), CONSTRAINT `PK_ACT_ID_GROUP` PRIMARY KEY (`ID_`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;


-- Changeset liquibase/activiti-identity-5.7.xml::2 ACT_ID_MEMBERSHIP::trademakers::(Checksum: 3:f2e0362afebe2a393afeff8fa94c9b16)
CREATE TABLE `ACT_ID_MEMBERSHIP` (`USER_ID_` VARCHAR(64) NOT NULL, `GROUP_ID_` VARCHAR(64) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;


-- Changeset liquibase/activiti-identity-5.7.xml::2.1 ACT_ID_MEMBERSHIP::trademakers::(Checksum: 3:47476712c18a3f34a48e8f5b449c44d2)
ALTER TABLE `ACT_ID_MEMBERSHIP` ADD PRIMARY KEY (`USER_ID_`, `GROUP_ID_`);


-- Changeset liquibase/activiti-identity-5.7.xml::3 ACT_ID_USER::trademakers::(Checksum: 3:7b2f2e0cd1284c4bb6b30314cbb0217e)
CREATE TABLE `ACT_ID_USER` (`ID_` VARCHAR(64) NOT NULL, `REV_` integer, `FIRST_` VARCHAR(255), `LAST_` VARCHAR(255), `EMAIL_` VARCHAR(255), `PWD_` VARCHAR(255), `PICTURE_ID_` VARCHAR(64), CONSTRAINT `PK_ACT_ID_USER` PRIMARY KEY (`ID_`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;


-- Changeset liquibase/activiti-identity-5.7.xml::4 Activiti 5.4 Create ACT_ID_INFO::trademakers::(Checksum: 3:dcd38e6ccd667753f80b35183f140a21)
CREATE TABLE `ACT_ID_INFO` (`ID_` VARCHAR(64) NOT NULL, `REV_` integer, `USER_ID_` VARCHAR(64), `TYPE_` VARCHAR(64), `KEY_` VARCHAR(255), `VALUE_` VARCHAR(255), `PASSWORD_` LONGBLOB, `PARENT_ID_` VARCHAR(255), CONSTRAINT `PK_ACT_ID_INFO` PRIMARY KEY (`ID_`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;


-- Changeset liquibase/activiti-identity-5.7.xml::50 ACT_FK_MEMB_GROUP::trademakers::(Checksum: 3:7ae6a2ecb54b8a8d816b56c053c1e355)
ALTER TABLE `ACT_ID_MEMBERSHIP` ADD CONSTRAINT `ACT_FK_MEMB_GROUP` FOREIGN KEY (`GROUP_ID_`) REFERENCES `ACT_ID_GROUP` (`ID_`);


-- Changeset liquibase/activiti-identity-5.7.xml::51 ACT_FK_MEMB_USER::trademakers::(Checksum: 3:70550c10ebba2294eea7dbe20766ae95)
ALTER TABLE `ACT_ID_MEMBERSHIP` ADD CONSTRAINT `ACT_FK_MEMB_USER` FOREIGN KEY (`USER_ID_`) REFERENCES `ACT_ID_USER` (`ID_`);


-- Release Database Lock
-- Release Database Lock
