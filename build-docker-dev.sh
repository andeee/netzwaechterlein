#!/bin/sh
lein cljsbuild once backend frontend
docker build -f docker/dev/Dockerfile -t andeee/netzwaechterlein:latest .
