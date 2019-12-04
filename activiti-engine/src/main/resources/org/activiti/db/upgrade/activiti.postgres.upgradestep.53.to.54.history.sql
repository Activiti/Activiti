alter table ACT_HI_DETAIL 
add DUE_DATE_ timestamp;

alter table ACT_HI_TASKINST 
add DUE_DATE_ timestamp;

create table ACT_HI_COMMENT (
    ID_ varchar(64) not null,
    TIME_ timestamp not null,
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    MESSAGE_ varchar(255),
    primary key (ID_)
);

create table ACT_HI_ATTACHMENT (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    DESCRIPTION_ varchar(255),
    TYPE_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    URL_ varchar(255),
    CONTENT_ID_ varchar(64),
    primary key (ID_)
);
