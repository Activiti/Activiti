drop index ACT_IDX_EXEC_BUSKEY on ACT_RU_EXECUTION;
drop index ACT_IDX_TASK_CREATE on ACT_RU_TASK;
drop index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK;
drop index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK;
drop index ACT_IDX_VARIABLE_TASK_ID on ACT_RU_VARIABLE;
drop index ACT_IDX_INFO_PROCDEF on ACT_PROCDEF_INFO;

alter table ACT_GE_BYTEARRAY 
    drop FOREIGN KEY ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop FOREIGN KEY ACT_FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION 
    drop FOREIGN KEY ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION 
    drop FOREIGN KEY ACT_FK_EXE_SUPER;
    
alter table ACT_RU_EXECUTION 
    drop FOREIGN KEY ACT_FK_EXE_PROCDEF;

alter table ACT_RU_IDENTITYLINK
    drop FOREIGN KEY ACT_FK_TSKASS_TASK;

alter table ACT_RU_IDENTITYLINK
    drop FOREIGN KEY ACT_FK_ATHRZ_PROCEDEF;
    
alter table ACT_RU_TASK
	drop FOREIGN KEY ACT_FK_TASK_EXE;

alter table ACT_RU_TASK
	drop FOREIGN KEY ACT_FK_TASK_PROCINST;
	
alter table ACT_RU_TASK
	drop FOREIGN KEY ACT_FK_TASK_PROCDEF;
    
alter table ACT_RU_VARIABLE
    drop FOREIGN KEY ACT_FK_VAR_EXE;
    
alter table ACT_RU_VARIABLE
	drop FOREIGN KEY ACT_FK_VAR_PROCINST;    

alter table ACT_RU_VARIABLE
    drop FOREIGN KEY ACT_FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop FOREIGN KEY ACT_FK_JOB_EXECUTION;
    
alter table ACT_RU_JOB 
    drop foreign key ACT_FK_JOB_PROCESS_INSTANCE;
    
alter table ACT_RU_JOB 
    drop foreign key ACT_FK_JOB_PROC_DEF;

alter table ACT_RU_JOB 
    drop foreign key ACT_FK_JOB_EXCEPTION;
    
alter table ACT_RU_TIMER_JOB 
    drop foreign key ACT_FK_TIMER_JOB_EXECUTION;
    
alter table ACT_RU_TIMER_JOB 
    drop foreign key ACT_FK_TIMER_JOB_PROCESS_INSTANCE;
    
alter table ACT_RU_TIMER_JOB 
    drop foreign key ACT_FK_TIMER_JOB_PROC_DEF;
    
alter table ACT_RU_TIMER_JOB 
    drop foreign key ACT_FK_TIMER_JOB_EXCEPTION;
    
alter table ACT_RU_SUSPENDED_JOB 
    drop foreign key ACT_FK_SUSPENDED_JOB_EXECUTION;
    
alter table ACT_RU_SUSPENDED_JOB 
    drop foreign key ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE;
    
alter table ACT_RU_SUSPENDED_JOB 
    drop foreign key ACT_FK_SUSPENDED_JOB_PROC_DEF;
    
alter table ACT_RU_SUSPENDED_JOB 
    drop foreign key ACT_FK_SUSPENDED_JOB_EXCEPTION;
    
alter table ACT_RU_DEADLETTER_JOB 
    drop foreign key ACT_FK_DEADLETTER_JOB_EXECUTION;
    
alter table ACT_RU_DEADLETTER_JOB 
    drop foreign key ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE;
    
alter table ACT_RU_DEADLETTER_JOB 
    drop foreign key ACT_FK_DEADLETTER_JOB_PROC_DEF;
    
alter table ACT_RU_DEADLETTER_JOB 
    drop foreign key ACT_FK_DEADLETTER_JOB_EXCEPTION;
    
alter table ACT_RU_EVENT_SUBSCR
    drop FOREIGN KEY ACT_FK_EVENT_EXEC;

alter table ACT_RE_MODEL 
    drop FOREIGN KEY ACT_FK_MODEL_SOURCE;

alter table ACT_RE_MODEL 
    drop FOREIGN KEY ACT_FK_MODEL_SOURCE_EXTRA;
    
alter table ACT_RE_MODEL 
    drop FOREIGN KEY ACT_FK_MODEL_DEPLOYMENT;

alter table ACT_PROCDEF_INFO 
    drop FOREIGN KEY ACT_FK_INFO_JSON_BA;
    
alter table ACT_PROCDEF_INFO 
    drop FOREIGN KEY ACT_FK_INFO_PROCDEF;

alter table ACT_RU_INTEGRATION
    drop foreign key ACT_FK_INT_EXECUTION;

alter table ACT_RU_INTEGRATION
    drop foreign key ACT_FK_INT_PROC_INST;

alter table ACT_RU_INTEGRATION
    drop foreign key ACT_FK_INT_PROC_DEF;

drop index ACT_IDX_ATHRZ_PROCEDEF on ACT_RU_IDENTITYLINK;
drop index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR;
    
drop table if exists ACT_GE_PROPERTY;
drop table if exists ACT_RU_VARIABLE;
drop table if exists ACT_GE_BYTEARRAY;
drop table if exists ACT_RE_DEPLOYMENT;
drop table if exists ACT_RE_MODEL;
drop table if exists ACT_RU_IDENTITYLINK;
drop table if exists ACT_RU_TASK;
drop table if exists ACT_RE_PROCDEF;
drop table if exists ACT_RU_EXECUTION;
drop table if exists ACT_RU_JOB;
drop table if exists ACT_RU_TIMER_JOB;
drop table if exists ACT_RU_SUSPENDED_JOB;
drop table if exists ACT_RU_DEADLETTER_JOB;
drop table if exists ACT_RU_EVENT_SUBSCR;
drop table if exists ACT_EVT_LOG;
drop table if exists ACT_PROCDEF_INFO;
drop table if exists ACT_RU_INTEGRATION;