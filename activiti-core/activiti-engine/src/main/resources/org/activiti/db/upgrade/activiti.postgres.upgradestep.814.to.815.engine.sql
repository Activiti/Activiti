update ACT_GE_PROPERTY set VALUE_ = '8.1.5' where NAME_ = 'schema.version';

ALTER TABLE act_evt_log ALTER COLUMN log_nr_ TYPE bigint;
ALTER SEQUENCE act_evt_log_log_nr__seq AS bigint;
ALTER SEQUENCE act_evt_log_log_nr__seq NO MAXVALUE;
