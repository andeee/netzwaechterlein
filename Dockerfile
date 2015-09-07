FROM hypriot/node-rpi:0.10.36

RUN set -x \
    && apt-get update \
    && apt-get install -y --no-install-recommends python build-essential \
    && npm install net-ping every-moment \
    && apt-get remove python build-essential \
    && apt-get autoremove