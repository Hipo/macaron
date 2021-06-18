#!/usr/bin/env bash
# fail if any commands fails
set -e
# debug log
set -x

# Set array split symbol which is '-'
IFS='-'

# To get bitrise tag, we have to call without double quotes. Calling with double quotes
# causes splitting issue. That's why we need to create new variable and call it with double quotes.
TAG=$BITRISE_GIT_TAG

# Create splitted array called TAG_SPLIT_ARRAY. IFS is being used to split TAG.
read -a TAG_SPLIT_ARRAY <<< "$TAG"

# Create environment variable for module name on Bitrise.
#envman add --key RELEASED_MODULE_NAME --value "${TAG_SPLIT_ARRAY[${#TAG_SPLIT_ARRAY[@]}-1]}"

# Comment out below line if you want to use secret variable created above by calling envman
RELEASED_MODULE_NAME="${TAG_SPLIT_ARRAY[${#TAG_SPLIT_ARRAY[@]}-1]}"

# Checks if there any missing property
./gradlew "$RELEASED_MODULE_NAME":checkProperties

# Generates pom, aar packages
./gradlew "$RELEASED_MODULE_NAME":build

# Uploads generated pom and aar packages
./gradlew "$RELEASED_MODULE_NAME":publish
