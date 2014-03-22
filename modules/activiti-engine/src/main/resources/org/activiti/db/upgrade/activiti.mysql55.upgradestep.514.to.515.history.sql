# MySQL < 5.6.4 does not support timestamps/dates with millisecond precision.
# This upgrade file lacks the necessary upgrades that are done on a 5.6.4+ installation
# to get millisecond precision.

alter table ACT_HI_TASKINST
    add CATEGORY_ varchar(255);
    
alter table ACT_HI_PROCINST drop index ACT_UNIQ_HI_BUS_KEY;   

alter table ACT_HI_VARINST
    add CREATE_TIME_ datetime; 
    
alter table ACT_HI_VARINST
    add LAST_UPDATED_TIME_ datetime; 
    
alter table ACT_HI_PROCINST
    add TENANT_ID_ varchar(255) default ''; 
       
alter table ACT_HI_ACTINST
    add TENANT_ID_ varchar(255) default ''; 
    
alter table ACT_HI_TASKINST
    add TENANT_ID_ varchar(255) default '';
    
alter table ACT_HI_ACTINST
    modify ASSIGNEE_ varchar(255);