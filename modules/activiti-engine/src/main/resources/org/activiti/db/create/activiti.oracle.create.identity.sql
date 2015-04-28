create table ACT_ID_GROUP (
    ID_ VARCHAR2(64),
    REV_ INTEGER,
    NAME_ VARCHAR2(255),
    TYPE_ VARCHAR2(255),
    primary key (ID_)
);

create table ACT_ID_MEMBERSHIP (
    USER_ID_ VARCHAR2(64),
    GROUP_ID_ VARCHAR2(64),
    primary key (USER_ID_, GROUP_ID_)
);

create table ACT_ID_USER (
    ID_ VARCHAR2(64),
    REV_ INTEGER,
    FIRST_ VARCHAR2(255),
    LAST_ VARCHAR2(255),
    EMAIL_ VARCHAR2(255),
    PWD_ VARCHAR2(255),
    PICTURE_ID_ VARCHAR2(64),
    primary key (ID_)
);

create table ACT_ID_INFO (
    ID_ VARCHAR2(64),
    REV_ INTEGER,
    USER_ID_ VARCHAR2(64),
    TYPE_ VARCHAR2(64),
    KEY_ VARCHAR2(255),
    VALUE_ VARCHAR2(255),
    PASSWORD_ BLOB,
    PARENT_ID_ VARCHAR2(255),
    primary key (ID_)
);

create index ACT_IDX_MEMB_GROUP on ACT_ID_MEMBERSHIP(GROUP_ID_);
alter table ACT_ID_MEMBERSHIP 
    add constraint ACT_FK_MEMB_GROUP 
    foreign key (GROUP_ID_) 
    references ACT_ID_GROUP (ID_);

create index ACT_IDX_MEMB_USER on ACT_ID_MEMBERSHIP(USER_ID_);
alter table ACT_ID_MEMBERSHIP 
    add constraint ACT_FK_MEMB_USER
    foreign key (USER_ID_) 
    references ACT_ID_USER (ID_);
