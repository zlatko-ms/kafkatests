#!/bin/bash
grep CSVSTATS kafkatests.log  | cut -f 1,2,9- -d' ' | tr -d " [:blank:]" | sed 's/CSVSTATS;//'
