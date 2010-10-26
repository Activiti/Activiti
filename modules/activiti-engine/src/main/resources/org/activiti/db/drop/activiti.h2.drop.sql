drop index ACT_IDX_EXEC_BUSKEY;
drop index ACT_IDX_TASK_CREATE;
drop index ACT_IDX_IDENT_LNK_USER;
drop index ACT_IDX_IDENT_LNK_GROUP;
drop index ACT_IDX_HI_PRO_INST_END;
drop index ACT_IDX_HI_PRO_I_BUSKEY;
drop index ACT_IDX_HI_ACT_INST_START;
drop index ACT_IDX_HI_ACT_INST_END;
drop index ACT_IDX_HI_DETAIL_PROC_INST;
drop index ACT_IDX_HI_DETAIL_ACT_INST;
drop index ACT_IDX_HI_DETAIL_TIME;
drop index ACT_IDX_HI_DETAIL_NAME;

alter table ACT_GE_BYTEARRAY 
    drop constraint FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop constraint FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION 
    drop constraint FK_EXE_PARENT;

alter table ACT_RU_EXECUTION 
    drop constraint FK_EXE_SUPER;
    
alter table ACT_ID_MEMBERSHIP 
    drop constraint FK_MEMB_GROUP;
    
alter table ACT_RU_EXECUTION
    drop constraint UNIQ_RU_BUS_KEY;
    
alter table ACT_HI_PROCINST
    drop constraint UNIQ_HI_BUS_KEY;
    
alter table ACT_ID_MEMBERSHIP 
    drop constraint FK_MEMB_USER;
    
alter table ACT_RU_IDENTITYLINK
    drop constraint FK_TSKASS_TASK;
 
alter table ACT_RU_TASK
	drop constraint FK_TASK_EXEC;

alter table ACT_RU_TASK
	drop constraint FK_TASK_PROCINST;
	
alter table ACT_RU_TASK
	drop constraint FK_TASK_PROCDEF;
	
alter table ACT_RU_VARIABLE
    drop constraint FK_VAR_EXE;
    
alter table ACT_RU_VARIABLE
    drop constraint FK_VAR_PROCINST;
    
alter table ACT_RU_VARIABLE
    drop constraint FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop constraint FK_JOB_EXCEPTION;
    
drop table ACT_GE_PROPERTY if exists;
drop table ACT_GE_BYTEARRAY if exists;
drop table ACT_RE_DEPLOYMENT if exists;
drop table ACT_RU_EXECUTION if exists;
drop table ACT_ID_GROUP if exists;
drop table ACT_ID_MEMBERSHIP if exists;
drop table ACT_ID_USER if exists;
drop table ACT_RU_JOB if exists;
drop table ACT_RE_PROCDEF if exists;
drop table ACT_RU_TASK if exists;
drop table ACT_RU_IDENTITYLINK if exists;
drop table ACT_RU_VARIABLE if exists;
drop table ACT_HI_PROCINST if exists;
drop table ACT_HI_ACTINST if exists;
drop table ACT_HI_DETAIL if exists;
