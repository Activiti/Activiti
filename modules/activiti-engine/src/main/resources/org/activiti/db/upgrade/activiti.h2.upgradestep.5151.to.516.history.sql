alter table ACT_HI_PROCINST
	add NAME_ varchar(255);
	
create table ACT_HI_EVT_LOG (
    LOG_NR_ bigint auto_increment,
    TYPE_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    TIME_STAMP_ timestamp not null,
    USER_ID_ varchar(255),
    DATA_ longvarchar,
    LOCK_OWNER_ varchar(255),
    LOCK_TIME_ timestamp,
    IS_PROCESSED_ bit default 0,
    primary key (LOG_NR_)
);	