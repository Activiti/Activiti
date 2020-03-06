alter table ACT_ID_USER 
add PICTURE_ID_ NVARCHAR2(64);

create table ACT_ID_INFO (
    ID_ NVARCHAR2(64),
    REV_ integer,
    USER_ID_ NVARCHAR2(64),
    TYPE_ NVARCHAR2(64),
    KEY_ NVARCHAR2(255),
    VALUE_ NVARCHAR2(255),
    PASSWORD_ BLOB,
    PARENT_ID_ NVARCHAR2(255),
    primary key (ID_)
);
