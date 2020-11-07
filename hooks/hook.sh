#!/bin/sh
CONFIGID=$(grep 'id[[:blank:]]*=[[:blank:]]*"org.olpc_france..*."' config.xml | sed -e "s/.*id[ \t]*=[ \t]*\"org.olpc_france.sugarizeros\([^\"]*\)\".*/\1/")
rm -f platforms/android/src/org/olpc_france/sugarizer*/MainActivity.java
if [ -n "$CONFIGID" ]; then
  cp plugins/cordova-plugin-sugarizeros/android/MainActivity.java platforms/android/src/org/olpc_france/sugarizer$CONFIGID/
fi
