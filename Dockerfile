FROM alpine:3.14.0

ENV STARTUP_PATH=/opt/disqualified-officers-delta-consumer/disqualified-officers-delta-consumer.jar

RUN apk --no-cache add \
    bash \
    openjdk11 \
    curl

COPY disqualified-officers-delta-consumer.jar $STARTUP_PATH
COPY start.sh /usr/local/bin/

RUN chmod 555 /usr/local/bin/start.sh

HEALTHCHECK --interval=1m --timeout=10s --retries=3 --start-period=1m CMD curl --fail http://localhost:8081/order-notification-sender/healthcheck || exit 1

CMD ["start.sh"]