#!/bin/sh -e

cd server/src/test/ddl

"$ORACLE_HOME/bin/sqlplus" -L -S / AS SYSDBA @all.sql
