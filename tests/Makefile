MODULES=basic

.PHONY: clean all help

all: $(MODULES)

clean:
	rm -f $(MODULES)

help:
	@cat README

.SUFFIXES: .at

.at: package.m4 atlocal.in
	autom4te --language=autotest -I $@.src -o $@ $@.at
	chmod +x $@