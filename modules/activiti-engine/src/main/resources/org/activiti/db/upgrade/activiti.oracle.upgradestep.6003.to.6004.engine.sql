alter table ACT_RU_TASK add IS_COUNT_ENABLED_ NUMBER(1,0) CHECK (IS_COUNT_ENABLED_ IN (1,0));
alter table ACT_RU_TASK add VAR_COUNT_ INTEGER;
alter table ACT_RU_TASK add ID_LINK_COUNT_ INTEGER;

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.4' where NAME_ = 'schema.version';