#!/bin/sh
lein do clean, cljsbuild once backend frontend
docker build -f docker/dev/Dockerfile --no-cache=true -t andeee/netzwaechterlein:latest .
