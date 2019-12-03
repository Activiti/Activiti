alter table ACT_RU_EXECUTION 
add SUSPENSION_STATE_ INTEGER;

alter table ACT_RE_PROCDEF
add SUSPENSION_STATE_ INTEGER;

alter table ACT_RE_PROCDEF
add REV_ INTEGER;

update ACT_RE_PROCDEF set REV_ = 1;
update ACT_RE_PROCDEF set SUSPENSION_STATE_ = 1;
update ACT_RU_EXECUTION set SUSPENSION_STATE_ = 1;





create table ACT_RU_EVENT_SUBSCR (
    ID_ NVARCHAR2(64) not null,
    REV_ integer,
    EVENT_TYPE_ NVARCHAR2(255) not null,
    EVENT_NAME_ NVARCHAR2(255),
    EXECUTION_ID_ NVARCHAR2(64),
    PROC_INST_ID_ NVARCHAR2(64),
    ACTIVITY_ID_ NVARCHAR2(64),
    CONFIGURATION_ NVARCHAR2(255),
    CREATED_ TIMESTAMP(6) not null,
    primary key (ID_)
);
create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR(CONFIGURATION_);

create index ACT_IDX_EVENT_SUBSCR on ACT_RU_EVENT_SUBSCR(EXECUTION_ID_);
alter table ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION(ID_);
    
    
    
    
    
    
alter table ACT_RU_EXECUTION 
add IS_EVENT_SCOPE_ NUMBER(1,0) CHECK (IS_EVENT_SCOPE_ IN (1,0));

update ACT_RU_EXECUTION set IS_EVENT_SCOPE_ = 0;




alter table ACT_HI_PROCINST
add DELETE_REASON_ NVARCHAR2(2000);




alter table ACT_GE_BYTEARRAY 
add GENERATED_ NUMBER(1,0) CHECK (GENERATED_ IN (1,0));

update ACT_GE_BYTEARRAY set GENERATED_ = 0;