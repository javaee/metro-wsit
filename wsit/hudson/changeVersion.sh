#!/bin/sh
# sed/changeword

old=$1
new=$2
file=$3

for FIL in `find . -name $file -exec grep -l $old {} \;`
   do
       echo 'Replacing in '$FIL
       perl -i -pe "s|$old|$new|g" $FIL
   done 
