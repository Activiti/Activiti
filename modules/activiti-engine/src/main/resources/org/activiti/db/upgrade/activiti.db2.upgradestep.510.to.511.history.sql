create table ACT_HI_PROCVARIABLE (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
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

create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_PROCVARIABLE(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_PROCVARIABLE(NAME_, VAR_TYPE_);

insert into ACT_HI_PROCVARIABLE
  (ID_,PROC_INST_ID_,NAME_,VAR_TYPE_,REV_,BYTEARRAY_ID_,DOUBLE_,LONG_,TEXT_,TEXT2_)
  select d.ID_,d.PROC_INST_ID_,d.NAME_,d.VAR_TYPE_,d.REV_,d.BYTEARRAY_ID_,d.DOUBLE_,d.LONG_,d.TEXT_,d.TEXT2_
  from ACT_HI_DETAIL d
  inner join
    (
      select de.PROC_INST_ID_, de.NAME_, MAX(de.TIME_) as MAXTIME
      from ACT_HI_DETAIL de
      inner join ACT_HI_PROCINST p on de.PROC_INST_ID_ = p.ID_
      where p.END_TIME_ is not NULL
      group by de.PROC_INST_ID_, de.NAME_
    )
  groupeddetail on d.PROC_INST_ID_ = groupeddetail.PROC_INST_ID_
  and d.NAME_ = groupeddetail.NAME_
  and d.TIME_ = groupeddetail.MAXTIME
  and (select prop.VALUE_ from ACT_GE_PROPERTY prop where prop.NAME_ = 'historyLevel') = 3;
  
update ACT_GE_PROPERTY
  set VALUE_ = VALUE_ + 1,
      REV_ = REV_ + 1
  where NAME_ = 'historyLevel' and VALUE_ >= 2;

alter table ACT_HI_ACTINST
	add TASK_ID_ varchar(64);
	
alter table ACT_HI_ACTINST
	add CALL_PROC_INST_ID_ varchar(64);

alter table ACT_HI_DETAIL
	alter column PROC_DEF_ID_ DROP NOT NULL;

alter table ACT_HI_DETAIL
	alter column EXECUTION_ID_ DROP NOT NULL;