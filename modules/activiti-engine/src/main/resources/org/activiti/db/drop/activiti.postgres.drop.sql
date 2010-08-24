alter table ACT_BYTEARRAY 
    drop constraint FK_BYTEARR_DEPL;

alter table ACT_EXECUTION
    drop constraint FK_EXE_PROCINST;

alter table ACT_EXECUTION 
    drop constraint FK_EXE_PARENT;

alter table ACT_EXECUTION 
    drop constraint FK_EXE_SUPER;
    
alter table ACT_ID_MEMBERSHIP 
    drop constraint FK_MEMB_GROUP;
    
alter table ACT_ID_MEMBERSHIP 
    drop constraint FK_MEMB_USER;
    
alter table ACT_TASKINVOLVEMENT
    drop constraint FK_TSKASS_TASK;

alter table ACT_TASK
	drop constraint FK_TASK_EXEC;

alter table ACT_TASK
	drop constraint FK_TASK_PROCINST;
	
alter table ACT_TASK
	drop constraint FK_TASK_PROCDEF;
    
alter table ACT_VARIABLE
    drop constraint FK_VAR_EXE;
    
alter table ACT_VARIABLE
    drop constraint FK_VAR_TASK;

alter table ACT_VARIABLE
    drop constraint FK_VAR_PROCINST;
    
alter table ACT_VARIABLE
    drop constraint FK_VAR_BYTEARRAY;
    
drop table ACT_PROPERTY;
drop table ACT_BYTEARRAY;
drop table ACT_DEPLOYMENT;
drop table ACT_EXECUTION;
drop table ACT_ID_GROUP;
drop table ACT_ID_MEMBERSHIP;
drop table ACT_ID_USER;
drop table ACT_JOB;
drop table ACT_PROCESSDEFINITION;
drop table ACT_TASK;
drop table ACT_TASKINVOLVEMENT;
drop table ACT_VARIABLE;
drop table ACT_H_PROCINST;
drop table ACT_H_ACTINST;