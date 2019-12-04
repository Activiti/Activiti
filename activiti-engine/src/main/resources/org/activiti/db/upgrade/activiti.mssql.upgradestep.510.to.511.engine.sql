alter table ACT_RE_PROCDEF drop constraint ACT_UNIQ_PROCDEF;

alter table ACT_RE_PROCDEF alter column KEY_ nvarchar(255) not null;

alter table ACT_RE_PROCDEF alter column VERSION_ int not null;

alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_);

alter table ACT_RE_DEPLOYMENT add CATEGORY_ nvarchar(255);
    
alter table ACT_RE_PROCDEF
    add DESCRIPTION_ nvarchar(4000);
    
alter table ACT_RU_TASK
    add SUSPENSION_STATE_ int;
    
update ACT_RU_TASK set SUSPENSION_STATE_ = 1; 

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF (ID_);

create table ACT_RE_MODEL (
    ID_ nvarchar(64) not null,
    REV_ int,
    NAME_ nvarchar(255),
    KEY_ nvarchar(255),
    CATEGORY_ nvarchar(255),
    CREATE_TIME_ datetime,
    LAST_UPDATE_TIME_ datetime,
    VERSION_ int,
    META_INFO_ nvarchar(4000),
    DEPLOYMENT_ID_ nvarchar(64),
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
    
alter table ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_DEPLOYMENT 
    foreign key (DEPLOYMENT_ID_) 
    references ACT_RE_DEPLOYMENT (ID_);   

delete from ACT_GE_PROPERTY where NAME_ = 'historyLevel';

alter table ACT_RU_JOB
    add PROC_DEF_ID_ nvarchar(64);

update ACT_GE_PROPERTY set VALUE_ = '5.11' where NAME_ = 'schema.version';
