-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: liquibase/activiti-history-sql-5.12.xml
-- Ran at: 2/7/13 4:46 PM
-- Against: ACTIVITIDB@jdbc:oracle:thin:@localhost:1521:xe
-- Liquibase version: 2.0.3
-- *********************************************************************

-- Lock Database
-- Changeset liquibase/activiti-history-5.12.xml::2 Activiti 5.12 Update ACT_HI_TASKINST::trademakers::(Checksum: 3:319900036855ef42da46e29d4d01c05f)
ALTER TABLE ACT_HI_TASKINST ADD CLAIM_TIME_ TIMESTAMP(6);


-- Changeset liquibase/activiti-history-5.12.xml::3 Activiti 5.12 Update ACT_HI_ACTINST assignee column size::fheremans::(Checksum: 3:7fce6782a51bfd36d2232adb3d386d14)
ALTER TABLE ACT_RU_TASK MODIFY OWNER_ NVARCHAR2(255);


-- Release Database Lock
-- Release Database Lock
