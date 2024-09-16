#!/bin/bash
#
# Start script for disqualified-officers-delta-consumer


PORT=8080
exec java -jar -Dserver.port="${PORT}" "disqualified-officers-delta-consumer.jar"
