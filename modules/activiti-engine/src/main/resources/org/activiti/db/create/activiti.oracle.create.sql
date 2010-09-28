create table ACT_GE_PROPERTY (
    NAME_ nvarchar2(64),
    VALUE_ nvarchar2(300),
    REV_ integer,
    primary key (NAME_)
);

insert into ACT_GE_PROPERTY
values ('schema.version', '5.0.rc1-SNAPSHOT', 1);

insert into ACT_GE_PROPERTY
values ('next.dbid', '1', 1);

create table ACT_GE_BYTEARRAY (
    ID_ nvarchar2(64),
    REV_ integer,
    NAME_ nvarchar2(255),
    DEPLOYMENT_ID_ nvarchar2(64),
    BYTES_ blob,
    primary key (ID_)
);

create table ACT_RE_DEPLOYMENT (
    ID_ nvarchar2(64),
    NAME_ nvarchar2(255),
    DEPLOY_TIME_ timestamp,
    primary key (ID_)
);

create table ACT_RU_EXECUTION (
    ID_ nvarchar2(64),
    REV_ integer,
    PROC_INST_ID_ nvarchar2(64),
    PARENT_ID_ nvarchar2(64),
    PROC_DEF_ID_ nvarchar2(64),
    SUPER_EXEC_ nvarchar2(64),
    ACTIVITY_ID_ nvarchar2(64),
    IS_ACTIVE_ NUMBER(1,0) CHECK (IS_ACTIVE_ IN (1,0)),
    IS_CONCURRENT_ NUMBER(1,0) CHECK (IS_CONCURRENT_ IN (1,0)),
    IS_SCOPE_ NUMBER(1,0) CHECK (IS_SCOPE_ IN (1,0)),
    primary key (ID_)
);

