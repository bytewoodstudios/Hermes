#!/bin/bash
set -e

chmod -R 0555 /home/ftpusers/hermes

pure-pw useradd hermes -u ftpuser -d /home/ftpusers/hermes < /home/ftpusers/hermes/hermespasswords.tmp > dev/null
pure-pw mkdb

rm /home/ftpusers/hermes/hermespasswords.tmp

echo "connect to FTP using hermes/hermes on {DOCKER_MACHINE_IP}:21"

/usr/sbin/pure-ftpd -c 50 -C 10 -l puredb:/etc/pure-ftpd/pureftpd.pdb -E -j -R -P $PUBLICHOST -p 30000:30009