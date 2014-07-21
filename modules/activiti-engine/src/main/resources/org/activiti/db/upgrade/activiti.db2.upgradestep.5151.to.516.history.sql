alter table ACT_HI_PROCINST
	add NAME_ varchar(255);
	
Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_PROCINST');
