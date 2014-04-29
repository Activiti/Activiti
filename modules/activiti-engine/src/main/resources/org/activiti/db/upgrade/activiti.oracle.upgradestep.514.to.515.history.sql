alter table ACT_HI_TASKINST
    add CATEGORY_ NVARCHAR2(255);
    
drop index ACT_UNIQ_HI_BUS_KEY;    
    
alter table ACT_HI_VARINST
    add CREATE_TIME_ TIMESTAMP(6); 
    
alter table ACT_HI_VARINST
    add LAST_UPDATED_TIME_ TIMESTAMP(6);   
    
alter table ACT_HI_PROCINST
    add TENANT_ID_ NVARCHAR2(255) default ''; 
       
alter table ACT_HI_ACTINST
    add TENANT_ID_ NVARCHAR2(255) default ''; 
    
alter table ACT_HI_TASKINST
    add TENANT_ID_ NVARCHAR2(255) default '';    
    
alter table ACT_HI_ACTINST
    modify ASSIGNEE_ NVARCHAR2(255);