alter table ACT_RE_PROCDEF
    alter column KEY_ nvarchar(255) not null;

alter table ACT_RE_PROCDEF
    alter column VERSION_ int not null;
    
alter table ACT_RE_DEPLOYMENT 
    add CATEGORY_ nvarchar(255);
    
alter table ACT_RE_PROCDEF
    add DESCRIPTION_ nvarchar(4000);

create table ACT_RE_MODEL (
    ID_ nvarchar(64) not null,
    NAME_ nvarchar(255),
    CATEGORY_ nvarchar(255),
    CREATE_TIME_ datetime,
    VERSION_ int,
    META_INFO_ nvarchar(4000),
    EDITOR_SOURCE_VALUE_ID_ nvarchar(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ nvarchar(64),
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
