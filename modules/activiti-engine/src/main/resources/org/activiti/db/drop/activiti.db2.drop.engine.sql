drop index ACT_IDX_EXEC_BUSKEY;
drop index ACT_IDX_TASK_CREATE;
drop index ACT_IDX_IDENT_LNK_USER;
drop index ACT_IDX_IDENT_LNK_GROUP;
drop index ACT_IDX_VARIABLE_TASK_ID;

alter table ACT_GE_BYTEARRAY 
    drop foreign key ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop foreign key ACT_FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION 
    drop foreign key ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION 
    drop foreign key ACT_FK_EXE_SUPER;
    
alter table ACT_RU_EXECUTION 
    drop foreign key ACT_FK_EXE_PROCDEF;

alter table ACT_RU_IDENTITYLINK
    drop foreign key ACT_FK_TSKASS_TASK;

alter table ACT_RU_IDENTITYLINK
    drop foreign key ACT_FK_ATHRZ_PROCEDEF;

alter table ACT_RU_TASK
	drop foreign key ACT_FK_TASK_EXE;

alter table ACT_RU_TASK
	drop foreign key ACT_FK_TASK_PROCINST;
	
alter table ACT_RU_TASK
	drop foreign key ACT_FK_TASK_PROCDEF;
    
alter table ACT_RU_VARIABLE
    drop foreign key ACT_FK_VAR_EXE;
    
alter table ACT_RU_VARIABLE
	drop foreign key ACT_FK_VAR_PROCINST;    

alter table ACT_RU_VARIABLE
    drop foreign key ACT_FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop foreign key ACT_FK_JOB_EXCEPTION;
    
alter table ACT_RU_EVENT_SUBSCR
    drop foreign key ACT_FK_EVENT_EXEC;

alter table ACT_RE_MODEL 
    drop foreign key ACT_FK_MODEL_SOURCE;

alter table ACT_RE_MODEL 
    drop foreign key ACT_FK_MODEL_SOURCE_EXTRA; 
    
alter table ACT_RE_MODEL 
    drop foreign key ACT_FK_MODEL_DEPLOYMENT; 

drop index ACT_IDX_EVENT_SUBSCR_CONFIG_;
drop index ACT_IDX_ATHRZ_PROCEDEF;
    
drop table ACT_GE_PROPERTY;
drop table ACT_GE_BYTEARRAY;
drop table ACT_RE_DEPLOYMENT;
drop table ACT_RE_MODEL;
drop table ACT_RE_PROCDEF;
drop table ACT_RU_VARIABLE;
drop table ACT_RU_IDENTITYLINK;
drop table ACT_RU_TASK;
drop table ACT_RU_EXECUTION;
drop table ACT_RU_JOB;
drop table ACT_RU_EVENT_SUBSCR;
