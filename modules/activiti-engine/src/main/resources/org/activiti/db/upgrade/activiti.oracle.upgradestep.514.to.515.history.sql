alter table ACT_HI_TASKINST
    add CATEGORY_ NVARCHAR2(255);
    
drop index ACT_UNIQ_HI_BUS_KEY;    