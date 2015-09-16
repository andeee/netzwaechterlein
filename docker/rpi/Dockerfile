FROM resin/rpi-node:0.10.36

RUN set -x \
    && apt-get update \
    && apt-get install -y --no-install-recommends python build-essential \
    && npm install net-ping ws express serve-static \
    && apt-get remove python build-essential \
    && apt-get autoremove

COPY resources/public/index.html /resources/public/
COPY resources/public/js/client.js /resources/public/js/
COPY target/backend/server.js /netzwaechterlein/

EXPOSE 8080
EXPOSE 8081

CMD ["node", "/netzwaechterlein/server.js"]