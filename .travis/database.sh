#!/bin/sh -e

cd server/src/test/ddl

"$ORACLE_HOME/bin/sqlplus" -L / AS SYSDBA @all.sql
