alter table ACT_HI_TASKINST
    add CATEGORY_ varchar(255);
    
alter table ACT_HI_PROCINST drop constraint ACT_UNIQ_HI_BUS_KEY;    
    