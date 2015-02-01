alter table ACT_RU_EXECUTION 
add SUSPENSION_STATE_ int;

alter table ACT_RE_PROCDEF
add SUSPENSION_STATE_ int;

alter table ACT_RE_PROCDEF
add REV_ int;

update ACT_RE_PROCDEF set REV_ = 1;
update ACT_RE_PROCDEF set SUSPENSION_STATE_ = 1;
update ACT_RU_EXECUTION set SUSPENSION_STATE_ = 1;





create table ACT_RU_EVENT_SUBSCR (
    ID_ nvarchar(64) not null,
    REV_ int,
    EVENT_TYPE_ nvarchar(255) not null,
    EVENT_NAME_ nvarchar(255),
    EXECUTION_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    ACTIVITY_ID_ nvarchar(64),
    CONFIGURATION_ nvarchar(255),
    CREATED_ datetime not null,
    primary key (ID_)
);

create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR(CONFIGURATION_);

alter table ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION(ID_);
    
    
        
    
    
    
alter table ACT_RU_EXECUTION
add IS_EVENT_SCOPE_ tinyint;

update ACT_RU_EXECUTION set IS_EVENT_SCOPE_ = 0;




alter table ACT_HI_PROCINST
add DELETE_REASON_ nvarchar(4000);




alter table ACT_GE_BYTEARRAY 
add GENERATED_ tinyint;

update ACT_GE_BYTEARRAY set GENERATED_ = 0;