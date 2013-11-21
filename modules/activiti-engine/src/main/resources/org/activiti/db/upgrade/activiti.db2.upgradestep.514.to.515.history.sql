alter table ACT_HI_TASKINST
    add CATEGORY_ varchar(255);
    
drop index ACT_UNIQ_HI_BUS_KEY;
alter table ACT_HI_PROCINST drop colum UNI_BUSINESS_KEY;
alter table ACT_HI_PROCINST drop colum UNI_PROC_DEF_ID;    