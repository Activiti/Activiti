alter table ACT_HI_COMMENT 
add TYPE_ varchar(255);

alter table ACT_HI_COMMENT 
add ACTION_ varchar(255);

alter table ACT_HI_COMMENT 
add FULL_MSG_ BLOB;

alter table ACT_HI_TASKINST 
add OWNER_ varchar(64);

alter table ACT_HI_TASKINST 
add PARENT_TASK_ID_ varchar(64);
