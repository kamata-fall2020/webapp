#!/bin/bash
cd /home/ec2-user/server
sudo java -jar webapp-0.0.1-SNAPSHOT.war > /dev/null 2> /dev/null < /dev/null &