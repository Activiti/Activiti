alter table ACT_RU_TASK 
add OWNER_ nvarchar(64);

alter table ACT_RU_TASK 
add DELEGATION_ nvarchar(64);

alter table ACT_RU_TASK 
add DUE_DATE_ datetime;