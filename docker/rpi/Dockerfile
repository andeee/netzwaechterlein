FROM resin/rpi-node:0.10.36

RUN set -x \
    && npm install --no-optional net-ping ws express serve-static sqlite3

COPY resources/public/index.html /resources/public/
COPY resources/public/js/client.js /resources/public/js/
COPY target/backend/server.js /netzwaechterlein/

EXPOSE 8080
EXPOSE 8081

CMD ["node", "/netzwaechterlein/server.js"]