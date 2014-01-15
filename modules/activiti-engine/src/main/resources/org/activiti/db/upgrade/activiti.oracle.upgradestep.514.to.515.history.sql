alter table ACT_HI_TASKINST
    add CATEGORY_ NVARCHAR2(255);
    
drop index ACT_UNIQ_HI_BUS_KEY;    
    
alter table ACT_HI_VARINST
    add CREATE_TIME_ TIMESTAMP(6); 
    
alter table ACT_HI_VARINST
    add LAST_UPDATED_TIME_ TIMESTAMP(6);     