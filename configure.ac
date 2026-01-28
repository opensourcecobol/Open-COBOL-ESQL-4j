#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ([2.69])
AC_INIT([Open COBOL ESQL 4J], [1.1.3], [opencobol@tsh-world.co.jp])
AC_CONFIG_SRCDIR([config.h.in])
AC_CONFIG_HEADERS([config.h])

# Autoheader templates
AH_TEMPLATE([I18N_UTF8], [Enable UTF-8 support])

AM_INIT_AUTOMAKE

# Checks for programs.
AC_PROG_AWK
AC_PROG_CC
AC_PROG_CPP
AC_PROG_INSTALL
AC_PROG_LN_S
AC_PROG_MAKE_SET
AC_PROG_RANLIB
AC_PROG_YACC
AC_PROG_CC
AC_PROG_LEX

# Configure options.
GEN_DEBUG_OPT="no"
AC_ARG_ENABLE(debug,
  [  --enable-debug          (OCESQL) enable -g C compiler debug option],
  [if test "$enable_debug" = "yes"
   then
        GEN_DEBUG_OPT="yes"
   fi])

if test "$GEN_DEBUG_OPT" = "no"
then
        CFLAGS=`echo "$CFLAGS" | sed -e 's/^-g//' -e 's/-g$//' -e 's/-g / /'`
fi

DISABLE_COMPILER=no
AC_ARG_ENABLE(precompiler,
  [  --disable-precompiler   (OCESQL) disable OCESQL precompiler install],
  [if test "$enable_precompiler" = "no"
   then
        DISABLE_COMPILER=yes
   fi])

AM_CONDITIONAL(DISABLE_OCESQL, test $DISABLE_COMPILER = yes)

# UTF-8 support
AC_ARG_ENABLE(utf8,
  [  --enable-utf8           enable UTF-8 support.],
  [if test "$enable_utf8" = "yes"
   then
	AC_DEFINE(I18N_UTF8)
	COB_M4_FLAGS=-DM4.I18N_UTF8
   fi])
AM_CONDITIONAL(I18N_UTF8, test x$COB_M4_FLAGS = x-DM4.I18N_UTF8)
AC_SUBST(COB_M4_FLAGS)

# Checks for libraries.
#AC_CHECK_LIB([pq], [PQconnectdb])

# Checks for header files.
AC_FUNC_ALLOCA
AC_CHECK_HEADERS([inttypes.h libintl.h locale.h malloc.h stddef.h stdlib.h string.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_CHECK_HEADER_STDBOOL
AC_TYPE_INT16_T
AC_TYPE_INT32_T
AC_TYPE_INT8_T
AC_TYPE_PID_T
AC_TYPE_SIZE_T
AC_TYPE_UINT16_T
AC_TYPE_UINT32_T
AC_TYPE_UINT8_T

# Checks for library functions.
AC_FUNC_MALLOC
AC_FUNC_REALLOC
AC_CHECK_FUNCS([memmove memset setenv strcasecmp strchr strdup strrchr strstr])

AC_CONFIG_FILES([Makefile
                 ocesql/Makefile
                 tests/Makefile
                 dblibj/Makefile])
AC_OUTPUT
