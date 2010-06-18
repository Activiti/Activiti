insert into ACT_ID_GROUP values ('admin',   'System administrator', 'security-role');
insert into ACT_ID_GROUP values ('user',    'User', 'security-role');
insert into ACT_ID_GROUP values ('manager', 'Manager', 'security-role');
insert into ACT_ID_GROUP values ('management',  'Management',  'assignment');
insert into ACT_ID_GROUP values ('accountancy', 'Accountancy', 'assignment');
insert into ACT_ID_GROUP values ('engineering', 'Engineering', 'assignment');
insert into ACT_ID_GROUP values ('sales',       'Sales', 'assignment');

insert into ACT_ID_USER values ('kermit', 'Kermit', 'the Frog', 'kermit@localhost', 'kermit');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'admin');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'manager');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'management');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'accountancy');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'engineering');
insert into ACT_ID_MEMBERSHIP values ('kermit', 'sales');

insert into ACT_ID_USER values ('fozzie', 'Fozzie', 'Bear', 'fozzie@localhost', 'fozzie');
insert into ACT_ID_MEMBERSHIP values ('fozzie', 'user');
insert into ACT_ID_MEMBERSHIP values ('fozzie', 'accountancy');

insert into ACT_ID_USER values ('gonzo', 'Gonzo', 'the Great', 'gonzo@localhost', 'gonzo');
insert into ACT_ID_MEMBERSHIP values ('gonzo', 'manager');
insert into ACT_ID_MEMBERSHIP values ('gonzo', 'management');
insert into ACT_ID_MEMBERSHIP values ('gonzo', 'accountancy');
insert into ACT_ID_MEMBERSHIP values ('gonzo', 'sales');

update ACT_PROPERTY
set VALUE_ = '10'
where NAME_ = 'next.dbid';
