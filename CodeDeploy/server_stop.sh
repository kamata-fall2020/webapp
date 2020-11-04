#!/bin/bash

pkill -f 'java -jar'
sudo rm -rf target/
sudo rm -rf CodeDeploy/
sudo rm -f appspec.yml
