alter table ACT_HI_DETAIL 
add TASK_ID_ varchar(64);

create table ACT_HI_TASKINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64),
    TASK_DEF_KEY_ varchar(255),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    NAME_ varchar(255),
    DESCRIPTION_ varchar(255),
    ASSIGNEE_ varchar(64),
    START_TIME_ datetime not null,
    END_TIME_ datetime,
    DURATION_ bigint,
    DELETE_REASON_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;
