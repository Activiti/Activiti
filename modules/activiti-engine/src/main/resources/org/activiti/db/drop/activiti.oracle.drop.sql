alter table ACT_GE_BYTEARRAY 
    drop CONSTRAINT FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop CONSTRAINT FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION 
    drop CONSTRAINT FK_EXE_PARENT;

alter table ACT_RU_EXECUTION 
    drop CONSTRAINT FK_EXE_SUPER;
    
alter table ACT_ID_MEMBERSHIP 
    drop CONSTRAINT FK_MEMB_GROUP;
    
alter table ACT_ID_MEMBERSHIP 
    drop CONSTRAINT FK_MEMB_USER;
    
alter table ACT_RU_IDENTITY_LINK
    drop CONSTRAINT FK_TSKASS_TASK;

alter table ACT_RU_TASK
	drop CONSTRAINT FK_TASK_EXEC;

alter table ACT_RU_TASK
	drop CONSTRAINT FK_TASK_PROCINST;
	
alter table ACT_RU_TASK
	drop CONSTRAINT FK_TASK_PROCDEF;
    
alter table ACT_RU_VARIABLE
    drop CONSTRAINT FK_VAR_EXE;
    
alter table ACT_RU_VARIABLE
    drop CONSTRAINT FK_VAR_TASK;

alter table ACT_RU_VARIABLE
	drop CONSTRAINT FK_VAR_PROCINST;

alter table ACT_RU_VARIABLE
    drop CONSTRAINT FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop CONSTRAINT FK_JOB_EXCEPTION;

drop table  ACT_GE_PROPERTY;
drop table  ACT_RU_VARIABLE;
drop table  ACT_GE_BYTEARRAY;
drop table  ACT_RE_DEPLOYMENT;
drop table  ACT_RU_IDENTITY_LINK;
drop table  ACT_RU_TASK;
drop table  ACT_RE_PROC_DEF;
drop table  ACT_RU_EXECUTION;
drop table  ACT_ID_MEMBERSHIP;
drop table  ACT_ID_GROUP;
drop table  ACT_ID_USER;
drop table  ACT_RU_JOB;
drop table  ACT_HI_PROC_INST;
drop table  ACT_HI_ACT_INST;
drop table  ACT_HI_VAR_UPDATE;