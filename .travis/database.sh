#!/bin/sh -e

pwd
env

"$ORACLE_HOME/bin/sqlplus" -L -S / AS SYSDBA <<SQL
select count(*) from cat;
SQL
