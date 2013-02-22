alter table ACT_HI_TASKINST
  add column CLAIM_TIME_ timestamp;

alter table ACT_HI_TASKINST
  add column FORM_KEY_ varchar(255);
  
create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);
  