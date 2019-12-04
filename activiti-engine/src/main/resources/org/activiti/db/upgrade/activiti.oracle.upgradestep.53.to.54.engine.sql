alter table ACT_RU_TASK 
add OWNER_ NVARCHAR2(64);

alter table ACT_RU_TASK 
add DELEGATION_ NVARCHAR2(64);

alter table ACT_RU_TASK 
add DUE_DATE_ TIMESTAMP(6);
