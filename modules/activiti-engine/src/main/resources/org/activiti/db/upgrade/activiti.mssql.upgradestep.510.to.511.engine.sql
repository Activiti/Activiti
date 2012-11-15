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
    EDITOR_SOURCE_ image,
    EDITOR_SOURCE_EXTRA_ image,
    primary key (ID_)
);

delete from ACT_GE_PROPERTY where NAME_ = 'historyLevel';