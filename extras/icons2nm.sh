#!/bin/bash

while read -r line
do
  PNGFILEIN=$line.png
  PNGFILEOUT=${line}_nightmode.png
 
  XMLFILEIN=$line.xml
  XMLFILEOUT=${line}_nightmode.xml

  if [ -e $PNGFILEIN ]; then
    [ -e $PNGFILEOUT ] && rm $PNGFILEOUT
    magick $PNGFILEIN -channel RGB +level-colors "#b4b7ba", $PNGFILEOUT
  elif [ -e $XMLFILEIN ]; then
    [ -e $XMLFILEOUT ] && rm $XMLFILEOUT
    cat $XMLFILEIN | sed s/\#FF000000/#FFB4B7BA/ > $XMLFILEOUT
  else
    echo $FILEIN not found!
  fi
done < "list.txt"
