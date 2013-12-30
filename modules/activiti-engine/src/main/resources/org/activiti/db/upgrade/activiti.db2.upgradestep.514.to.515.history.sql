alter table ACT_HI_TASKINST
    add CATEGORY_ varchar(255);
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_TASKINST');    
    
drop index ACT_UNIQ_HI_BUS_KEY;

-- DB2 *cannot* drop columns. Yes, this is 2013.
-- This means that for DB2 the columns will remain as they are (they won't be used)
-- alter table ACT_HI_PROCINST drop colum UNI_BUSINESS_KEY;
-- alter table ACT_HI_PROCINST drop colum UNI_PROC_DEF_ID;

Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_PROCINST');    