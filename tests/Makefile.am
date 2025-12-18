if I18N_UTF8
TESTS = basic \
	cobol_data \
	sql_data \
	sqlca \
	cobol4j_utf8 \
	ocesql_utf8 \
	misc
else
TESTS = basic \
	cobol_data \
	sql_data \
	sqlca \
	misc
endif

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
	cobol_data.src/comp3_signed.at \
	cobol_data.src/varying.at

sql_data_DEPENDENCIES = \
	sql_data.src/sql_type.at

sqlca_DEPENDENCIES = \
	sqlca.src/connect-disconnect.at \
	sqlca.src/insert-select-update-delete.at \
	sqlca.src/open-fetch-close.at \
	sqlca.src/prepare-execute.at \
	sqlca.src/fetch-loop.at \
	sqlca.src/errml-errmc.at

cobol4j_utf8_DEPENDENCIES = \
	cobol4j_utf8.src/compare-national-diff-size.at \
	cobol4j_utf8.src/compare-national.at \
	cobol4j_utf8.src/error-print.at \
	cobol4j_utf8.src/file-handler-japanese.at \
	cobol4j_utf8.src/japanese-char-section-var.at \
	cobol4j_utf8.src/java-interface.at \
	cobol4j_utf8.src/limits.at \
	cobol4j_utf8.src/mb-space.at \
	cobol4j_utf8.src/national.at \
	cobol4j_utf8.src/pic-n.at \
	cobol4j_utf8.src/pic-x.at \
	cobol4j_utf8.src/program-id.at \
	cobol4j_utf8.src/user-defined-word.at

ocesql_utf8_DEPENDENCIES = \
	ocesql_utf8.src/cursor.at \
	ocesql_utf8.src/host-variable.at \
	ocesql_utf8.src/include.at \
	ocesql_utf8.src/japanese.at \
	ocesql_utf8.src/long-sql.at \
	ocesql_utf8.src/prepare.at \
	ocesql_utf8.src/sample.at \
	ocesql_utf8.src/sql-convert.at \
	ocesql_utf8.src/varying.at

misc_DEPENDENCIES = \
	misc.src/long-sql.at \
	misc.src/include.at \
	misc.src/fetch-sqlca.at \
	misc.src/group-sub.at \
	misc.src/compile.at \
	misc.src/sql-convert.at

EXTRA_DIST = $(srcdir)/package.m4 \
	$(TESTS) \
	$(basic_DEPENDENCIES) \
	$(cobol_data_DEPENDENCIES) \
	$(sql_data_DEPENDENCIES) \
	$(sqlca_DEPENDENCIES) \
	$(cobol4j_utf8_DEPENDENCIES) \
	$(ocesql_utf8_DEPENDENCIES) \
	$(misc_DEPENDENCIES)

DISTCLEANFILES = atconfig

all: $(TESTS)

clean-local:
	rm -rf $(TESTS)

test: $(TESTS)
	@for TEST_SCRIPT in $(TESTS); do \
		./$${TEST_SCRIPT}; \
	done

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
$(srcdir)/cobol4j_utf8: $(cobol4j_utf8_DEPENDENCIES)
$(srcdir)/ocesql_utf8: $(ocesql_utf8_DEPENDENCIES)
$(srcdir)/misc: $(misc_DEPENDENCIES)
