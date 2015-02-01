alter table ACT_HI_COMMENT 
add TYPE_ nvarchar(255);

alter table ACT_HI_COMMENT 
add ACTION_ nvarchar(255);

alter table ACT_HI_COMMENT 
add FULL_MSG_ image;

alter table ACT_HI_TASKINST 
add OWNER_ nvarchar(64);

alter table ACT_HI_TASKINST 
add PARENT_TASK_ID_ nvarchar(64);
