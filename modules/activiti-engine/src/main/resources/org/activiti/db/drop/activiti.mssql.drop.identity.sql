<<<<<<< HEAD
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_MEMBERSHIP') alter table ACT_ID_MEMBERSHIP drop constraint ACT_FK_MEMB_GROUP;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_MEMBERSHIP') alter table ACT_ID_MEMBERSHIP drop constraint ACT_FK_MEMB_USER;
    
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_INFO') drop table ACT_ID_INFO;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_MEMBERSHIP') drop table ACT_ID_MEMBERSHIP;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_GROUP') drop table ACT_ID_GROUP;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_USER') drop table ACT_ID_USER;
=======
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_MEMBERSHIP') alter table ACT_ID_MEMBERSHIP drop constraint ACT_FK_MEMB_GROUP;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_MEMBERSHIP') alter table ACT_ID_MEMBERSHIP drop constraint ACT_FK_MEMB_USER;
    
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_INFO') drop table ACT_ID_INFO;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_MEMBERSHIP') drop table ACT_ID_MEMBERSHIP;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_GROUP') drop table ACT_ID_GROUP;
if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_USER') drop table ACT_ID_USER;
>>>>>>> upstream/master
