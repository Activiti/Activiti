alter table ACT_HI_DETAIL 
add DUE_DATE_ datetime;

alter table ACT_HI_TASKINST 
add DUE_DATE_ datetime;

create table ACT_HI_COMMENT (
    ID_ nvarchar(64) not null,
    TIME_ datetime not null,
    USER_ID_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    MESSAGE_ nvarchar(255),
    primary key (ID_)
);

create table ACT_HI_ATTACHMENT (
    ID_ nvarchar(64) not null,
    REV_ integer,
    NAME_ nvarchar(255),
    DESCRIPTION_ nvarchar(255),
    TYPE_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    URL_ nvarchar(255),
    CONTENT_ID_ nvarchar(64),
    primary key (ID_)
);
