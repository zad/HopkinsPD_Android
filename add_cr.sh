#!/bin/bash

echo $1
cat copyright.txt $1 >$1.new && mv $1.new $1

