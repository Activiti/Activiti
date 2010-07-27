alter table ACT_BYTEARRAY 
    drop constraint FK_BYTEARR_DEPL;

alter table ACT_EXECUTION
    drop constraint FK_EXE_PROCINST;

alter table ACT_EXECUTION 
    drop constraint FK_EXE_PARENT;
    
alter table ACT_ID_MEMBERSHIP 
    drop constraint FK_MEMB_GROUP;
    
alter table ACT_ID_MEMBERSHIP 
    drop constraint FK_MEMB_USER;
    
alter table ACT_TASKINVOLVEMENT
    drop constraint FK_TSKASS_TASK;
    
alter table ACT_VARIABLE
    drop constraint FK_VAR_EXE;
    
alter table ACT_VARIABLE
    drop constraint FK_VAR_TASK;
    
alter table ACT_VARIABLE
    drop constraint FK_VAR_BYTEARRAY;
    
drop table ACT_PROPERTY if exists;
drop table ACT_BYTEARRAY if exists;
drop table ACT_DEPLOYMENT if exists;
drop table ACT_EXECUTION if exists;
drop table ACT_ID_GROUP if exists;
drop table ACT_ID_MEMBERSHIP if exists;
drop table ACT_ID_USER if exists;
drop table ACT_JOB if exists;
drop table ACT_PROCESSDEFINITION if exists;
drop table ACT_TASK if exists;
drop table ACT_TASKINVOLVEMENT if exists;
drop table ACT_VARIABLE if exists;
drop table ACT_H_PROCINST if exists;
drop table ACT_H_ACTINST if exists;
