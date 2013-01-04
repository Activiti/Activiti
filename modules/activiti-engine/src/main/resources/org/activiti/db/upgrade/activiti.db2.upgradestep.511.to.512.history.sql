Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_VARINST');

alter table ACT_HI_TASKINST
	add CLAIM_TIME_ timestamp;
