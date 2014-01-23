alter table ACT_RU_TASK 
    add CATEGORY_ varchar(255);

Call Sysproc.admin_cmd ('REORG TABLE ACT_RU_TASK');
        
drop index ACT_UNIQ_RU_BUS_KEY;

-- DB2 *cannot* drop columns. Yes, this is 2013.
-- This means that for DB2 the columns will remain as they are (they won't be used)
-- alter table ACT_RU_EXECUTION drop colum UNI_BUSINESS_KEY;
-- alter table ACT_RU_EXECUTION drop colum UNI_PROC_DEF_ID;

Call Sysproc.admin_cmd ('REORG TABLE ACT_RU_EXECUTION');

alter table ACT_RE_DEPLOYMENT 
    add TENANT_ID_ varchar(255);
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_RE_DEPLOYMENT');

alter table ACT_RE_PROCDEF 
    add TENANT_ID_ varchar(255);
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_RE_PROCDEF');

alter table ACT_RU_EXECUTION
    add TENANT_ID_ varchar(255);
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_RU_EXECUTION');  

alter table ACT_RU_TASK
    add TENANT_ID_ varchar(255);
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_RU_TASK');   

alter table ACT_RU_JOB
    add TENANT_ID_ varchar(255);
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_RU_JOB');  

alter table ACT_RE_MODEL
    add TENANT_ID_ varchar(255);
    
Call Sysproc.admin_cmd ('REORG TABLE ACT_RE_MODEL');  

update ACT_GE_PROPERTY set VALUE_ = '5.15-SNAPSHOT' where NAME_ = 'schema.version';
