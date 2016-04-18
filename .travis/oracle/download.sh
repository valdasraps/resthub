#!/bin/sh -e

[ -n "$ORACLE_FILE" ] || { echo "Missing ORACLE_FILE environment variable!"; exit 1; }
[ -n "$ORACLE_URL" ] || { echo "Missing ORACLE_URL environment variable!"; exit 1; }

cd "$(dirname "$(readlink -f "$0")")"

wget "$ORACLE_URL"
