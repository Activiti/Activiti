alter table ACT_ID_MEMBERSHIP 
    drop constraint ACT_FK_MEMB_GROUP;
    
alter table ACT_ID_MEMBERSHIP 
    drop constraint ACT_FK_MEMB_USER;
    
drop table ACT_ID_INFO if exists;
drop table ACT_ID_GROUP if exists;
drop table ACT_ID_MEMBERSHIP if exists;
drop table ACT_ID_USER if exists;
