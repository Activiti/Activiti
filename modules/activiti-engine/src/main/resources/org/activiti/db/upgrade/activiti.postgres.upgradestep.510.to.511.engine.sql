alter table ACT_RE_PROCDEF
    alter column KEY_ set not null;

alter table ACT_RE_PROCDEF
    alter column VERSION_ set not null;
    
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
    EDITOR_SOURCE_ bytea,
    EDITOR_SOURCE_EXTRA_ bytea,
    primary key (ID_)
);

delete from ACT_GE_PROPERTY where NAME_ = 'historyLevel';