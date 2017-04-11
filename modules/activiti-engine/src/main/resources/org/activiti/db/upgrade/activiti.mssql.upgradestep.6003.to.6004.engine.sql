alter table ACT_RU_TASK add IS_COUNT_ENABLED_ tinyint;
alter table ACT_RU_TASK add VAR_COUNT_ int;
alter table ACT_RU_TASK add ID_LINK_COUNT_ int;

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.4' where NAME_ = 'schema.version';