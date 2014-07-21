alter table ACT_RU_TASK
	add FORM_KEY_ varchar(255);
	
Call Sysproc.admin_cmd ('REORG TABLE ACT_RU_TASK');
	
alter table ACT_RU_EXECUTION
	add NAME_ varchar(255);

Call Sysproc.admin_cmd ('REORG TABLE ACT_RU_EXECUTION');
	
create table ACT_EVT_LOG (
    LOG_NR_ bigint not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    TYPE_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    TIME_STAMP_ timestamp not null,
    USER_ID_ varchar(255),
    DATA_ BLOB,
    LOCK_OWNER_ varchar(255),
    LOCK_TIME_ timestamp,
    IS_PROCESSED_ integer default 0,
    primary key (LOG_NR_)
);
    	
	
update ACT_GE_PROPERTY set VALUE_ = '5.16' where NAME_ = 'schema.version';
