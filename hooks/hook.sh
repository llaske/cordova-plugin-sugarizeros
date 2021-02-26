#!/bin/sh
CONFIGID=$(grep 'id[[:blank:]]*=[[:blank:]]*"org.olpc_france..*."' config.xml | sed -e "s/.*id[ \t]*=[ \t]*\"org.olpc_france.sugarizeros\([^\"]*\)\".*/\1/")
rm -f platforms/android/app/src/main/java/org/olpc_france/sugarizer*/MainActivity.java
mkdir -p platforms/android/app/src/main/java/org/olpc_france/sugarizer$CONFIGID/
cp plugins/cordova-plugin-sugarizeros/android/MainActivity.java platforms/android/app/src/main/java/org/olpc_france/sugarizer$CONFIGID/
if [ -n "$CONFIGID" ]; then
  sed -i -e "s/package org.olpc_france.sugarizeros/package org.olpc_france.sugarizer$CONFIGID/" platforms/android/app/src/main/java/org/olpc_france/sugarizer$CONFIGID/MainActivity.java
fi
