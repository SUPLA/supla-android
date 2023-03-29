#!/bin/bash

set -e

PROJ=~/StudioProjects

cd "$PROJ"

docker run -v "$PROJ":/StudioProjects -it devel/supla-android-ci:latest /bin/bash
