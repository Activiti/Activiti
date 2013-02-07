-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: liquibase/activiti-identity-master.xml
-- Ran at: 07/02/13 15:23
-- Against: SA@jdbc:h2:tcp://localhost/activiti
-- Liquibase version: 2.0.3
-- *********************************************************************

-- Lock Database
-- Changeset liquibase/activiti-identity-5.7.xml::1 ACT_ID_GROUP::trademakers::(Checksum: 3:4c40eec5a6a2abb9742c20a844fa8138)
CREATE TABLE ACT_ID_GROUP (ID_ VARCHAR(64) NOT NULL, REV_ integer, NAME_ VARCHAR(255), TYPE_ VARCHAR(255), CONSTRAINT PK_ACT_ID_GROUP PRIMARY KEY (ID_));


-- Changeset liquibase/activiti-identity-5.7.xml::2 ACT_ID_MEMBERSHIP::trademakers::(Checksum: 3:5bb3528593b2b1cee09d48da843dfb68)
CREATE TABLE ACT_ID_MEMBERSHIP (USER_ID_ VARCHAR(64) NOT NULL, GROUP_ID_ VARCHAR(64) NOT NULL);


-- Changeset liquibase/activiti-identity-5.7.xml::2.1 ACT_ID_MEMBERSHIP::trademakers::(Checksum: 3:47476712c18a3f34a48e8f5b449c44d2)
ALTER TABLE ACT_ID_MEMBERSHIP ADD PRIMARY KEY (USER_ID_, GROUP_ID_);


-- Changeset liquibase/activiti-identity-5.7.xml::3 ACT_ID_USER::trademakers::(Checksum: 3:115f22651f83dd85aeaea3b0e10b608b)
CREATE TABLE ACT_ID_USER (ID_ VARCHAR(64) NOT NULL, REV_ integer, FIRST_ VARCHAR(255), LAST_ VARCHAR(255), EMAIL_ VARCHAR(255), PWD_ VARCHAR(255), PICTURE_ID_ VARCHAR(64), CONSTRAINT PK_ACT_ID_USER PRIMARY KEY (ID_));


-- Changeset liquibase/activiti-identity-5.7.xml::4 Activiti 5.4 Create ACT_ID_INFO::trademakers::(Checksum: 3:7fa9dc3f228d207d1ba6c36318d5f7d4)
CREATE TABLE ACT_ID_INFO (ID_ VARCHAR(64) NOT NULL, REV_ integer, USER_ID_ VARCHAR(64), TYPE_ VARCHAR(64), KEY_ VARCHAR(255), VALUE_ VARCHAR(255), PASSWORD_ LONGVARBINARY, PARENT_ID_ VARCHAR(255), CONSTRAINT PK_ACT_ID_INFO PRIMARY KEY (ID_));


-- Changeset liquibase/activiti-identity-5.7.xml::50 ACT_FK_MEMB_GROUP::trademakers::(Checksum: 3:7ae6a2ecb54b8a8d816b56c053c1e355)
ALTER TABLE ACT_ID_MEMBERSHIP ADD CONSTRAINT ACT_FK_MEMB_GROUP FOREIGN KEY (GROUP_ID_) REFERENCES ACT_ID_GROUP (ID_);


-- Changeset liquibase/activiti-identity-5.7.xml::51 ACT_FK_MEMB_USER::trademakers::(Checksum: 3:70550c10ebba2294eea7dbe20766ae95)
ALTER TABLE ACT_ID_MEMBERSHIP ADD CONSTRAINT ACT_FK_MEMB_USER FOREIGN KEY (USER_ID_) REFERENCES ACT_ID_USER (ID_);


-- Release Database Lock
-- Release Database Lock
