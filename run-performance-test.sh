#!/bin/bash
USERS=${1:-1}
RAMP=${2:-1}

# Run the Gatling test with configurable users and ramp duration
mvn gatling:test -Dusers=$USERS -DrampDuration=$RAMP 