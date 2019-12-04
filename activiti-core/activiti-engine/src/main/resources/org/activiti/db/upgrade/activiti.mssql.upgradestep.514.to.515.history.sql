alter table ACT_HI_TASKINST
    add CATEGORY_ nvarchar(255);
    
drop index ACT_HI_PROCINST.ACT_UNIQ_HI_BUS_KEY;    

alter table ACT_HI_VARINST
    add CREATE_TIME_ datetime; 
    
alter table ACT_HI_VARINST
    add LAST_UPDATED_TIME_ datetime; 
    
alter table ACT_HI_PROCINST
    add TENANT_ID_ nvarchar(255) default ''; 
       
alter table ACT_HI_ACTINST
    add TENANT_ID_ nvarchar(255) default ''; 
    
alter table ACT_HI_TASKINST
    add TENANT_ID_ nvarchar(255) default '';       
    
alter table ACT_HI_ACTINST
    alter column ASSIGNEE_ nvarchar(255);
    