alter table ACT_HI_TASKINST
  add CLAIM_TIME_ datetime;

alter table ACT_HI_TASKINST
  add FORM_KEY_ varchar(255);

create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);