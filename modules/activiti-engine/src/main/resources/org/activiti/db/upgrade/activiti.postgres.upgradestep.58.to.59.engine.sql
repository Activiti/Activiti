alter table ACT_RU_EXECUTION 
add column SUSPENSION_STATE_ integer;

alter table ACT_RE_PROCDEF
add column SUSPENSION_STATE_ integer;

alter table ACT_RE_PROCDEF
add column REV_ integer;

update ACT_RE_PROCDEF set REV_ = 1;
update ACT_RE_PROCDEF set SUSPENSION_STATE_ = 1;
update ACT_RU_EXECUTION set SUSPENSION_STATE_ = 1;





create table ACT_RU_EVENT_SUBSCR (
    ID_ varchar(64) not null,
    REV_ integer,
    EVENT_TYPE_ varchar(255) not null,
    EVENT_NAME_ varchar(255),
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTIVITY_ID_ varchar(64),
    CONFIGURATION_ varchar(255),
    CREATED_ timestamp not null,
    primary key (ID_)
);

create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR(CONFIGURATION_);

create index ACT_IDX_EVENT_SUBSCR on ACT_RU_EVENT_SUBSCR(EXECUTION_ID_);
alter table ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION(ID_);
    
    
    
    
    
alter table ACT_RU_EXECUTION 
add column IS_EVENT_SCOPE_ boolean;

update ACT_RU_EXECUTION set IS_EVENT_SCOPE_ = false;



alter table ACT_HI_PROCINST
add DELETE_REASON_ varchar(4000);




alter table ACT_GE_BYTEARRAY 
add GENERATED_ boolean;

update ACT_GE_BYTEARRAY set GENERATED_ = false;
