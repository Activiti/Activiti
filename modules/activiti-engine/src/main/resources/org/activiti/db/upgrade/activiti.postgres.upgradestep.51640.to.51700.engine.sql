update ACT_GE_PROPERTY set VALUE_ = '5.17.0.0' where NAME_ = 'schema.version';

alter table ACT_RE_PROCDEF add HAS_GRAPHICAL_NOTATION_ boolean;
