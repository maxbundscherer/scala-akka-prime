echo "SET SBT OPTS"
export SBT_OPTS="-Xss2M -Xms4G -Xmx15G"

echo "RUN SBT"
sbt run >> log.txt

echo "SHUTDOWN SERVER"
sudo shutdown now
