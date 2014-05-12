create table ACT_HI_PROCINST (
    ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64) not null,
    BUSINESS_KEY_ nvarchar(255),
    PROC_DEF_ID_ nvarchar(64) not null,
    START_TIME_ datetime not null,
    END_TIME_ datetime,
    DURATION_ numeric(19,0),
    START_USER_ID_ nvarchar(255),
    START_ACT_ID_ nvarchar(255),
    END_ACT_ID_ nvarchar(255),
    SUPER_PROCESS_INSTANCE_ID_ nvarchar(64),
    DELETE_REASON_ nvarchar(4000),
    TENANT_ID_ nvarchar(255) default '',
    primary key (ID_),
    unique (PROC_INST_ID_)
);

create table ACT_HI_ACTINST (
    ID_ nvarchar(64) not null,
    PROC_DEF_ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64) not null,
    EXECUTION_ID_ nvarchar(64) not null,
    ACT_ID_ nvarchar(255) not null,
    TASK_ID_ nvarchar(64),
    CALL_PROC_INST_ID_ nvarchar(64),
    ACT_NAME_ nvarchar(255),
    ACT_TYPE_ nvarchar(255) not null,
    ASSIGNEE_ nvarchar(255),
    START_TIME_ datetime not null,
    END_TIME_ datetime,
    DURATION_ numeric(19,0),
    TENANT_ID_ nvarchar(255) default '',
    primary key (ID_)
);

create table ACT_HI_TASKINST (
    ID_ nvarchar(64) not null,
    PROC_DEF_ID_ nvarchar(64),
    TASK_DEF_KEY_ nvarchar(255),
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    NAME_ nvarchar(255),
    PARENT_TASK_ID_ nvarchar(64),
    DESCRIPTION_ nvarchar(4000),
    OWNER_ nvarchar(255),
    ASSIGNEE_ nvarchar(255),
    START_TIME_ datetime not null,
    CLAIM_TIME_ datetime,
    END_TIME_ datetime,
    DURATION_ numeric(19,0),
    DELETE_REASON_ nvarchar(4000),
    PRIORITY_ int,
    DUE_DATE_ datetime,
    FORM_KEY_ nvarchar(255),
    CATEGORY_ nvarchar(255),
    TENANT_ID_ nvarchar(255) default '',
    primary key (ID_)
);

create table ACT_HI_VARINST (
    ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    NAME_ nvarchar(255) not null,
    VAR_TYPE_ nvarchar(100),
    REV_ int,
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    CREATE_TIME_ datetime,
    LAST_UPDATED_TIME_ datetime,
    primary key (ID_)
);

create table ACT_HI_DETAIL (
    ID_ nvarchar(64) not null,
    TYPE_ nvarchar(255) not null,
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    ACT_INST_ID_ nvarchar(64),
    NAME_ nvarchar(255) not null,
    VAR_TYPE_ nvarchar(255),
    REV_ int,
    TIME_ datetime not null,
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    primary key (ID_)
);

create table ACT_HI_COMMENT (
    ID_ nvarchar(64) not null,
    TYPE_ nvarchar(255),
    TIME_ datetime not null,
    USER_ID_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    ACTION_ nvarchar(255),
    MESSAGE_ nvarchar(4000),
    FULL_MSG_ varbinary(max),
    primary key (ID_)
);

create table ACT_HI_ATTACHMENT (
    ID_ nvarchar(64) not null,
    REV_ integer,
    USER_ID_ nvarchar(255),
    NAME_ nvarchar(255),
    DESCRIPTION_ nvarchar(4000),
    TYPE_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    URL_ nvarchar(4000),
    CONTENT_ID_ nvarchar(64),
    primary key (ID_)
);

create table ACT_HI_IDENTITYLINK (
    ID_ nvarchar(64),
    GROUP_ID_ nvarchar(255),
    TYPE_ nvarchar(255),
    USER_ID_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    primary key (ID_)
);

create index ACT_IDX_HI_PRO_INST_END on ACT_HI_PROCINST(END_TIME_);
create index ACT_IDX_HI_PRO_I_BUSKEY on ACT_HI_PROCINST(BUSINESS_KEY_);
create index ACT_IDX_HI_ACT_INST_START on ACT_HI_ACTINST(START_TIME_);
create index ACT_IDX_HI_ACT_INST_END on ACT_HI_ACTINST(END_TIME_);
create index ACT_IDX_HI_DETAIL_PROC_INST on ACT_HI_DETAIL(PROC_INST_ID_);
create index ACT_IDX_HI_DETAIL_ACT_INST on ACT_HI_DETAIL(ACT_INST_ID_);
create index ACT_IDX_HI_DETAIL_TIME on ACT_HI_DETAIL(TIME_);
create index ACT_IDX_HI_DETAIL_NAME on ACT_HI_DETAIL(NAME_);
create index ACT_IDX_HI_DETAIL_TASK_ID on ACT_HI_DETAIL(TASK_ID_);
create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);
create index ACT_IDX_HI_ACT_INST_PROCINST on ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);
create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);
create index ACT_IDX_HI_IDENT_LNK_USER on ACT_HI_IDENTITYLINK(USER_ID_);
create index ACT_IDX_HI_IDENT_LNK_TASK on ACT_HI_IDENTITYLINK(TASK_ID_);
create index ACT_IDX_HI_IDENT_LNK_PROCINST on ACT_HI_IDENTITYLINK(PROC_INST_ID_);