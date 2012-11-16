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
    EDITOR_SOURCE_VALUE_ID_ varchar(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ varchar(64),
    primary key (ID_)
);

alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE 
    foreign key (EDITOR_SOURCE_VALUE_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE_EXTRA 
    foreign key (EDITOR_SOURCE_EXTRA_VALUE_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

delete from ACT_GE_PROPERTY where NAME_ = 'historyLevel';

update ACT_GE_PROPERTY set VALUE_ = '5.11' where NAME_ = 'schema.version';
