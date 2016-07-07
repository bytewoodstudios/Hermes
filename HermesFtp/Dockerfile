FROM stilliard/pure-ftpd

MAINTAINER "Rainer Kern <ray@bytewood.com>"
MAINTAINER "Marco Silva <marco.silva@bytewood.com>"

RUN mkdir /home/ftpusers/hermes

COPY src/test/resources/ftpRoot /home/ftpusers/hermes/
COPY src/test/resources/hermespasswords.tmp /home/ftpusers/hermes/hermespasswords.tmp

ADD src/test/resources/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

