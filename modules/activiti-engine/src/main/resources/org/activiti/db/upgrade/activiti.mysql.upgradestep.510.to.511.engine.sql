alter table ACT_RE_PROCDEF 
    MODIFY KEY_ varchar(255) not null;

alter table ACT_RE_PROCDEF 
    MODIFY VERSION_ integer not null;
    
-- http://jira.codehaus.org/browse/ACT-1424    
alter table ACT_RU_JOB 
    MODIFY LOCK_EXP_TIME_ timestamp null;
    
alter table ACT_RE_DEPLOYMENT 
    add CATEGORY_ varchar(255);
    
alter table ACT_RE_PROCDEF
    add DESCRIPTION_ varchar(4000);

create table ACT_RE_MODEL (
    ID_ varchar(64) not null,
    NAME_ varchar(255),
    CATEGORY_ varchar(255),
    CREATE_TIME_ timestamp,
    VERSION_ integer,
    META_INFO_ varchar(4000),
    EDITOR_SOURCE_ LONGBLOB,
    EDITOR_SOURCE_EXTRA_ LONGBLOB,
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

delete from ACT_GE_PROPERTY where NAME_ = 'historyLevel';