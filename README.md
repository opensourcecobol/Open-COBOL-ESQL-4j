# Open COBOL ESQL 4J

Open COBOL ESQL 4J (OCESQL 4J) consits of open-source Embedded SQL pre-compiler and run time libraries for [opensource COBOL 4J](https://github.com/opensourcecobol/opensourcecobol4j).


# Requirements

* Open-source database.
  OCESQL 4J currently supports PostgreSQL database only.

* [opensource COBOL 4j](https://github.com/opensourcecobol/opensourcecobol4j) v1.0.5 or later.

* [sbt](https://www.scala-sbt.org/)(Optional).


# Installation

## Install the pre-compiler:

Run the following commands.

```sh
./configure --prefix=/usr/
make
make install
```

## Install runtime libraries:

### JDBC driver

Download [the PostgreSQL JDBC driver](https://jdbc.postgresql.org/download.html) and add the path to $CLASSPATH.

### ocesql4j.jar

Download the runtime library ocesql4j.jar in [Release page](https://github.com/opensourcecobol/Open-COBOL-ESQL-4j/releases) and add the path to $CLASSPATH.

#### Build ocesql4j.jar from source codes (Optional)

1. Put the libcobj.jar (runtime library of [opensource COBOL 4j](https://github.com/opensourcecobol/opensourcecobol4j)) into dblibj/lib/.
2. Move to dblibj directory and launch [sbt](https://www.scala-sbt.org/).
3. Run assembly command to create dblibj/target/scala-2.13/ocesql4j.jar.


# Usage

Usage manuals is under construction.
See test cases or sample programs.


# TODO

- [ ] Support other COBOL data types (COMP3, SIGN LEADING, ... etc).
- [ ] Create docker images.
