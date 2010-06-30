alter table ACT_BYTEARRAY 
    drop FOREIGN KEY FK_BYTEARR_DEPL;

alter table ACT_EXECUTION
    drop FOREIGN KEY FK_EXE_PROCINST;

alter table ACT_EXECUTION 
    drop FOREIGN KEY FK_EXE_PARENT;
    
alter table ACT_ID_MEMBERSHIP 
    drop FOREIGN KEY FK_MEMB_GROUP;
    
alter table ACT_ID_MEMBERSHIP 
    drop FOREIGN KEY FK_MEMB_USER;
    
alter table ACT_TASKINVOLVEMENT
    drop FOREIGN KEY FK_TSKASS_TASK;
    
alter table ACT_VARIABLE
    drop FOREIGN KEY FK_VAR_EXE;
    
alter table ACT_VARIABLE
    drop FOREIGN KEY FK_VAR_TASK;
    
alter table ACT_VARIABLE
    drop FOREIGN KEY FK_VAR_BYTEARRAY;
    
drop table if exists ACT_PROPERTY;
drop table if exists ACT_BYTEARRAY;
drop table if exists ACT_DEPLOYMENT;
drop table if exists ACT_ID_GROUP;
drop table if exists ACT_ID_MEMBERSHIP;
drop table if exists ACT_ID_USER;
drop table if exists ACT_JOB;
drop table if exists ACT_TASK;
drop table if exists ACT_TASKINVOLVEMENT;
drop table if exists ACT_VARIABLE;
drop table if exists ACT_EXECUTION;
drop table if exists ACT_PROCESSDEFINITION;