alter table ACT_HI_TASKINST
    add CATEGORY_ varchar(255);
    
alter table ACT_HI_PROCINST drop constraint act_hi_procinst_proc_def_id__business_key__key;    
    