create table ACT_RU_JOB (
    ID_ nvarchar2(64) NOT NULL,
    REV_ integer,
    TYPE_ nvarchar2(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp,
    LOCK_OWNER_ nvarchar2(255),
    EXCLUSIVE_ NUMBER(1,0) CHECK (EXCLUSIVE_ IN (1,0)),
    EXECUTION_ID_ nvarchar2(64),
    PROCESS_INSTANCE_ID_ nvarchar2(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ nvarchar2(64),
    EXCEPTION_MSG_ nvarchar2(255),
    DUEDATE_ timestamp,
    REPEAT_ nvarchar2(255),
    HANDLER_TYPE_ nvarchar2(255),
    HANDLER_CFG_ nvarchar2(255),
    primary key (ID_)
);

create table ACT_ID_GROUP (
    ID_ nvarchar2(64),
    REV_ integer,
    NAME_ nvarchar2(255),
    TYPE_ nvarchar2(255),
    primary key (ID_)
);

create table ACT_ID_MEMBERSHIP (
    USER_ID_ nvarchar2(64),
    GROUP_ID_ nvarchar2(64),
    primary key (USER_ID_, GROUP_ID_)
);

create table ACT_ID_USER (
    ID_ nvarchar2(64),
    REV_ integer,
    FIRST_ nvarchar2(255),
    LAST_ nvarchar2(255),
    EMAIL_ nvarchar2(255),
    PWD_ nvarchar2(255),
    primary key (ID_)
);

create table ACT_RE_PROC_DEF (
    ID_ nvarchar2(64),
    NAME_ nvarchar2(255),
    KEY_ nvarchar2(255),
    VERSION_ integer,
    DEPLOYMENT_ID_ nvarchar2(64),
    RESOURCE_NAME_ nvarchar2(255),
    START_FORM_ nvarchar2(255),
    primary key (ID_)
);

create table ACT_RU_TASK (
    ID_ nvarchar2(64),
    REV_ integer,
    EXECUTION_ID_ nvarchar2(64),
    PROC_INST_ID_ nvarchar2(64),
    PROC_DEF_ID_ nvarchar2(64),
    NAME_ nvarchar2(255),
    DESCRIPTION_ nvarchar2(255),
    FORM_ nvarchar2(255),
    ASSIGNEE_ nvarchar2(64),
    PRIORITY_ integer,
    CREATE_TIME_ timestamp,
    START_DEADLINE_ timestamp,
    COMPLETION_DEADLINE_ timestamp,
    SKIPPABLE_ NUMBER(1,0) CHECK (SKIPPABLE_ IN (1,0)),
    primary key (ID_)
);

create table ACT_RU_IDENTITY_LINK (
    ID_ nvarchar2(64),
    REV_ integer,
    GROUP_ID_ nvarchar2(64),
    TYPE_ nvarchar2(255),
    USER_ID_ nvarchar2(64),
    TASK_ID_ nvarchar2(64),
    primary key (ID_)
);

create table ACT_RU_VARIABLE (
    ID_ nvarchar2(64) not null,
    REV_ integer,
    TYPE_ nvarchar2(255) not null,
    NAME_ nvarchar2(255) not null,
    EXECUTION_ID_ nvarchar2(64),
    PROC_INST_ID_ nvarchar2(64),
    TASK_ID_ nvarchar2(64),
    BYTEARRAY_ID_ nvarchar2(64),
    DATE_ timestamp,
    DOUBLE_ number(*,4),
    LONG_ long,
    TEXT_ nvarchar2(255),
    primary key (ID_)
);

create table ACT_HI_PROC_INST (
    ID_ nvarchar2(64) not null,
    PROC_INST_ID_ nvarchar2(64) not null,
    PROC_DEF_ID_ nvarchar2(64) not null,
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ long,
    END_ACT_ID_ nvarchar2(64),
    primary key (ID_),
    unique (PROC_INST_ID_)
);

create table ACT_HI_ACT_INST (
    ID_ nvarchar2(64) not null,
    PROC_DEF_ID_ nvarchar2(64) not null,
    PROC_INST_ID_ nvarchar2(64) not null,
    EXECUTION_ID_ nvarchar2(64) not null,
    ACT_ID_ nvarchar2(64) not null,
    ACT_NAME_ nvarchar2(255),
    ACT_TYPE_ nvarchar2(255) not null,
    ASSIGNEE_ nvarchar2(64),
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ long,
    primary key (ID_)
);

	alter table ACT_GE_BYTEARRAY
    add constraint FK_BYTEARR_DEPL 
    foreign key (DEPLOYMENT_ID_) 
    references ACT_RE_DEPLOYMENT (ID_);

alter table ACT_RU_EXECUTION
    add constraint FK_EXE_PROCINST 
    foreign key (PROC_INST_ID_) 
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_EXECUTION
    add constraint FK_EXE_PARENT 
    foreign key (PARENT_ID_) 
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_EXECUTION
    add constraint FK_EXE_SUPER 
    foreign key (SUPER_EXEC_) 
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_ID_MEMBERSHIP 
    add constraint FK_MEMB_GROUP 
    foreign key (GROUP_ID_) 
    references ACT_ID_GROUP (ID_);

alter table ACT_ID_MEMBERSHIP 
    add constraint FK_MEMB_USER 
    foreign key (USER_ID_) 
    references ACT_ID_USER (ID_);

alter table ACT_RU_IDENTITY_LINK
    add constraint FK_TSKASS_TASK 
    foreign key (TASK_ID_) 
    references ACT_RU_TASK (ID_);
    
alter table ACT_RU_TASK
    add constraint FK_TASK_EXEC
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_TASK
    add constraint FK_TASK_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_TASK
  add constraint FK_TASK_PROCDEF
  foreign key (PROC_DEF_ID_)
  references ACT_RE_PROC_DEF (ID_);
  
alter table ACT_RU_VARIABLE 
    add constraint FK_VAR_TASK 
    foreign key (TASK_ID_) 
    references ACT_RU_TASK (ID_);

alter table ACT_RU_VARIABLE 
    add constraint FK_VAR_EXE 
    foreign key (EXECUTION_ID_) 
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_VARIABLE
    add constraint FK_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION(ID_);

alter table ACT_RU_VARIABLE 
    add constraint FK_VAR_BYTEARRAY 
    foreign key (BYTEARRAY_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_JOB 
    add constraint FK_JOB_EXCEPTION 
    foreign key (EXCEPTION_STACK_ID_) 
    references ACT_GE_BYTEARRAY (ID_);