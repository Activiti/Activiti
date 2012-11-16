alter table ACT_RE_PROCDEF
    modify KEY_ not null;

alter table ACT_RE_PROCDEF
    modify VERSION_ not null;
    
alter table ACT_RE_DEPLOYMENT 
    add CATEGORY_ NVARCHAR2(255);
    
alter table ACT_RE_PROCDEF
    add DESCRIPTION_ NVARCHAR2(2000);

create table ACT_RE_MODEL (
    ID_ NVARCHAR2(64) not null,
    NAME_ NVARCHAR2(255),
    CATEGORY_ NVARCHAR2(255),
    CREATE_TIME_ TIMESTAMP(6),
    VERSION_ INTEGER,
    META_INFO_ NVARCHAR2(2000),
    EDITOR_SOURCE_VALUE_ID_ NVARCHAR2(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ NVARCHAR2(64),
    primary key (ID_)
);

create index ACT_IDX_MODEL_SOURCE on ACT_RE_MODEL(EDITOR_SOURCE_VALUE_ID_);
alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE 
    foreign key (EDITOR_SOURCE_VALUE_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_MODEL_SOURCE_EXTRA on ACT_RE_MODEL(EDITOR_SOURCE_EXTRA_VALUE_ID_);
alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE_EXTRA 
    foreign key (EDITOR_SOURCE_EXTRA_VALUE_ID_) 
    references ACT_GE_BYTEARRAY (ID_);

delete from ACT_GE_PROPERTY where NAME_ = 'historyLevel';

update ACT_GE_PROPERTY set VALUE_ = '5.11' where NAME_ = 'schema.version';
