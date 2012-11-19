drop schema activiti;
create database activiti character set utf8 collate utf8_bin;
grant all on activiti.* to 'activiti'@'localhost' identified by 'activiti' with grant option;
