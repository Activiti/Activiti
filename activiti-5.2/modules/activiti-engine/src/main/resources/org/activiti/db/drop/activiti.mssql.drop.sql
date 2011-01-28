drop index ACT_RU_EXECUTION.ACT_IDX_EXEC_BUSKEY;
drop index ACT_RU_TASK.ACT_IDX_TASK_CREATE;
drop index ACT_RU_IDENTITYLINK.ACT_IDX_IDENT_LNK_USER;
drop index ACT_RU_IDENTITYLINK.ACT_IDX_IDENT_LNK_GROUP;
drop index ACT_HI_PROCINST.ACT_IDX_HI_PRO_INST_END;
drop index ACT_HI_PROCINST.ACT_IDX_HI_PRO_I_BUSKEY;
drop index ACT_HI_ACTINST.ACT_IDX_HI_ACT_INST_START;
drop index ACT_HI_ACTINST.ACT_IDX_HI_ACT_INST_END;
drop index ACT_HI_DETAIL.ACT_IDX_HI_DETAIL_PROC_INST;
drop index ACT_HI_DETAIL.ACT_IDX_HI_DETAIL_ACT_INST;
drop index ACT_HI_DETAIL.ACT_IDX_HI_DETAIL_TIME;
drop index ACT_HI_DETAIL.ACT_IDX_HI_DETAIL_NAME;


alter table ACT_GE_BYTEARRAY 
    drop constraint ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION 
    drop constraint ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION 
    drop constraint ACT_FK_EXE_SUPER;
    
alter table ACT_ID_MEMBERSHIP 
    drop constraint ACT_FK_MEMB_GROUP;
    
alter table ACT_ID_MEMBERSHIP 
    drop constraint ACT_FK_MEMB_USER;
    
alter table ACT_RU_IDENTITYLINK
    drop constraint ACT_FK_TSKASS_TASK;

alter table ACT_RU_TASK
	drop constraint ACT_FK_TASK_EXE;

alter table ACT_RU_TASK
	drop constraint ACT_FK_TASK_PROCINST;
	
alter table ACT_RU_TASK
	drop constraint ACT_FK_TASK_PROCDEF;
    
alter table ACT_RU_VARIABLE
    drop constraint ACT_FK_VAR_EXE;
    
alter table ACT_RU_VARIABLE
	drop constraint ACT_FK_VAR_PROCINST;    

alter table ACT_RU_VARIABLE
    drop constraint ACT_FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop constraint ACT_FK_JOB_EXCEPTION;
    
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_GE_PROPERTY') drop table ACT_GE_PROPERTY;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_VARIABLE') drop table ACT_RU_VARIABLE;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_GE_BYTEARRAY') drop table ACT_GE_BYTEARRAY;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RE_DEPLOYMENT') drop table ACT_RE_DEPLOYMENT;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_IDENTITYLINK') drop table ACT_RU_IDENTITYLINK;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_TASK') drop table ACT_RU_TASK;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RE_PROCDEF') drop table ACT_RE_PROCDEF;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_EXECUTION') drop table ACT_RU_EXECUTION;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_MEMBERSHIP') drop table ACT_ID_MEMBERSHIP;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_GROUP') drop table ACT_ID_GROUP;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_ID_USER') drop table ACT_ID_USER;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_RU_JOB') drop table ACT_RU_JOB;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_HI_PROCINST') drop table ACT_HI_PROCINST;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_HI_ACTINST') drop table ACT_HI_ACTINST;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_HI_TASKINST') drop table ACT_HI_TASKINST;
 if exists (select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'ACT_HI_DETAIL') drop table ACT_HI_DETAIL;
 