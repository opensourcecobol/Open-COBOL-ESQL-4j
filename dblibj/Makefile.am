MAIN_SRC_FILES = \
    ./src/main/scala/Common.scala \
    ./src/main/scala/Connect.scala \
    ./src/main/scala/ConnectionInfo.scala \
    ./src/main/scala/ConstValues.scala \
    ./src/main/scala/Cursor.scala \
    ./src/main/scala/DBtype.scala \
    ./src/main/scala/Disconnect.scala \
    ./src/main/scala/GlobalState.scala \
    ./src/main/scala/Main.scala \
    ./src/main/scala/OCDBState.scala \
    ./src/main/scala/OccursInfo.scala \
    ./src/main/scala/Operation.scala \
    ./src/main/scala/Prepare.scala \
    ./src/main/scala/PreparedStatementCache.scala \
    ./src/main/scala/QueryInfo.scala \
    ./src/main/scala/SQLVar.scala \
    ./src/main/scala/Select.scala \
    ./src/main/scala/SqlCA.scala

TEST_SRC_FILES = \
    ./src/test/scala/ConnectTest.scala

SETTING_FILES = \
    build.sbt \
    scalastyle-config.xml

JAR_DEPENDENCIES = \
    lib/libcobj.jar \
    lib/postgresql.jar

TARGET_JAR = target/scala-2.13/ocesql4j.jar

all: $(TARGET_JAR)

clean:
	rm -rf $(TARGET_JAR)

$(TARGET_JAR): $(MAIN_SRC_FILES) $(TEST_SRC_FILES) $(SETTING_FILES) $(JAR_DEPENDENCIES)
	sbt assembly

install: $(TARGET_JAR)
	mkdir -p $(DESTDIR)$(prefix)/lib/Open-COBOL-ESQL-4j/
	install $(TARGET_JAR) $(DESTDIR)$(prefix)/lib/Open-COBOL-ESQL-4j/

.PHONY: all install clean
