#!/bin/sh -e

[ -n "$ORACLE_COOKIE" ] || { echo "Missing ORACLE_COOKIE environment variable!"; exit 1; }
[ -n "$ORACLE_FILE" ] || { echo "Missing ORACLE_FILE environment variable!"; exit 1; }

cd "$(dirname "$(readlink -f "$0")")"

npm install bluebird node-phantom-simple

export COOKIES='cookies.txt'
export USER_AGENT='Mozilla/5.0'

echo > "$COOKIES"
chmod 600 "$COOKIES"

exec node download.js
cat oracle-xe-11.2.0-1.0.x86_64.rpm.zip
