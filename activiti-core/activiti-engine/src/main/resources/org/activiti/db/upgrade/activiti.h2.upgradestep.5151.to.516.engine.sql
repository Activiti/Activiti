alter table ACT_RU_TASK
	add FORM_KEY_ varchar(255);
	
alter table ACT_RU_EXECUTION
	add NAME_ varchar(255);
	
create table ACT_EVT_LOG (
    LOG_NR_ bigint auto_increment,
    TYPE_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    TIME_STAMP_ timestamp not null,
    USER_ID_ varchar(255),
    DATA_ longvarbinary,
    LOCK_OWNER_ varchar(255),
    LOCK_TIME_ timestamp,
    IS_PROCESSED_ bit default 0
);  	
	
	
update ACT_GE_PROPERTY set VALUE_ = '5.16' where NAME_ = 'schema.version';
