create table ACT_HI_VARINST (
    ID_ NVARCHAR2(64) not null,
    PROC_INST_ID_ NVARCHAR2(64) not null,
    NAME_ NVARCHAR2(255) not null,
    VAR_TYPE_ NVARCHAR2(100),
    REV_ INTEGER,
    BYTEARRAY_ID_ NVARCHAR2(64),
    DOUBLE_ NUMBER(*,10),
    LONG_ NUMBER(19,0),
    TEXT_ NVARCHAR2(2000),
    TEXT2_ NVARCHAR2(2000),
    primary key (ID_)
);

create index ACT_IDX_HI_PROCVAR_PROC_INST on ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ACT_HI_VARINST(NAME_, VAR_TYPE_);

insert into ACT_HI_VARINST
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
add TASK_ID_ NVARCHAR2(64);

alter table ACT_HI_ACTINST
add CALL_PROC_INST_ID_ NVARCHAR2(64);

alter table ACT_HI_DETAIL
	modify PROC_INST_ID_ NVARCHAR2(64) null;

alter table ACT_HI_DETAIL
	modify EXECUTION_ID_ NVARCHAR2(64) null;
