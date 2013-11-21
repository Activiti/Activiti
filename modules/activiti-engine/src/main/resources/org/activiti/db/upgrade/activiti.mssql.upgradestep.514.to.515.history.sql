alter table ACT_HI_TASKINST
    add CATEGORY_ nvarchar(255);
    
drop index ACT_HI_PROCINST.ACT_UNIQ_HI_BUS_KEY;    
    