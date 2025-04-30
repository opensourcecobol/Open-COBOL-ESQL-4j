#!/bin/bash

# Install opensource COBOL 4J
git submodule init
git submodule update
cd opensourcecobol4j/
./configure --prefix=/usr/
make
make install
cd ../

# Install Open COBOL ESQL 4J
cp /usr/lib/opensourcecobol4j/libcobj.jar dblibj/lib/
mkdir -p /usr/lib/Open-COBOL-ESQL-4j
curl -L -o /usr/lib/Open-COBOL-ESQL-4j/postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.2.24.jar
cp /usr/lib/Open-COBOL-ESQL-4j/postgresql.jar dblibj/lib/
./configure --prefix=/usr/
make
make install
