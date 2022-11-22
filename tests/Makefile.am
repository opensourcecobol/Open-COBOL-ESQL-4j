TESTS = basic \
	cobol_data \
	sql_data \
	sqlca \
	misc

basic_DEPENDENCIES = \
	basic.src/commit-rollback.at \
	basic.src/connect-disconnect.at \
	basic.src/declare-bind-var.at \
	basic.src/declare.at \
	basic.src/delete.at \
	basic.src/fetch.at \
	basic.src/insert.at \
	basic.src/open-close.at \
	basic.src/other-sql.at \
	basic.src/prepare-execute.at \
	basic.src/select.at \
	basic.src/update.at

cobol_data_DEPENDENCIES = \
	cobol_data.src/alphanumeric.at \
	cobol_data.src/japanese.at \
	cobol_data.src/numeric_signed_v.at \
	cobol_data.src/numeric_v.at \
	cobol_data.src/comp3.at \
	cobol_data.src/comp3_signed.at

sql_data_DEPENDENCIES = \
	sql_data.src/sql_type.at

sqlca_DEPENDENCIES = \
	sqlca.src/connect-disconnect.at \
	sqlca.src/insert-select-update-delete.at \
	sqlca.src/open-fetch-close.at \
	sqlca.src/prepare-execute.at \
	sqlca.src/fetch-loop.at \
	sqlca.src/errml-errmc.at

misc_DEPENDENCIES = \
	misc.src/long-sql.at \
	misc.src/include.at \
	misc.src/fetch-sqlca.at

EXTRA_DIST = $(srcdir)/package.m4 \
	$(TESTS) \
	$(basic_DEPENDENCIES) \
	$(cobol_data_DEPENDENCIES) \
	$(sql_data_DEPENDENCIES) \
	$(sqlca_DEPENDENCIES)

DISTCLEANFILES = atconfig

all: $(TESTS)

clean-local:
	rm -rf $(TESTS)

$(srcdir)/package.m4: $(top_srcdir)/configure.ac
	@echo "creating $(srcdir)/package.m4"
	@{								   \
	  echo '# Signature of the current package.';			   \
	  echo 'm4_define([AT_PACKAGE_NAME],	  [@PACKAGE_NAME@])';	   \
	  echo 'm4_define([AT_PACKAGE_TARNAME],	  [@PACKAGE_TARNAME@])';   \
	  echo 'm4_define([AT_PACKAGE_VERSION],	  [@PACKAGE_VERSION@])';   \
	  echo 'm4_define([AT_PACKAGE_STRING],	  [@PACKAGE_STRING@])';    \
	  echo 'm4_define([AT_PACKAGE_BUGREPORT], [@PACKAGE_BUGREPORT@])'; \
	} > $(srcdir)/package.m4

$(srcdir)/%: $(srcdir)/%.at $(srcdir)/package.m4
	autom4te --language=autotest -I $(srcdir) -I $@.src -o $@ $<
	chmod +x $@

$(srcdir)/basic: $(basic_DEPENDENCIES)
$(srcdir)/cobol_data: $(cobol_data_DEPENDENCIES)
$(srcdir)/sql_data: $(sql_data_DEPENDENCIES)
$(srcdir)/sqlca: $(sqlca_DEPENDENCIES)
$(srcdir)/misc: $(misc_DEPENDENCIES)
