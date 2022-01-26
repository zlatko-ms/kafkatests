#!/bin/bash

echo "DATE;TIME;KB/s;Evt/S;TotalEvents" > stats.csv
grep -v "∞" kafkatests.stats.log | sed 's/\./\,/g' >> stats.csv

