FROM node:0.12

RUN set -x \
    && npm install --no-optional net-ping ws express serve-static sqlite3

COPY resources/public/index.html /resources/public/
COPY resources/public/css/bootstrap.css /resources/public/css
COPY resources/public/js/client.js /resources/public/js/
COPY target/backend/server.js /netzwaechterlein/

EXPOSE 8080
EXPOSE 8081

CMD ["node", "/netzwaechterlein/server.js"]