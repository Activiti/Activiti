#!/bin/sh
echo "Dropping activitiadmin schema"
mysql -u alfresco -palfresco -e "DROP SCHEMA activitiadmin"

echo "Creating activitiadmin schema"
mysql -u alfresco -palfresco -e "CREATE SCHEMA activitiadmin DEFAULT CHARACTER SET utf8 COLLATE utf8_bin"

./start.sh $@