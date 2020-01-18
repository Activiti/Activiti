alter table ACT_HI_TASKINST
    add CATEGORY_ varchar(255);
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_TASKINST');    
    
drop index ACT_UNIQ_HI_BUS_KEY;

alter table ACT_HI_VARINST
    add CREATE_TIME_ timestamp; 
    
alter table ACT_HI_VARINST
    add LAST_UPDATED_TIME_ timestamp; 

-- DB2 *cannot* drop columns. Yes, this is 2013.
-- This means that for DB2 the columns will remain as they are (they won't be used)
-- alter table ACT_HI_PROCINST drop colum UNI_BUSINESS_KEY;
-- alter table ACT_HI_PROCINST drop colum UNI_PROC_DEF_ID;
-- Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_PROCINST'); 


alter table ACT_HI_PROCINST
    add TENANT_ID_ varchar(255) default ''; 
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_PROCINST');
       
alter table ACT_HI_ACTINST
    add TENANT_ID_ varchar(255) default ''; 
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_ACTINST');    
    
alter table ACT_HI_TASKINST
    add TENANT_ID_ varchar(255) default '';  
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_TASKINST');      

alter table ACT_HI_ACTINST alter column ASSIGNEE_ SET DATA TYPE varchar(255);

Call Sysproc.admin_cmd ('REORG TABLE ACT_HI_ACTINST');