drop index ACT_RU_EXECUTION.ACT_IDX_EXEC_BUSKEY;
drop index ACT_RU_TASK.ACT_IDX_TASK_CREATE;
drop index ACT_RU_IDENTITYLINK.ACT_IDX_IDENT_LNK_USER;
drop index ACT_RU_IDENTITYLINK.ACT_IDX_IDENT_LNK_GROUP;
drop index ACT_RU_VARIABLE.ACT_IDX_VARIABLE_TASK_ID;
drop index ACT_PROCDEF_INFO.ACT_IDX_INFO_PROCDEF;

alter table ACT_GE_BYTEARRAY
drop constraint ACT_FK_BYTEARR_DEPL;

alter table ACT_RU_EXECUTION
drop constraint ACT_FK_EXE_PROCINST;

alter table ACT_RU_EXECUTION
drop constraint ACT_FK_EXE_PARENT;

alter table ACT_RU_EXECUTION
drop constraint ACT_FK_EXE_SUPER;

alter table ACT_RU_EXECUTION
drop constraint ACT_FK_EXE_PROCDEF;

alter table ACT_RU_IDENTITYLINK
drop constraint ACT_FK_TSKASS_TASK;

alter table ACT_RU_IDENTITYLINK
drop constraint ACT_FK_ATHRZ_PROCEDEF;

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
drop constraint ACT_FK_JOB_EXECUTION;

alter table ACT_RU_JOB
drop constraint ACT_FK_JOB_PROCESS_INSTANCE;

alter table ACT_RU_JOB
drop constraint ACT_FK_JOB_PROC_DEF;

alter table ACT_RU_JOB
drop constraint ACT_FK_JOB_EXCEPTION;

alter table ACT_RU_TIMER_JOB
drop constraint ACT_FK_TIMER_JOB_EXECUTION;

alter table ACT_RU_TIMER_JOB
drop constraint ACT_FK_TIMER_JOB_PROCESS_INSTANCE;

alter table ACT_RU_TIMER_JOB
drop constraint ACT_FK_TIMER_JOB_PROC_DEF;

alter table ACT_RU_TIMER_JOB
drop constraint ACT_FK_TIMER_JOB_EXCEPTION;

alter table ACT_RU_SUSPENDED_JOB
drop constraint ACT_FK_SUSPENDED_JOB_EXECUTION;

alter table ACT_RU_SUSPENDED_JOB
drop constraint ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE;

alter table ACT_RU_SUSPENDED_JOB
drop constraint ACT_FK_SUSPENDED_JOB_PROC_DEF;

alter table ACT_RU_SUSPENDED_JOB
drop constraint ACT_FK_SUSPENDED_JOB_EXCEPTION;

alter table ACT_RU_DEADLETTER_JOB
drop constraint ACT_FK_DEADLETTER_JOB_EXECUTION;

alter table ACT_RU_DEADLETTER_JOB
drop constraint ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE;

alter table ACT_RU_DEADLETTER_JOB
drop constraint ACT_FK_DEADLETTER_JOB_PROC_DEF;

alter table ACT_RU_DEADLETTER_JOB
drop constraint ACT_FK_DEADLETTER_JOB_EXCEPTION;

alter table ACT_RU_EVENT_SUBSCR
drop constraint ACT_FK_EVENT_EXEC;

alter table ACT_RE_MODEL
drop constraint ACT_FK_MODEL_SOURCE;

alter table ACT_RE_MODEL
drop constraint ACT_FK_MODEL_SOURCE_EXTRA;

alter table ACT_RE_MODEL
drop constraint ACT_FK_MODEL_DEPLOYMENT;

alter table ACT_PROCDEF_INFO
drop constraint ACT_FK_INFO_JSON_BA;

alter table ACT_PROCDEF_INFO
drop constraint ACT_FK_INFO_PROCDEF;

alter table ACT_RU_INTEGRATION
drop constraint ACT_FK_INT_EXECUTION;

alter table ACT_RU_INTEGRATION
drop constraint ACT_FK_INT_PROC_INST;

alter table ACT_RU_INTEGRATION
drop constraint ACT_FK_INT_PROC_DEF;

drop index ACT_RU_IDENTITYLINK.ACT_IDX_ATHRZ_PROCEDEF;
drop index ACT_RU_EVENT_SUBSCR.ACT_IDX_EVENT_SUBSCR_CONFIG_;

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
