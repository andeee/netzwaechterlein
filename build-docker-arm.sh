#!/bin/sh
lein cljsbuild once backend frontend
rsync -av -e 'ssh -p2200' . root@localhost:netzwaechterlein
ssh -p2200 root@localhost "cd netzwaechterlein; docker build -f docker/rpi/Dockerfile -t andeee/rpi-netzwaechterlein:latest ."
