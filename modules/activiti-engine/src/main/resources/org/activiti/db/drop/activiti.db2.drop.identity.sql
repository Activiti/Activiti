alter table ACT_ID_MEMBERSHIP 
    drop foreign key ACT_FK_MEMB_GROUP;
    
alter table ACT_ID_MEMBERSHIP 
    drop foreign key ACT_FK_MEMB_USER;
    
drop table ACT_ID_MEMBERSHIP;
drop table ACT_ID_GROUP;
drop table ACT_ID_USER;
drop table ACT_HI_PROCINST;
drop table ACT_HI_ACTINST;
drop table ACT_HI_TASKINST;
drop table ACT_HI_DETAIL;
