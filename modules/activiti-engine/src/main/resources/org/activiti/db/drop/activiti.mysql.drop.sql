alter table ACT_GE_BYTEARRAY 
    drop FOREIGN KEY FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop FOREIGN KEY FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION 
    drop FOREIGN KEY FK_EXE_PARENT;

alter table ACT_RU_EXECUTION 
    drop FOREIGN KEY FK_EXE_SUPER;
    
alter table ACT_ID_MEMBERSHIP 
    drop FOREIGN KEY FK_MEMB_GROUP;
    
alter table ACT_ID_MEMBERSHIP 
    drop FOREIGN KEY FK_MEMB_USER;
    
alter table ACT_RU_IDENTITY_LINK
    drop FOREIGN KEY FK_TSKASS_TASK;

alter table ACT_RU_TASK
	drop FOREIGN KEY FK_TASK_EXEC;

alter table ACT_RU_TASK
	drop FOREIGN KEY FK_TASK_PROCINST;
	
alter table ACT_RU_TASK
	drop FOREIGN KEY FK_TASK_PROCDEF;
    
alter table ACT_RU_VARIABLE
    drop FOREIGN KEY FK_VAR_EXE;
    
alter table ACT_RU_VARIABLE
    drop FOREIGN KEY FK_VAR_TASK;

alter table ACT_RU_VARIABLE
	drop FOREIGN KEY FK_VAR_PROCINST;    

alter table ACT_RU_VARIABLE
    drop FOREIGN KEY FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop FOREIGN KEY FK_JOB_EXCEPTION;
    
 drop table if exists ACT_GE_PROPERTY;
 drop table if exists ACT_RU_VARIABLE;
 drop table if exists ACT_GE_BYTEARRAY;
 drop table if exists ACT_RE_DEPLOYMENT;
 drop table if exists ACT_RU_IDENTITY_LINK;
 drop table if exists ACT_RU_TASK;
 drop table if exists ACT_RE_PROC_DEF;
 drop table if exists ACT_RU_EXECUTION;
 drop table if exists ACT_ID_MEMBERSHIP;
 drop table if exists ACT_ID_GROUP;
 drop table if exists ACT_ID_USER;
 drop table if exists ACT_RU_JOB;
 drop table if exists ACT_HI_PROC_INST;
 drop table if exists ACT_HI_ACT_INST;
 drop table if exists ACT_HI_DETAIL;