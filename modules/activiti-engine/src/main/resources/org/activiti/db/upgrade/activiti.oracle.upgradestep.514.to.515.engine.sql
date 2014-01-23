alter table ACT_RU_TASK 
    add CATEGORY_ NVARCHAR2(255);
    
drop index ACT_UNIQ_RU_BUS_KEY;  

alter table ACT_RE_DEPLOYMENT 
    add TENANT_ID_ NVARCHAR2(255);  
    
alter table ACT_RE_PROCDEF 
    add TENANT_ID_ NVARCHAR2(255); 
    
alter table ACT_RU_EXECUTION
    add TENANT_ID_ NVARCHAR2(255);  
    
alter table ACT_RU_TASK
    add TENANT_ID_ NVARCHAR2(255);
    
alter table ACT_RU_JOB
    add TENANT_ID_ NVARCHAR2(255); 
    
alter table ACT_RE_MODEL
    add TENANT_ID_ NVARCHAR2(255);      

update ACT_GE_PROPERTY set VALUE_ = '5.15-SNAPSHOT' where NAME_ = 'schema.version';
