alter table ACT_HI_TASKINST
  add CLAIM_TIME_ timestamp;

alter table ACT_HI_TASKINST
  add FORM_KEY_ varchar(255);
  
alter table ACT_RU_IDENTITYLINK
  add PROC_INST_ID_ varchar(64);
  
alter table ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
    foreign key (PROC_INST_ID_) 
    references ACT_RU_EXECUTION (ID_);     
  
create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);