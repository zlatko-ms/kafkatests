#!/bin/bash
grep CSVSTATS $1  | cut -f 1,2,9- -d' ' | tr -d " [:blank:]" | sed 's/CSVSTATS;//'
