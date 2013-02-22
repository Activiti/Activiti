alter table ACT_HI_TASKINST
	add CLAIM_TIME_ timestamp;

alter table ACT_HI_TASKINST
  add FORM_KEY_ varchar(255);

Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_TASKINST');

create index ACT_IDX_HI_ACT_INST_EXEC on ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);