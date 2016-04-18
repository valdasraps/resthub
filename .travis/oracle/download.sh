#!/bin/sh -e

[ -n "$ORACLE_FILE" ] || { echo "Missing ORACLE_FILE environment variable!"; exit 1; }
[ -n "$ORACLE_URL" ] || { echo "Missing ORACLE_URL environment variable!"; exit 1; }
[ -n "$ORACLE_URL_USER" ] || { echo "Missing ORACLE_URL_USER environment variable!"; exit 1; }
[ -n "$ORACLE_URL_PASS" ] || { echo "Missing ORACLE_URL_PASS environment variable!"; exit 1; }

cd "$(dirname "$(readlink -f "$0")")"

wget --user="$ORACLE_URL_USER" --password="$ORACLE_URL_PASS" "$ORACLE_URL"
