alter table ACT_HI_PROCINST
	add NAME_ nvarchar(255);
	
	
create table ACT_HI_EVT_LOG (
    LOG_NR_ numeric(19,0) IDENTITY(1,1) PRIMARY KEY,
    TYPE_ nvarchar(64),
    PROC_DEF_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    TIME_STAMP_ datetime not null,
    USER_ID_ nvarchar(255),
    DATA_ text,
    LOCK_OWNER_ nvarchar(255),
    LOCK_TIME_ datetime null,
    IS_PROCESSED_ tinyint default 0,
    primary key (LOG_NR_)
);	