create table ACT_GE_PROPERTY (
    NAME_ varchar(64) not null,
    VALUE_ varchar(300),
    REV_ integer,
    primary key (NAME_)
);

insert into ACT_GE_PROPERTY
values ('schema.version', '5.2-SNAPSHOT', 1);

insert into ACT_GE_PROPERTY
values ('schema.history', 'create(5.2-SNAPSHOT)', 1);

insert into ACT_GE_PROPERTY
values ('next.dbid', '1', 1);

create table ACT_GE_BYTEARRAY (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    DEPLOYMENT_ID_ varchar(64),
    BYTES_ BLOB,
    primary key (ID_)
);

create table ACT_RE_DEPLOYMENT (
    ID_ varchar(64) not null,
    NAME_ varchar(255),
    DEPLOY_TIME_ timestamp,
    primary key (ID_)
);

create table ACT_RU_EXECUTION (
    ID_ varchar(64) not null,
    REV_ integer,
    PROC_INST_ID_ varchar(64),
    BUSINESS_KEY_ varchar(255),
    PARENT_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    SUPER_EXEC_ varchar(64),
    ACT_ID_ varchar(255),
    IS_ACTIVE_ smallint check(IS_ACTIVE_ in (1,0)),
    IS_CONCURRENT_ smallint check(IS_CONCURRENT_ in (1,0)),
    IS_SCOPE_ smallint check(IS_SCOPE_ in (1,0)),
	UNI_BUSINESS_KEY varchar (255)  not null  generated always as (case when "BUSINESS_KEY_" is null then "ID_" else "BUSINESS_KEY_" end),
	UNI_PROC_DEF_ID varchar (64)  not null  generated always as (case when "PROC_DEF_ID_" is null then "ID_" else "PROC_DEF_ID_" end),
    primary key (ID_)
);

create table ACT_RU_JOB (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    LOCK_EXP_TIME_ timestamp,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ smallint check(EXCLUSIVE_ in (1,0)),
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(255),
    DUEDATE_ timestamp null,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(255),
    primary key (ID_)
);

create table ACT_ID_GROUP (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    TYPE_ varchar(255),
    primary key (ID_)
);

create table ACT_ID_MEMBERSHIP (
    USER_ID_ varchar(64) not null,
    GROUP_ID_ varchar(64) not null,
    primary key (USER_ID_, GROUP_ID_)
);

create table ACT_ID_USER (
    ID_ varchar(64) not null,
    REV_ integer,
    FIRST_ varchar(255),
    LAST_ varchar(255),
    EMAIL_ varchar(255),
    PWD_ varchar(255),
    primary key (ID_)
);

create table ACT_RE_PROCDEF (
    ID_ varchar(64) not null,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255),
    VERSION_ integer,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(255),
    DGRM_RESOURCE_NAME_ varchar(255),
    HAS_START_FORM_KEY_ smallint check(HAS_START_FORM_KEY_ in (1,0)),
    primary key (ID_)
);

create table ACT_RU_TASK (
    ID_ varchar(64) not null,
    REV_ integer,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    NAME_ varchar(255),
    DESCRIPTION_ varchar(255),
    TASK_DEF_KEY_ varchar(255),
    ASSIGNEE_ varchar(64),
    PRIORITY_ integer,
    CREATE_TIME_ timestamp,
    primary key (ID_)
);

create table ACT_RU_IDENTITYLINK (
    ID_ varchar(64) not null,
    REV_ integer,
    GROUP_ID_ varchar(64),
    TYPE_ varchar(255),
    USER_ID_ varchar(64),
    TASK_ID_ varchar(64),
    primary key (ID_)
);

create table ACT_RU_VARIABLE (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    NAME_ varchar(255) not null,
    EXECUTION_ID_ varchar(64),
	PROC_INST_ID_ varchar(64),
    TASK_ID_ varchar(64),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(255),
    TEXT2_ varchar(255),
    primary key (ID_)
);

