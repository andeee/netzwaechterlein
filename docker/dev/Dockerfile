FROM node:0.12

RUN set -x \
    && npm install --no-optional net-ping ws express serve-static

COPY resources/public/index.html /resources/public/
COPY resources/public/js/client.js /resources/public/js/
COPY target/backend/server.js /netzwaechterlein/

EXPOSE 8080
EXPOSE 8081

CMD ["node", "/netzwaechterlein/server.js"]