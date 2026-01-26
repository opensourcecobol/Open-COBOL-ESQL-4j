bin_PROGRAMS = ocesql
ocesql_SOURCES = errorfile.c ocesql.c ocesqlutil.c parser.y ppout.c scanner.l

BUILT_SOURCES = scanner.c parser.c parser.h
EXTRA_DIST = parser.y scanner.l.m4
CLEANFILES = parser.c parser.h scanner.c scanner.l

%.c: %.l
	flex --nounistd -s -o$@ $<

%.c: %.y
	bison -o $@ $<

%.l: %.l.m4
	m4 $(COB_M4_FLAGS) $< >$@

scanner.l: scanner.l.m4
