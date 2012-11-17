create table ACT_HI_VARINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(100),
    REV_ integer,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
);

create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);

Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_VARINST');

update ACT_GE_PROPERTY
  set VALUE_ = VALUE_ + 1,
      REV_ = REV_ + 1
  where NAME_ = 'historyLevel' and VALUE_ >= 2;

alter table ACT_HI_ACTINST
	add TASK_ID_ varchar(64);
	
alter table ACT_HI_ACTINST
	add CALL_PROC_INST_ID_ varchar(64);

alter table ACT_HI_DETAIL
	alter column PROC_INST_ID_ DROP NOT NULL;

alter table ACT_HI_DETAIL
	alter column EXECUTION_ID_ DROP NOT NULL;

Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_DETAIL');
