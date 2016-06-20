create table ACT_RU_TIMER_JOB (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    LOCK_EXP_TIME_ timestamp,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ smallint check(EXCLUSIVE_ in (1,0)),
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ACT_RU_SUSPENDED_JOB (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    EXCLUSIVE_ smallint check(EXCLUSIVE_ in (1,0)),
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ACT_RU_DEADLETTER_JOB (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    EXCLUSIVE_ smallint check(EXCLUSIVE_ in (1,0)),
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

alter table ACT_RU_EXECUTION add column START_TIME_ timestamp;
alter table ACT_RU_EXECUTION add column START_USER_ID_ varchar(255);
alter table ACT_RU_TASK add column CLAIM_TIME_ timestamp;

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.2' where NAME_ = 'schema.version';
