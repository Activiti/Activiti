update ACT_GE_PROPERTY set VALUE_ = '5.20.0.0' where NAME_ = 'schema.version';

alter table ACT_RE_DEPLOYMENT modify DEPLOY_TIME_ timestamp(3) NULL;

alter table ACT_RU_TASK modify CREATE_TIME_ timestamp(3) NULL;