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
