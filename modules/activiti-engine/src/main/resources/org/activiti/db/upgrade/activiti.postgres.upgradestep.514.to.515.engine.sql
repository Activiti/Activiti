alter table ACT_RU_TASK 
    add CATEGORY_ varchar(255);
    
alter table ACT_RU_EXECUTION drop constraint act_ru_execution_proc_def_id__business_key__key;

alter table ACT_RE_DEPLOYMENT 
    add TENANT_ID_ varchar(255);  
    
alter table ACT_RE_PROCDEF 
    add TENANT_ID_ varchar(255); 
    
alter table ACT_RU_EXECUTION
    add TENANT_ID_ varchar(255);       
    
alter table ACT_RU_TASK
    add TENANT_ID_ varchar(255);  
    
alter table ACT_RU_JOB
    add TENANT_ID_ varchar(255);   
    
alter table ACT_RE_MODEL
    add TENANT_ID_ varchar(255);        

update ACT_GE_PROPERTY set VALUE_ = '5.15-SNAPSHOT' where NAME_ = 'schema.version';
