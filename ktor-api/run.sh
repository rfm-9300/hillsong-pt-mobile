#!/bin/bash
   export $(grep -v '^#' .env | xargs)
   ./gradlew run
