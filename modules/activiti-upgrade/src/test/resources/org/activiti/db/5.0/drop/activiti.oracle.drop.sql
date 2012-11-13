drop index ACT_IDX_BYTEAR_DEPL ;
drop index ACT_IDX_EXE_PROCINST ;
drop index ACT_IDX_EXE_PARENT ;
drop index ACT_IDX_EXE_SUPER;
drop index ACT_IDX_MEMB_GROUP;
drop index ACT_IDX_MEMB_USER;
drop index ACT_IDX_TSKASS_TASK;
drop index ACT_IDX_TASK_EXEC;
drop index ACT_IDX_TASK_PROCINST;
drop index ACT_IDX_TASK_PROCDEF;
drop index ACT_IDX_VAR_EXE;
drop index ACT_IDX_VAR_PROCINST;
drop index ACT_IDX_VAR_BYTEARRAY;
drop index ACT_IDX_JOB_EXCEPTION;

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
    drop CONSTRAINT ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
    drop CONSTRAINT ACT_FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION 
    drop CONSTRAINT ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION 
    drop CONSTRAINT ACT_FK_EXE_SUPER;
    
alter table ACT_ID_MEMBERSHIP 
    drop CONSTRAINT ACT_FK_MEMB_GROUP;
    
alter table ACT_ID_MEMBERSHIP 
    drop CONSTRAINT ACT_FK_MEMB_USER;
    
alter table ACT_RU_IDENTITYLINK
    drop CONSTRAINT ACT_FK_TSKASS_TASK;

alter table ACT_RU_TASK
	drop CONSTRAINT ACT_FK_TASK_EXE;

alter table ACT_RU_TASK
	drop CONSTRAINT ACT_FK_TASK_PROCINST;
	
alter table ACT_RU_TASK
	drop CONSTRAINT ACT_FK_TASK_PROCDEF;
    
alter table ACT_RU_VARIABLE
    drop CONSTRAINT ACT_FK_VAR_EXE;
    
alter table ACT_RU_VARIABLE
	drop CONSTRAINT ACT_FK_VAR_PROCINST;

alter table ACT_RU_VARIABLE
    drop CONSTRAINT ACT_FK_VAR_BYTEARRAY;

alter table ACT_RU_JOB
    drop CONSTRAINT ACT_FK_JOB_EXCEPTION;

drop table  ACT_GE_PROPERTY;
drop table  ACT_RU_VARIABLE;
drop table  ACT_GE_BYTEARRAY;
drop table  ACT_RE_DEPLOYMENT;
drop table  ACT_RU_IDENTITYLINK;
drop table  ACT_RU_TASK;
drop table  ACT_RE_PROCDEF;
drop table  ACT_RU_EXECUTION;
drop table  ACT_ID_MEMBERSHIP;
drop table  ACT_ID_GROUP;
drop table  ACT_ID_USER;
drop table  ACT_RU_JOB;
drop table  ACT_HI_PROCINST;
drop table  ACT_HI_ACTINST;
drop table  ACT_HI_DETAIL;