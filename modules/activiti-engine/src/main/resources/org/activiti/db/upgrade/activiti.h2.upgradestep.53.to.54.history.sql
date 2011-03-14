alter table ACT_HI_DETAIL 
add DUE_DATE_ timestamp;

create table ACT_HI_ATTACHMENT (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    DESCRIPTION_ varchar(255),
    TYPE_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    REF_ varchar(255),
    CONTENT_ID_ varchar(64),
    primary key (ID_)
);
