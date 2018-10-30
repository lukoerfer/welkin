#!/bin/sh

# WARNING: this script is executed by ant from the root folder 
#          (where the build.xml is located) therefore all relative 
#          file references should be relative to that directory or the
#          script will break.

echo "Building MacOSX DMG for Welkin " $1
hdiutil create -ov -size 5m -fs HFS+ -volname "Welkin" pre_welkin
hdiutil attach pre_welkin.dmg
/Developer/Tools/CpMac -r dist/Welkin.app /Volumes/Welkin
/Developer/Tools/CpMac misc/dmg_template/dmg_background.png /Volumes/Welkin/dmg_background.png
/Developer/Tools/SetFile -a V /Volumes/Welkin/dmg_background.png
/Developer/Tools/CpMac misc/dmg_template/DS_Store /Volumes/Welkin/.DS_Store
#open /Volumes/Welkin/
hdiutil detach /Volumes/Welkin
hdiutil convert pre_welkin.dmg -format UDZO -imagekey zlib-level=9 -o welkin.dmg
rm pre_welkin.dmg
hdiutil unflatten welkin.dmg
/Developer/Tools/Rez /Developer/Headers/FlatCarbon/*.r misc/SLA.r -a -o welkin.dmg
hdiutil flatten welkin.dmg
hdiutil internet-enable -yes welkin.dmg
mv welkin.dmg dist/welkin-$1.dmg