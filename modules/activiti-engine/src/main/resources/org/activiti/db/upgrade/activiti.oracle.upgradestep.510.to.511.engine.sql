alter table ACT_RE_PROCDEF
    modify KEY_ not null;

alter table ACT_RE_PROCDEF
    modify VERSION_ not null;
    
alter table ACT_RE_DEPLOYMENT 
    add CATEGORY_ NVARCHAR2(255);
    
alter table ACT_RE_PROCDEF
    add DESCRIPTION_ NVARCHAR2(4000);

create table ACT_RE_MODEL (
    ID_ NVARCHAR2(64) not null,
    NAME_ NVARCHAR2(255),
    CATEGORY_ NVARCHAR2(255),
    CREATE_TIME_ TIMESTAMP(6),
    VERSION_ INTEGER,
    META_INFO_ NVARCHAR2(4000),
    EDITOR_SOURCE_ BLOB,
    EDITOR_SOURCE_EXTRA_ BLOB,
    primary key (ID_)
);