create table ACT_HI_PROCINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    BUSINESS_KEY_ varchar(255),
    PROC_DEF_ID_ varchar(64) not null,
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    START_USER_ID_ varchar(255),
    START_ACT_ID_ varchar(255),
    END_ACT_ID_ varchar(255),
	UNI_BUSINESS_KEY varchar (255)  not null  generated always as (case when "BUSINESS_KEY_" is null then "ID_" else "BUSINESS_KEY_" end),
	UNI_PROC_DEF_ID varchar (64)  not null  generated always as (case when "PROC_DEF_ID_" is null then "ID_" else "PROC_DEF_ID_" end),
    primary key (ID_),
    unique (PROC_INST_ID_)
);

create table ACT_HI_ACTINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    EXECUTION_ID_ varchar(64) not null,
    ACT_ID_ varchar(255) not null,
    ACT_NAME_ varchar(255),
    ACT_TYPE_ varchar(255) not null,
    ASSIGNEE_ varchar(64),
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    primary key (ID_)
);

create table ACT_HI_TASKINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64),
    TASK_DEF_KEY_ varchar(255),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    NAME_ varchar(255),
    DESCRIPTION_ varchar(255),
    ASSIGNEE_ varchar(64),
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    DELETE_REASON_ varchar(255),
    primary key (ID_)
);

create table ACT_HI_DETAIL (
    ID_ varchar(64) not null,
    TYPE_ varchar(255) not null,
    PROC_INST_ID_ varchar(64) not null,
    EXECUTION_ID_ varchar(64) not null,
    TASK_ID_ varchar(64),
    ACT_INST_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(255),
    REV_ integer,
    TIME_ timestamp not null,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(255),
    TEXT2_ varchar(255),
    primary key (ID_)
);

create unique index ACT_UNIQ_RU_BUS_KEY on ACT_RU_EXECUTION(UNI_PROC_DEF_ID, UNI_BUSINESS_KEY);
create unique index ACT_UNIQ_HI_BUS_KEY on ACT_HI_PROCINST(UNI_PROC_DEF_ID, UNI_BUSINESS_KEY);
create index ACT_IDX_EXEC_BUSKEY on ACT_RU_EXECUTION(BUSINESS_KEY_);
create index ACT_IDX_TASK_CREATE on ACT_RU_TASK(CREATE_TIME_);
create index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK(USER_ID_);
create index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK(GROUP_ID_);
create index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST(END_TIME_);
create index ACT_IDX_HI_PRO_I_BUSKEY on ACT_HI_PROCINST(BUSINESS_KEY_);
create index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST(START_TIME_);
create index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST(END_TIME_);
create index ACT_IDX_HI_DETAIL_PROC_INST on ACT_HI_DETAIL(PROC_INST_ID_);
create index ACT_IDX_HI_DETAIL_ACT_INST on ACT_HI_DETAIL(ACT_INST_ID_);
create index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL(TIME_);
create index ACT_IDX_HI_DETAIL_NAME on ACT_HI_DETAIL(NAME_);

alter table ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL 
    foreign key (DEPLOYMENT_ID_) 
    references ACT_RE_DEPLOYMENT (ID_);

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCINST 
    foreign key (PROC_INST_ID_) 
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PARENT 
    foreign key (PARENT_ID_) 
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_SUPER 
    foreign key (SUPER_EXEC_) 
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_ID_MEMBERSHIP 
    add constraint ACT_FK_MEMB_GROUP 
    foreign key (GROUP_ID_) 
    references ACT_ID_GROUP (ID_);

alter table ACT_ID_MEMBERSHIP 
    add constraint ACT_FK_MEMB_USER 
    foreign key (USER_ID_) 
    references ACT_ID_USER (ID_);

alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK 
    foreign key (TASK_ID_) 
    references ACT_RU_TASK (ID_);
    
alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_EXE
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_TASK
  add constraint ACT_FK_TASK_PROCDEF
  foreign key (PROC_DEF_ID_)
  references ACT_RE_PROCDEF (ID_);
  
alter table ACT_RU_VARIABLE 
    add constraint ACT_FK_VAR_EXE 
    foreign key (EXECUTION_ID_) 
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION(ID_);

alter table ACT_RU_VARIABLE 
    add constraint ACT_FK_VAR_BYTEARRAY 
    foreign key (BYTEARRAY_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RU_JOB 
    add constraint ACT_FK_JOB_EXCEPTION 
    foreign key (EXCEPTION_STACK_ID_) 
    references ACT_GE_BYTEARRAY (ID_);
