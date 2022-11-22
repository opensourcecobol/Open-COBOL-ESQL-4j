/* A Bison parser, made by GNU Bison 3.0.4.  */

/* Bison implementation for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2015 Free Software Foundation, Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

/* C LALR(1) parser skeleton written by Richard Stallman, by
   simplifying the original so-called "semantic" parser.  */

/* All symbols defined below should begin with yy or YY, to avoid
   infringing on user name space.  This should be done even for local
   variables, as they might otherwise be expanded by user macros.
   There are some unavoidable exceptions within include files to
   define necessary library symbols; they are noted "INFRINGES ON
   USER NAME SPACE" below.  */

/* Identify Bison output.  */
#define YYBISON 1

/* Bison version.  */
#define YYBISON_VERSION "3.0.4"

/* Skeleton name.  */
#define YYSKELETON_NAME "yacc.c"

/* Pure parsers.  */
#define YYPURE 0

/* Push parsers.  */
#define YYPUSH 0

/* Pull parsers.  */
#define YYPULL 1




/* Copy the first part of user declarations.  */
#line 21 "parser.y" /* yacc.c:339  */



#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "ocesql.h"
#include "ocesqlutil.h"

	static void put_exec_list();
	int cb_get_level(int level);
	struct cb_field * cb_build_field_tree(int level, char *name , struct cb_field *last_field);
	int build_picture (const char *str,struct cb_field * pic);
	int check_has_occurs_children(struct cb_field *field);
	int check_host_has_multi_occurs(struct cb_field *field);

	static struct cb_field		*current_field;
	static struct cb_field		*description_field;
	int hostreferenceCount = 0;

	int yyerror(const char *msg)
	{
	  	printmsg("%06d:%s\n", yylineno,msg);
		return 0;
	}



#line 96 "parser.c" /* yacc.c:339  */

# ifndef YY_NULLPTR
#  if defined __cplusplus && 201103L <= __cplusplus
#   define YY_NULLPTR nullptr
#  else
#   define YY_NULLPTR 0
#  endif
# endif

/* Enabling verbose error messages.  */
#ifdef YYERROR_VERBOSE
# undef YYERROR_VERBOSE
# define YYERROR_VERBOSE 1
#else
# define YYERROR_VERBOSE 0
#endif

/* In a future release of Bison, this section will be replaced
   by #include "y.tab.h".  */
#ifndef YY_YY_PARSER_H_INCLUDED
# define YY_YY_PARSER_H_INCLUDED
/* Debug traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif
#if YYDEBUG
extern int yydebug;
#endif

/* Token type.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
  enum yytokentype
  {
    SELECT = 258,
    SELECTFROM = 259,
    TOKEN = 260,
    CURNAME = 261,
    HOSTTOKEN = 262,
    WORD = 263,
    PICTURE = 264,
    INSERT = 265,
    UPDATE = 266,
    DISCONNECT = 267,
    DELETE = 268,
    EXECUTE = 269,
    OTHERFUNC = 270,
    INTO = 271,
    NUMERIC = 272,
    END_EXEC = 273,
    EXECSQL = 274,
    EXECSQL_INCLUDE = 275,
    PREPARE = 276,
    FROM = 277,
    DECLARE = 278,
    CURSOR = 279,
    FOR = 280,
    WORKINGBEGIN = 281,
    WORKINGEND = 282,
    HOSTVARIANTBEGIN = 283,
    HOSTVARIANTEND = 284,
    INCLUDE_FILE = 285,
    INCLUDE_SQLCA = 286,
    SQLCA = 287,
    IDENTIFIED_BY = 288,
    COMMIT_WORK = 289,
    ROLLBACK_WORK = 290,
    CONNECT = 291,
    USING = 292,
    OPEN = 293,
    CLOSE = 294,
    FETCH = 295,
    TRAILING = 296,
    COMP_1 = 297,
    COMP_2 = 298,
    COMP_3 = 299,
    USAGE = 300,
    SIGN = 301,
    LEADING = 302,
    SEPARATE = 303,
    AT = 304,
    IS = 305,
    ARE = 306,
    VALUE = 307,
    VARYING = 308,
    ALL = 309,
    OCCURS = 310,
    EXTERNAL = 311,
    TIMES = 312,
    CONST = 313,
    WHERECURRENTOF = 314
  };
#endif
/* Tokens.  */
#define SELECT 258
#define SELECTFROM 259
#define TOKEN 260
#define CURNAME 261
#define HOSTTOKEN 262
#define WORD 263
#define PICTURE 264
#define INSERT 265
#define UPDATE 266
#define DISCONNECT 267
#define DELETE 268
#define EXECUTE 269
#define OTHERFUNC 270
#define INTO 271
#define NUMERIC 272
#define END_EXEC 273
#define EXECSQL 274
#define EXECSQL_INCLUDE 275
#define PREPARE 276
#define FROM 277
#define DECLARE 278
#define CURSOR 279
#define FOR 280
#define WORKINGBEGIN 281
#define WORKINGEND 282
#define HOSTVARIANTBEGIN 283
#define HOSTVARIANTEND 284
#define INCLUDE_FILE 285
#define INCLUDE_SQLCA 286
#define SQLCA 287
#define IDENTIFIED_BY 288
#define COMMIT_WORK 289
#define ROLLBACK_WORK 290
#define CONNECT 291
#define USING 292
#define OPEN 293
#define CLOSE 294
#define FETCH 295
#define TRAILING 296
#define COMP_1 297
#define COMP_2 298
#define COMP_3 299
#define USAGE 300
#define SIGN 301
#define LEADING 302
#define SEPARATE 303
#define AT 304
#define IS 305
#define ARE 306
#define VALUE 307
#define VARYING 308
#define ALL 309
#define OCCURS 310
#define EXTERNAL 311
#define TIMES 312
#define CONST 313
#define WHERECURRENTOF 314

/* Value type.  */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED

union YYSTYPE
{
#line 51 "parser.y" /* yacc.c:355  */

	char *s;
	long int ld;
	struct cb_sql_list	*l;
	struct cb_hostreference_list *h;

#line 261 "parser.c" /* yacc.c:355  */
};

typedef union YYSTYPE YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define YYSTYPE_IS_DECLARED 1
#endif


extern YYSTYPE yylval;

int yyparse (void);

#endif /* !YY_YY_PARSER_H_INCLUDED  */

/* Copy the second part of user declarations.  */

#line 278 "parser.c" /* yacc.c:358  */

#ifdef short
# undef short
#endif

#ifdef YYTYPE_UINT8
typedef YYTYPE_UINT8 yytype_uint8;
#else
typedef unsigned char yytype_uint8;
#endif

#ifdef YYTYPE_INT8
typedef YYTYPE_INT8 yytype_int8;
#else
typedef signed char yytype_int8;
#endif

#ifdef YYTYPE_UINT16
typedef YYTYPE_UINT16 yytype_uint16;
#else
typedef unsigned short int yytype_uint16;
#endif

#ifdef YYTYPE_INT16
typedef YYTYPE_INT16 yytype_int16;
#else
typedef short int yytype_int16;
#endif

#ifndef YYSIZE_T
# ifdef __SIZE_TYPE__
#  define YYSIZE_T __SIZE_TYPE__
# elif defined size_t
#  define YYSIZE_T size_t
# elif ! defined YYSIZE_T
#  include <stddef.h> /* INFRINGES ON USER NAME SPACE */
#  define YYSIZE_T size_t
# else
#  define YYSIZE_T unsigned int
# endif
#endif

#define YYSIZE_MAXIMUM ((YYSIZE_T) -1)

#ifndef YY_
# if defined YYENABLE_NLS && YYENABLE_NLS
#  if ENABLE_NLS
#   include <libintl.h> /* INFRINGES ON USER NAME SPACE */
#   define YY_(Msgid) dgettext ("bison-runtime", Msgid)
#  endif
# endif
# ifndef YY_
#  define YY_(Msgid) Msgid
# endif
#endif

#ifndef YY_ATTRIBUTE
# if (defined __GNUC__                                               \
      && (2 < __GNUC__ || (__GNUC__ == 2 && 96 <= __GNUC_MINOR__)))  \
     || defined __SUNPRO_C && 0x5110 <= __SUNPRO_C
#  define YY_ATTRIBUTE(Spec) __attribute__(Spec)
# else
#  define YY_ATTRIBUTE(Spec) /* empty */
# endif
#endif

#ifndef YY_ATTRIBUTE_PURE
# define YY_ATTRIBUTE_PURE   YY_ATTRIBUTE ((__pure__))
#endif

#ifndef YY_ATTRIBUTE_UNUSED
# define YY_ATTRIBUTE_UNUSED YY_ATTRIBUTE ((__unused__))
#endif

#if !defined _Noreturn \
     && (!defined __STDC_VERSION__ || __STDC_VERSION__ < 201112)
# if defined _MSC_VER && 1200 <= _MSC_VER
#  define _Noreturn __declspec (noreturn)
# else
#  define _Noreturn YY_ATTRIBUTE ((__noreturn__))
# endif
#endif

/* Suppress unused-variable warnings by "using" E.  */
#if ! defined lint || defined __GNUC__
# define YYUSE(E) ((void) (E))
#else
# define YYUSE(E) /* empty */
#endif

#if defined __GNUC__ && 407 <= __GNUC__ * 100 + __GNUC_MINOR__
/* Suppress an incorrect diagnostic about yylval being uninitialized.  */
# define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN \
    _Pragma ("GCC diagnostic push") \
    _Pragma ("GCC diagnostic ignored \"-Wuninitialized\"")\
    _Pragma ("GCC diagnostic ignored \"-Wmaybe-uninitialized\"")
# define YY_IGNORE_MAYBE_UNINITIALIZED_END \
    _Pragma ("GCC diagnostic pop")
#else
# define YY_INITIAL_VALUE(Value) Value
#endif
#ifndef YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# define YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
# define YY_IGNORE_MAYBE_UNINITIALIZED_END
#endif
#ifndef YY_INITIAL_VALUE
# define YY_INITIAL_VALUE(Value) /* Nothing. */
#endif


#if ! defined yyoverflow || YYERROR_VERBOSE

/* The parser invokes alloca or malloc; define the necessary symbols.  */

# ifdef YYSTACK_USE_ALLOCA
#  if YYSTACK_USE_ALLOCA
#   ifdef __GNUC__
#    define YYSTACK_ALLOC __builtin_alloca
#   elif defined __BUILTIN_VA_ARG_INCR
#    include <alloca.h> /* INFRINGES ON USER NAME SPACE */
#   elif defined _AIX
#    define YYSTACK_ALLOC __alloca
#   elif defined _MSC_VER
#    include <malloc.h> /* INFRINGES ON USER NAME SPACE */
#    define alloca _alloca
#   else
#    define YYSTACK_ALLOC alloca
#    if ! defined _ALLOCA_H && ! defined EXIT_SUCCESS
#     include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
      /* Use EXIT_SUCCESS as a witness for stdlib.h.  */
#     ifndef EXIT_SUCCESS
#      define EXIT_SUCCESS 0
#     endif
#    endif
#   endif
#  endif
# endif

# ifdef YYSTACK_ALLOC
   /* Pacify GCC's 'empty if-body' warning.  */
#  define YYSTACK_FREE(Ptr) do { /* empty */; } while (0)
#  ifndef YYSTACK_ALLOC_MAXIMUM
    /* The OS might guarantee only one guard page at the bottom of the stack,
       and a page size can be as small as 4096 bytes.  So we cannot safely
       invoke alloca (N) if N exceeds 4096.  Use a slightly smaller number
       to allow for a few compiler-allocated temporary stack slots.  */
#   define YYSTACK_ALLOC_MAXIMUM 4032 /* reasonable circa 2006 */
#  endif
# else
#  define YYSTACK_ALLOC YYMALLOC
#  define YYSTACK_FREE YYFREE
#  ifndef YYSTACK_ALLOC_MAXIMUM
#   define YYSTACK_ALLOC_MAXIMUM YYSIZE_MAXIMUM
#  endif
#  if (defined __cplusplus && ! defined EXIT_SUCCESS \
       && ! ((defined YYMALLOC || defined malloc) \
             && (defined YYFREE || defined free)))
#   include <stdlib.h> /* INFRINGES ON USER NAME SPACE */
#   ifndef EXIT_SUCCESS
#    define EXIT_SUCCESS 0
#   endif
#  endif
#  ifndef YYMALLOC
#   define YYMALLOC malloc
#   if ! defined malloc && ! defined EXIT_SUCCESS
void *malloc (YYSIZE_T); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
#  ifndef YYFREE
#   define YYFREE free
#   if ! defined free && ! defined EXIT_SUCCESS
void free (void *); /* INFRINGES ON USER NAME SPACE */
#   endif
#  endif
# endif
#endif /* ! defined yyoverflow || YYERROR_VERBOSE */


#if (! defined yyoverflow \
     && (! defined __cplusplus \
         || (defined YYSTYPE_IS_TRIVIAL && YYSTYPE_IS_TRIVIAL)))

/* A type that is properly aligned for any stack member.  */
union yyalloc
{
  yytype_int16 yyss_alloc;
  YYSTYPE yyvs_alloc;
};

/* The size of the maximum gap between one aligned stack and the next.  */
# define YYSTACK_GAP_MAXIMUM (sizeof (union yyalloc) - 1)

/* The size of an array large to enough to hold all stacks, each with
   N elements.  */
# define YYSTACK_BYTES(N) \
     ((N) * (sizeof (yytype_int16) + sizeof (YYSTYPE)) \
      + YYSTACK_GAP_MAXIMUM)

# define YYCOPY_NEEDED 1

/* Relocate STACK from its old location to the new one.  The
   local variables YYSIZE and YYSTACKSIZE give the old and new number of
   elements in the stack, and YYPTR gives the new location of the
   stack.  Advance YYPTR to a properly aligned location for the next
   stack.  */
# define YYSTACK_RELOCATE(Stack_alloc, Stack)                           \
    do                                                                  \
      {                                                                 \
        YYSIZE_T yynewbytes;                                            \
        YYCOPY (&yyptr->Stack_alloc, Stack, yysize);                    \
        Stack = &yyptr->Stack_alloc;                                    \
        yynewbytes = yystacksize * sizeof (*Stack) + YYSTACK_GAP_MAXIMUM; \
        yyptr += yynewbytes / sizeof (*yyptr);                          \
      }                                                                 \
    while (0)

#endif

#if defined YYCOPY_NEEDED && YYCOPY_NEEDED
/* Copy COUNT objects from SRC to DST.  The source and destination do
   not overlap.  */
# ifndef YYCOPY
#  if defined __GNUC__ && 1 < __GNUC__
#   define YYCOPY(Dst, Src, Count) \
      __builtin_memcpy (Dst, Src, (Count) * sizeof (*(Src)))
#  else
#   define YYCOPY(Dst, Src, Count)              \
      do                                        \
        {                                       \
          YYSIZE_T yyi;                         \
          for (yyi = 0; yyi < (Count); yyi++)   \
            (Dst)[yyi] = (Src)[yyi];            \
        }                                       \
      while (0)
#  endif
# endif
#endif /* !YYCOPY_NEEDED */

/* YYFINAL -- State number of the termination state.  */
#define YYFINAL  2
/* YYLAST -- Last index in YYTABLE.  */
#define YYLAST   219

/* YYNTOKENS -- Number of terminals.  */
#define YYNTOKENS  61
/* YYNNTS -- Number of nonterminals.  */
#define YYNNTS  62
/* YYNRULES -- Number of rules.  */
#define YYNRULES  126
/* YYNSTATES -- Number of states.  */
#define YYNSTATES  199

/* YYTRANSLATE[YYX] -- Symbol number corresponding to YYX as returned
   by yylex, with out-of-bounds checking.  */
#define YYUNDEFTOK  2
#define YYMAXUTOK   314

#define YYTRANSLATE(YYX)                                                \
  ((unsigned int) (YYX) <= YYMAXUTOK ? yytranslate[YYX] : YYUNDEFTOK)

/* YYTRANSLATE[TOKEN-NUM] -- Symbol number corresponding to TOKEN-NUM
   as returned by yylex, without out-of-bounds checking.  */
static const yytype_uint8 yytranslate[] =
{
       0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,    60,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54,
      55,    56,    57,    58,    59
};

#if YYDEBUG
  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
static const yytype_uint16 yyrline[] =
{
       0,   122,   122,   123,   124,   125,   126,   127,   128,   129,
     130,   131,   132,   133,   134,   135,   136,   137,   138,   139,
     140,   144,   153,   157,   165,   168,   177,   180,   188,   189,
     194,   199,   206,   210,   213,   214,   215,   218,   219,   220,
     223,   229,   233,   238,   239,   242,   247,   248,   249,   250,
     253,   259,   264,   269,   274,   279,   284,   289,   292,   297,
     303,   310,   311,   314,   317,   320,   323,   326,   327,   328,
     331,   338,   340,   341,   342,   343,   345,   345,   358,   359,
     360,   361,   362,   363,   364,   368,   368,   383,   383,   401,
     402,   407,   408,   409,   410,   411,   412,   413,   417,   421,
     422,   426,   427,   428,   429,   433,   447,   449,   450,   451,
     454,   458,   464,   465,   467,   468,   472,   479,   482,   482,
     483,   483,   483,   484,   484,   485,   485
};
#endif

#if YYDEBUG || YYERROR_VERBOSE || 0
/* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
   First, the terminals, then, starting at YYNTOKENS, nonterminals.  */
static const char *const yytname[] =
{
  "$end", "error", "$undefined", "SELECT", "SELECTFROM", "TOKEN",
  "CURNAME", "HOSTTOKEN", "WORD", "PICTURE", "INSERT", "UPDATE",
  "DISCONNECT", "DELETE", "EXECUTE", "OTHERFUNC", "INTO", "NUMERIC",
  "END_EXEC", "EXECSQL", "EXECSQL_INCLUDE", "PREPARE", "FROM", "DECLARE",
  "CURSOR", "FOR", "WORKINGBEGIN", "WORKINGEND", "HOSTVARIANTBEGIN",
  "HOSTVARIANTEND", "INCLUDE_FILE", "INCLUDE_SQLCA", "SQLCA",
  "IDENTIFIED_BY", "COMMIT_WORK", "ROLLBACK_WORK", "CONNECT", "USING",
  "OPEN", "CLOSE", "FETCH", "TRAILING", "COMP_1", "COMP_2", "COMP_3",
  "USAGE", "SIGN", "LEADING", "SEPARATE", "AT", "IS", "ARE", "VALUE",
  "VARYING", "ALL", "OCCURS", "EXTERNAL", "TIMES", "CONST",
  "WHERECURRENTOF", "'.'", "$accept", "sqlstate_list", "sqlstate",
  "updatesql", "update", "disconnectsql", "disconnect", "deletesql",
  "delete", "insertsql", "insert", "rollbacksql", "commitsql", "fetchsql",
  "fetch", "host_references", "res_host_references", "closesql", "opensql",
  "otherdb", "dbid", "connectsql", "othersql", "connect", "identified",
  "using", "incfile", "includesql", "preparesql", "execsql",
  "selectintosql", "declaresql", "prepared_stname", "statement_id",
  "select", "declare_for", "token_list", "host_reference", "expr",
  "sqlvariantstates", "$@1", "sqlvariantstate_list", "sqlvariantstate",
  "$@2", "$@3", "data_description_clause_sequence",
  "data_description_clause", "picture_clause", "usage_clause", "usage",
  "varying_clause", "value_clause", "const_clause", "sign_clause",
  "_sign_is", "flag_separate", "occurs_clause", "external_clause", "_is",
  "_is_are", "_all", "_times", YY_NULLPTR
};
#endif

# ifdef YYPRINT
/* YYTOKNUM[NUM] -- (External) token number corresponding to the
   (internal) symbol number NUM (which must be that of a token).  */
static const yytype_uint16 yytoknum[] =
{
       0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
     275,   276,   277,   278,   279,   280,   281,   282,   283,   284,
     285,   286,   287,   288,   289,   290,   291,   292,   293,   294,
     295,   296,   297,   298,   299,   300,   301,   302,   303,   304,
     305,   306,   307,   308,   309,   310,   311,   312,   313,   314,
      46
};
# endif

#define YYPACT_NINF -45

#define yypact_value_is_default(Yystate) \
  (!!((Yystate) == (-45)))

#define YYTABLE_NINF -119

#define yytable_value_is_error(Yytable_value) \
  0

  /* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
     STATE-NUM.  */
static const yytype_int16 yypact[] =
{
     -45,   102,   -45,   -30,   -15,   -45,   -45,   -45,   -45,   -45,
     -45,   -45,   -45,   -45,   -45,   -45,   -45,   -45,   -45,   -45,
     -45,   -45,   -45,   -45,     2,    13,   179,   -26,    17,   -45,
     -45,    13,    27,   -45,   -45,    40,   139,   -45,   -45,   -45,
     -45,    78,   139,    78,   139,    82,    86,   139,   139,   139,
     139,   139,   139,   132,    96,   149,   122,   112,    85,   -45,
     142,   -45,   -45,   122,   123,   -45,   -45,   -45,   -45,    11,
     -45,   -45,    25,    39,   124,   125,   -45,   -45,    31,   135,
     -45,    49,    68,    92,   -45,   108,   122,   139,   137,   138,
     -45,   -45,   126,   152,     1,   -45,   -45,   -45,   -45,   -45,
     -45,   101,   -45,   122,   160,   -45,   -45,   -45,   122,   -45,
     161,   153,   -45,   122,   -45,   -45,   -45,   -45,   -45,    54,
     -45,    23,   -45,   -45,   122,   162,   -45,   -45,   167,   -45,
      87,   -45,   118,   -45,   -45,   170,   -45,   140,   -45,   -45,
     -45,   -45,   -45,   -45,   131,   -45,   139,   -45,   -45,   -45,
     -45,   -45,   -45,   131,   -45,   -45,   -45,   -45,   -45,   129,
     145,   -45,    15,   -45,   180,   -45,   -45,   -45,   -45,   -45,
     -45,   -45,    37,   -45,   -45,   143,   113,    45,   -45,   -45,
     -45,   144,   146,   148,   148,   -45,   -45,   -45,   -45,     0,
     -45,   -45,   -45,   -45,   -45,   -45,   -45,   -45,   -45
};

  /* YYDEFACT[STATE-NUM] -- Default reduction number in state STATE-NUM.
     Performed when YYTABLE does not specify something else to do.  Zero
     means the default is an error.  */
static const yytype_uint8 yydefact[] =
{
       2,     0,     1,    43,     0,    76,     3,    18,    19,    17,
      16,    14,    13,    12,    11,    10,     7,    20,     6,     8,
       9,    15,     4,     5,    43,     0,     0,    43,     0,    78,
      71,     0,     0,    51,    45,    44,     0,    28,    22,    24,
      26,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,    43,    54,
       0,    44,    46,     0,     0,    73,    72,    75,    74,     0,
      67,    63,     0,     0,     0,     0,    31,    30,     0,     0,
      33,     0,     0,     0,    29,     0,     0,     0,     0,     0,
      52,    49,     0,    87,     0,    77,    83,    84,    79,    80,
      81,     0,    48,     0,     0,    69,    68,    58,     0,    50,
       0,     0,    41,     0,    40,    21,    23,    25,    27,     0,
      37,    65,    62,    61,     0,     0,    85,    89,     0,    82,
       0,    70,     0,    34,    64,     0,    66,     0,    38,    32,
      39,    53,    47,    89,    88,    55,     0,    60,    35,    57,
      36,    56,    42,    86,   104,    98,   101,   102,   103,   118,
     112,   119,   120,   105,     0,    90,    91,    92,    99,    97,
      95,    93,     0,    94,    96,     0,     0,     0,   113,   121,
     122,   123,   125,   114,   114,   117,    59,   100,   124,     0,
     126,   116,   115,   111,   110,   108,   107,   109,   106
};

  /* YYPGOTO[NTERM-NUM].  */
static const yytype_int16 yypgoto[] =
{
     -45,   -45,   -45,   -45,   -45,   -45,   -45,   -45,   -45,   -45,
     -45,   -45,   -45,   -45,   -45,    88,   103,   -45,   -45,    -3,
     173,   -45,   -45,   172,   -45,   -45,   150,   -45,   -45,   -45,
     -45,   -45,    26,   -45,   -45,   151,   -40,   -23,   -44,   -45,
     -45,   -45,   -45,   -45,   -45,    62,   -45,   -45,   -45,    32,
     -45,   -45,   -45,   -45,   -45,    24,   -45,   -45,    53,   -45,
     -45,   -45
};

  /* YYDEFGOTO[NTERM-NUM].  */
static const yytype_int16 yydefgoto[] =
{
      -1,     1,     6,     7,    50,     8,    51,     9,    52,    10,
      53,    11,    12,    13,    54,   132,   119,    14,    15,    26,
      35,    16,    17,    27,    58,   125,    18,    99,    19,    20,
      21,    22,    72,   135,    89,    55,    69,   105,    70,    23,
      29,    60,   101,   143,   127,   144,   165,   166,   167,   168,
     169,   170,   198,   171,   172,   193,   173,   174,   175,   181,
     189,   191
};

  /* YYTABLE[YYPACT[STATE-NUM]] -- What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule whose
     number is the opposite.  If YYTABLE_NINF, syntax error.  */
static const yytype_int16 yytable[] =
{
      75,    33,    73,    78,    79,    80,    24,    56,   195,    30,
      81,    82,    83,    85,    65,    28,    66,   196,    30,    25,
      34,    32,    67,    31,    57,   106,    65,   103,    66,   106,
      30,    28,   128,    90,    67,    59,    68,   106,   106,   106,
      33,   106,    65,   107,    66,    62,    30,   121,    68,   112,
      67,    31,    65,   154,    66,    92,    30,   109,   197,   138,
      67,    30,   108,   120,    68,   179,   180,   115,   113,    74,
     104,    65,   139,    66,    68,    30,    63,   106,   183,    67,
     120,    88,   104,    71,   184,   133,   116,   156,   157,   158,
     133,   146,   138,    68,    30,    65,   140,    66,   104,    30,
      76,   141,     2,    67,    77,   147,   176,   140,   104,   150,
     117,    65,    86,    66,   150,    30,    65,    68,    66,    67,
      30,     3,     4,   148,    67,    30,   118,   104,     5,    30,
      91,   186,   106,    68,    31,    65,   149,    66,    68,   154,
     155,   102,    65,    67,    66,   148,   110,    30,    84,   111,
      67,   104,    87,   114,    71,   122,   123,    68,   152,    93,
     126,   129,    94,   124,    68,    44,   131,   104,   134,    95,
      96,    97,   104,   156,   157,   158,   159,   160,   136,   161,
     142,   161,    36,   162,   163,   145,   164,  -118,   151,    37,
      38,    39,    40,    41,    42,   178,   192,   182,   188,   185,
      43,   137,    44,   190,    61,   153,   130,    64,   194,   187,
      98,   100,   177,    45,    46,     0,     0,    47,    48,    49
};

static const yytype_int16 yycheck[] =
{
      44,    24,    42,    47,    48,    49,    36,    33,     8,     7,
      50,    51,    52,    53,     3,    30,     5,    17,     7,    49,
       7,    24,    11,    49,    27,    69,     3,    16,     5,    73,
       7,    30,    31,    56,    11,    18,    25,    81,    82,    83,
      63,    85,     3,    18,     5,    18,     7,    87,    25,    18,
      11,    49,     3,     8,     5,    58,     7,    18,    58,     5,
      11,     7,    37,    86,    25,    50,    51,    18,    37,    43,
      59,     3,    18,     5,    25,     7,    36,   121,    41,    11,
     103,    55,    59,     5,    47,   108,    18,    42,    43,    44,
     113,     4,     5,    25,     7,     3,   119,     5,    59,     7,
      18,   124,     0,    11,    18,    18,   146,   130,    59,   132,
      18,     3,    16,     5,   137,     7,     3,    25,     5,    11,
       7,    19,    20,     5,    11,     7,    18,    59,    26,     7,
      18,    18,   176,    25,    49,     3,    18,     5,    25,     8,
       9,    18,     3,    11,     5,     5,    22,     7,    16,    24,
      11,    59,     3,    18,     5,    18,    18,    25,    18,    17,
       8,    60,    20,    37,    25,    23,     6,    59,     7,    27,
      28,    29,    59,    42,    43,    44,    45,    46,    25,    50,
      18,    50,     3,    52,    53,    18,    55,    56,    18,    10,
      11,    12,    13,    14,    15,    50,    48,    17,    54,    56,
      21,   113,    23,    57,    31,   143,   103,    35,   184,   177,
      60,    60,   159,    34,    35,    -1,    -1,    38,    39,    40
};

  /* YYSTOS[STATE-NUM] -- The (internal number of the) accessing
     symbol of state STATE-NUM.  */
static const yytype_uint8 yystos[] =
{
       0,    62,     0,    19,    20,    26,    63,    64,    66,    68,
      70,    72,    73,    74,    78,    79,    82,    83,    87,    89,
      90,    91,    92,   100,    36,    49,    80,    84,    30,   101,
       7,    49,    80,    98,     7,    81,     3,    10,    11,    12,
      13,    14,    15,    21,    23,    34,    35,    38,    39,    40,
      65,    67,    69,    71,    75,    96,    33,    80,    85,    18,
     102,    81,    18,    36,    84,     3,     5,    11,    25,    97,
      99,     5,    93,    97,    93,    99,    18,    18,    99,    99,
      99,    97,    97,    97,    16,    97,    16,     3,    93,    95,
      98,    18,    80,    17,    20,    27,    28,    29,    87,    88,
      96,   103,    18,    16,    59,    98,    99,    18,    37,    18,
      22,    24,    18,    37,    18,    18,    18,    18,    18,    77,
      98,    97,    18,    18,    37,    86,     8,   105,    31,    60,
      77,     6,    76,    98,     7,    94,    25,    76,     5,    18,
      98,    98,    18,   104,   106,    18,     4,    18,     5,    18,
      98,    18,    18,   106,     8,     9,    42,    43,    44,    45,
      46,    50,    52,    53,    55,   107,   108,   109,   110,   111,
     112,   114,   115,   117,   118,   119,    97,   119,    50,    50,
      51,   120,    17,    41,    47,    56,    18,   110,    54,   121,
      57,   122,    48,   116,   116,     8,    17,    58,   113
};

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
static const yytype_uint8 yyr1[] =
{
       0,    61,    62,    62,    63,    63,    63,    63,    63,    63,
      63,    63,    63,    63,    63,    63,    63,    63,    63,    63,
      63,    64,    65,    66,    67,    68,    69,    70,    71,    71,
      72,    73,    74,    75,    76,    76,    76,    77,    77,    77,
      78,    79,    79,    80,    80,    81,    82,    82,    82,    82,
      83,    84,    85,    86,    87,    88,    89,    90,    90,    91,
      91,    92,    92,    93,    94,    95,    96,    97,    97,    97,
      97,    98,    99,    99,    99,    99,   101,   100,   102,   102,
     102,   102,   102,   102,   102,   104,   103,   105,   103,   106,
     106,   107,   107,   107,   107,   107,   107,   107,   108,   109,
     109,   110,   110,   110,   110,   111,   112,   113,   113,   113,
     114,   114,   115,   115,   116,   116,   117,   118,   119,   119,
     120,   120,   120,   121,   121,   122,   122
};

  /* YYR2[YYN] -- Number of symbols on the right hand side of rule YYN.  */
static const yytype_uint8 yyr2[] =
{
       0,     2,     0,     2,     1,     1,     1,     1,     1,     1,
       1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       1,     5,     1,     5,     1,     5,     1,     5,     1,     2,
       4,     4,     6,     2,     1,     2,     2,     1,     2,     2,
       5,     5,     7,     0,     2,     1,     4,     6,     5,     4,
       5,     2,     2,     2,     3,     3,     7,     7,     5,     9,
       7,     5,     5,     1,     1,     2,     4,     1,     2,     2,
       3,     1,     1,     1,     1,     1,     0,     4,     0,     2,
       2,     2,     3,     2,     2,     0,     4,     0,     3,     0,
       2,     1,     1,     1,     1,     1,     1,     1,     1,     1,
       3,     1,     1,     1,     1,     1,     4,     1,     1,     1,
       3,     3,     1,     2,     0,     1,     3,     2,     0,     1,
       0,     1,     1,     0,     1,     0,     1
};


#define yyerrok         (yyerrstatus = 0)
#define yyclearin       (yychar = YYEMPTY)
#define YYEMPTY         (-2)
#define YYEOF           0

#define YYACCEPT        goto yyacceptlab
#define YYABORT         goto yyabortlab
#define YYERROR         goto yyerrorlab


#define YYRECOVERING()  (!!yyerrstatus)

#define YYBACKUP(Token, Value)                                  \
do                                                              \
  if (yychar == YYEMPTY)                                        \
    {                                                           \
      yychar = (Token);                                         \
      yylval = (Value);                                         \
      YYPOPSTACK (yylen);                                       \
      yystate = *yyssp;                                         \
      goto yybackup;                                            \
    }                                                           \
  else                                                          \
    {                                                           \
      yyerror (YY_("syntax error: cannot back up")); \
      YYERROR;                                                  \
    }                                                           \
while (0)

/* Error token number */
#define YYTERROR        1
#define YYERRCODE       256



/* Enable debugging if requested.  */
#if YYDEBUG

# ifndef YYFPRINTF
#  include <stdio.h> /* INFRINGES ON USER NAME SPACE */
#  define YYFPRINTF fprintf
# endif

# define YYDPRINTF(Args)                        \
do {                                            \
  if (yydebug)                                  \
    YYFPRINTF Args;                             \
} while (0)

/* This macro is provided for backward compatibility. */
#ifndef YY_LOCATION_PRINT
# define YY_LOCATION_PRINT(File, Loc) ((void) 0)
#endif


# define YY_SYMBOL_PRINT(Title, Type, Value, Location)                    \
do {                                                                      \
  if (yydebug)                                                            \
    {                                                                     \
      YYFPRINTF (stderr, "%s ", Title);                                   \
      yy_symbol_print (stderr,                                            \
                  Type, Value); \
      YYFPRINTF (stderr, "\n");                                           \
    }                                                                     \
} while (0)


/*----------------------------------------.
| Print this symbol's value on YYOUTPUT.  |
`----------------------------------------*/

static void
yy_symbol_value_print (FILE *yyoutput, int yytype, YYSTYPE const * const yyvaluep)
{
  FILE *yyo = yyoutput;
  YYUSE (yyo);
  if (!yyvaluep)
    return;
# ifdef YYPRINT
  if (yytype < YYNTOKENS)
    YYPRINT (yyoutput, yytoknum[yytype], *yyvaluep);
# endif
  YYUSE (yytype);
}


/*--------------------------------.
| Print this symbol on YYOUTPUT.  |
`--------------------------------*/

static void
yy_symbol_print (FILE *yyoutput, int yytype, YYSTYPE const * const yyvaluep)
{
  YYFPRINTF (yyoutput, "%s %s (",
             yytype < YYNTOKENS ? "token" : "nterm", yytname[yytype]);

  yy_symbol_value_print (yyoutput, yytype, yyvaluep);
  YYFPRINTF (yyoutput, ")");
}

/*------------------------------------------------------------------.
| yy_stack_print -- Print the state stack from its BOTTOM up to its |
| TOP (included).                                                   |
`------------------------------------------------------------------*/

static void
yy_stack_print (yytype_int16 *yybottom, yytype_int16 *yytop)
{
  YYFPRINTF (stderr, "Stack now");
  for (; yybottom <= yytop; yybottom++)
    {
      int yybot = *yybottom;
      YYFPRINTF (stderr, " %d", yybot);
    }
  YYFPRINTF (stderr, "\n");
}

# define YY_STACK_PRINT(Bottom, Top)                            \
do {                                                            \
  if (yydebug)                                                  \
    yy_stack_print ((Bottom), (Top));                           \
} while (0)


/*------------------------------------------------.
| Report that the YYRULE is going to be reduced.  |
`------------------------------------------------*/

static void
yy_reduce_print (yytype_int16 *yyssp, YYSTYPE *yyvsp, int yyrule)
{
  unsigned long int yylno = yyrline[yyrule];
  int yynrhs = yyr2[yyrule];
  int yyi;
  YYFPRINTF (stderr, "Reducing stack by rule %d (line %lu):\n",
             yyrule - 1, yylno);
  /* The symbols being reduced.  */
  for (yyi = 0; yyi < yynrhs; yyi++)
    {
      YYFPRINTF (stderr, "   $%d = ", yyi + 1);
      yy_symbol_print (stderr,
                       yystos[yyssp[yyi + 1 - yynrhs]],
                       &(yyvsp[(yyi + 1) - (yynrhs)])
                                              );
      YYFPRINTF (stderr, "\n");
    }
}

# define YY_REDUCE_PRINT(Rule)          \
do {                                    \
  if (yydebug)                          \
    yy_reduce_print (yyssp, yyvsp, Rule); \
} while (0)

/* Nonzero means print parse trace.  It is left uninitialized so that
   multiple parsers can coexist.  */
int yydebug;
#else /* !YYDEBUG */
# define YYDPRINTF(Args)
# define YY_SYMBOL_PRINT(Title, Type, Value, Location)
# define YY_STACK_PRINT(Bottom, Top)
# define YY_REDUCE_PRINT(Rule)
#endif /* !YYDEBUG */


/* YYINITDEPTH -- initial size of the parser's stacks.  */
#ifndef YYINITDEPTH
# define YYINITDEPTH 200
#endif

/* YYMAXDEPTH -- maximum size the stacks can grow to (effective only
   if the built-in stack extension method is used).

   Do not make this value too large; the results are undefined if
   YYSTACK_ALLOC_MAXIMUM < YYSTACK_BYTES (YYMAXDEPTH)
   evaluated with infinite-precision integer arithmetic.  */

#ifndef YYMAXDEPTH
# define YYMAXDEPTH 10000
#endif


#if YYERROR_VERBOSE

# ifndef yystrlen
#  if defined __GLIBC__ && defined _STRING_H
#   define yystrlen strlen
#  else
/* Return the length of YYSTR.  */
static YYSIZE_T
yystrlen (const char *yystr)
{
  YYSIZE_T yylen;
  for (yylen = 0; yystr[yylen]; yylen++)
    continue;
  return yylen;
}
#  endif
# endif

# ifndef yystpcpy
#  if defined __GLIBC__ && defined _STRING_H && defined _GNU_SOURCE
#   define yystpcpy stpcpy
#  else
/* Copy YYSRC to YYDEST, returning the address of the terminating '\0' in
   YYDEST.  */
static char *
yystpcpy (char *yydest, const char *yysrc)
{
  char *yyd = yydest;
  const char *yys = yysrc;

  while ((*yyd++ = *yys++) != '\0')
    continue;

  return yyd - 1;
}
#  endif
# endif

# ifndef yytnamerr
/* Copy to YYRES the contents of YYSTR after stripping away unnecessary
   quotes and backslashes, so that it's suitable for yyerror.  The
   heuristic is that double-quoting is unnecessary unless the string
   contains an apostrophe, a comma, or backslash (other than
   backslash-backslash).  YYSTR is taken from yytname.  If YYRES is
   null, do not copy; instead, return the length of what the result
   would have been.  */
static YYSIZE_T
yytnamerr (char *yyres, const char *yystr)
{
  if (*yystr == '"')
    {
      YYSIZE_T yyn = 0;
      char const *yyp = yystr;

      for (;;)
        switch (*++yyp)
          {
          case '\'':
          case ',':
            goto do_not_strip_quotes;

          case '\\':
            if (*++yyp != '\\')
              goto do_not_strip_quotes;
            /* Fall through.  */
          default:
            if (yyres)
              yyres[yyn] = *yyp;
            yyn++;
            break;

          case '"':
            if (yyres)
              yyres[yyn] = '\0';
            return yyn;
          }
    do_not_strip_quotes: ;
    }

  if (! yyres)
    return yystrlen (yystr);

  return yystpcpy (yyres, yystr) - yyres;
}
# endif

/* Copy into *YYMSG, which is of size *YYMSG_ALLOC, an error message
   about the unexpected token YYTOKEN for the state stack whose top is
   YYSSP.

   Return 0 if *YYMSG was successfully written.  Return 1 if *YYMSG is
   not large enough to hold the message.  In that case, also set
   *YYMSG_ALLOC to the required number of bytes.  Return 2 if the
   required number of bytes is too large to store.  */
static int
yysyntax_error (YYSIZE_T *yymsg_alloc, char **yymsg,
                yytype_int16 *yyssp, int yytoken)
{
  YYSIZE_T yysize0 = yytnamerr (YY_NULLPTR, yytname[yytoken]);
  YYSIZE_T yysize = yysize0;
  enum { YYERROR_VERBOSE_ARGS_MAXIMUM = 5 };
  /* Internationalized format string. */
  const char *yyformat = YY_NULLPTR;
  /* Arguments of yyformat. */
  char const *yyarg[YYERROR_VERBOSE_ARGS_MAXIMUM];
  /* Number of reported tokens (one for the "unexpected", one per
     "expected"). */
  int yycount = 0;

  /* There are many possibilities here to consider:
     - If this state is a consistent state with a default action, then
       the only way this function was invoked is if the default action
       is an error action.  In that case, don't check for expected
       tokens because there are none.
     - The only way there can be no lookahead present (in yychar) is if
       this state is a consistent state with a default action.  Thus,
       detecting the absence of a lookahead is sufficient to determine
       that there is no unexpected or expected token to report.  In that
       case, just report a simple "syntax error".
     - Don't assume there isn't a lookahead just because this state is a
       consistent state with a default action.  There might have been a
       previous inconsistent state, consistent state with a non-default
       action, or user semantic action that manipulated yychar.
     - Of course, the expected token list depends on states to have
       correct lookahead information, and it depends on the parser not
       to perform extra reductions after fetching a lookahead from the
       scanner and before detecting a syntax error.  Thus, state merging
       (from LALR or IELR) and default reductions corrupt the expected
       token list.  However, the list is correct for canonical LR with
       one exception: it will still contain any token that will not be
       accepted due to an error action in a later state.
  */
  if (yytoken != YYEMPTY)
    {
      int yyn = yypact[*yyssp];
      yyarg[yycount++] = yytname[yytoken];
      if (!yypact_value_is_default (yyn))
        {
          /* Start YYX at -YYN if negative to avoid negative indexes in
             YYCHECK.  In other words, skip the first -YYN actions for
             this state because they are default actions.  */
          int yyxbegin = yyn < 0 ? -yyn : 0;
          /* Stay within bounds of both yycheck and yytname.  */
          int yychecklim = YYLAST - yyn + 1;
          int yyxend = yychecklim < YYNTOKENS ? yychecklim : YYNTOKENS;
          int yyx;

          for (yyx = yyxbegin; yyx < yyxend; ++yyx)
            if (yycheck[yyx + yyn] == yyx && yyx != YYTERROR
                && !yytable_value_is_error (yytable[yyx + yyn]))
              {
                if (yycount == YYERROR_VERBOSE_ARGS_MAXIMUM)
                  {
                    yycount = 1;
                    yysize = yysize0;
                    break;
                  }
                yyarg[yycount++] = yytname[yyx];
                {
                  YYSIZE_T yysize1 = yysize + yytnamerr (YY_NULLPTR, yytname[yyx]);
                  if (! (yysize <= yysize1
                         && yysize1 <= YYSTACK_ALLOC_MAXIMUM))
                    return 2;
                  yysize = yysize1;
                }
              }
        }
    }

  switch (yycount)
    {
# define YYCASE_(N, S)                      \
      case N:                               \
        yyformat = S;                       \
      break
      YYCASE_(0, YY_("syntax error"));
      YYCASE_(1, YY_("syntax error, unexpected %s"));
      YYCASE_(2, YY_("syntax error, unexpected %s, expecting %s"));
      YYCASE_(3, YY_("syntax error, unexpected %s, expecting %s or %s"));
      YYCASE_(4, YY_("syntax error, unexpected %s, expecting %s or %s or %s"));
      YYCASE_(5, YY_("syntax error, unexpected %s, expecting %s or %s or %s or %s"));
# undef YYCASE_
    }

  {
    YYSIZE_T yysize1 = yysize + yystrlen (yyformat);
    if (! (yysize <= yysize1 && yysize1 <= YYSTACK_ALLOC_MAXIMUM))
      return 2;
    yysize = yysize1;
  }

  if (*yymsg_alloc < yysize)
    {
      *yymsg_alloc = 2 * yysize;
      if (! (yysize <= *yymsg_alloc
             && *yymsg_alloc <= YYSTACK_ALLOC_MAXIMUM))
        *yymsg_alloc = YYSTACK_ALLOC_MAXIMUM;
      return 1;
    }

  /* Avoid sprintf, as that infringes on the user's name space.
     Don't have undefined behavior even if the translation
     produced a string with the wrong number of "%s"s.  */
  {
    char *yyp = *yymsg;
    int yyi = 0;
    while ((*yyp = *yyformat) != '\0')
      if (*yyp == '%' && yyformat[1] == 's' && yyi < yycount)
        {
          yyp += yytnamerr (yyp, yyarg[yyi++]);
          yyformat += 2;
        }
      else
        {
          yyp++;
          yyformat++;
        }
  }
  return 0;
}
#endif /* YYERROR_VERBOSE */

/*-----------------------------------------------.
| Release the memory associated to this symbol.  |
`-----------------------------------------------*/

static void
yydestruct (const char *yymsg, int yytype, YYSTYPE *yyvaluep)
{
  YYUSE (yyvaluep);
  if (!yymsg)
    yymsg = "Deleting";
  YY_SYMBOL_PRINT (yymsg, yytype, yyvaluep, yylocationp);

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  YYUSE (yytype);
  YY_IGNORE_MAYBE_UNINITIALIZED_END
}




/* The lookahead symbol.  */
int yychar;

/* The semantic value of the lookahead symbol.  */
YYSTYPE yylval;
/* Number of syntax errors so far.  */
int yynerrs;


/*----------.
| yyparse.  |
`----------*/

int
yyparse (void)
{
    int yystate;
    /* Number of tokens to shift before error messages enabled.  */
    int yyerrstatus;

    /* The stacks and their tools:
       'yyss': related to states.
       'yyvs': related to semantic values.

       Refer to the stacks through separate pointers, to allow yyoverflow
       to reallocate them elsewhere.  */

    /* The state stack.  */
    yytype_int16 yyssa[YYINITDEPTH];
    yytype_int16 *yyss;
    yytype_int16 *yyssp;

    /* The semantic value stack.  */
    YYSTYPE yyvsa[YYINITDEPTH];
    YYSTYPE *yyvs;
    YYSTYPE *yyvsp;

    YYSIZE_T yystacksize;

  int yyn;
  int yyresult;
  /* Lookahead token as an internal (translated) token number.  */
  int yytoken = 0;
  /* The variables used to return semantic value and location from the
     action routines.  */
  YYSTYPE yyval;

#if YYERROR_VERBOSE
  /* Buffer for error messages, and its allocated size.  */
  char yymsgbuf[128];
  char *yymsg = yymsgbuf;
  YYSIZE_T yymsg_alloc = sizeof yymsgbuf;
#endif

#define YYPOPSTACK(N)   (yyvsp -= (N), yyssp -= (N))

  /* The number of symbols on the RHS of the reduced rule.
     Keep to zero when no symbol should be popped.  */
  int yylen = 0;

  yyssp = yyss = yyssa;
  yyvsp = yyvs = yyvsa;
  yystacksize = YYINITDEPTH;

  YYDPRINTF ((stderr, "Starting parse\n"));

  yystate = 0;
  yyerrstatus = 0;
  yynerrs = 0;
  yychar = YYEMPTY; /* Cause a token to be read.  */
  goto yysetstate;

/*------------------------------------------------------------.
| yynewstate -- Push a new state, which is found in yystate.  |
`------------------------------------------------------------*/
 yynewstate:
  /* In all cases, when you get here, the value and location stacks
     have just been pushed.  So pushing a state here evens the stacks.  */
  yyssp++;

 yysetstate:
  *yyssp = yystate;

  if (yyss + yystacksize - 1 <= yyssp)
    {
      /* Get the current used size of the three stacks, in elements.  */
      YYSIZE_T yysize = yyssp - yyss + 1;

#ifdef yyoverflow
      {
        /* Give user a chance to reallocate the stack.  Use copies of
           these so that the &'s don't force the real ones into
           memory.  */
        YYSTYPE *yyvs1 = yyvs;
        yytype_int16 *yyss1 = yyss;

        /* Each stack pointer address is followed by the size of the
           data in use in that stack, in bytes.  This used to be a
           conditional around just the two extra args, but that might
           be undefined if yyoverflow is a macro.  */
        yyoverflow (YY_("memory exhausted"),
                    &yyss1, yysize * sizeof (*yyssp),
                    &yyvs1, yysize * sizeof (*yyvsp),
                    &yystacksize);

        yyss = yyss1;
        yyvs = yyvs1;
      }
#else /* no yyoverflow */
# ifndef YYSTACK_RELOCATE
      goto yyexhaustedlab;
# else
      /* Extend the stack our own way.  */
      if (YYMAXDEPTH <= yystacksize)
        goto yyexhaustedlab;
      yystacksize *= 2;
      if (YYMAXDEPTH < yystacksize)
        yystacksize = YYMAXDEPTH;

      {
        yytype_int16 *yyss1 = yyss;
        union yyalloc *yyptr =
          (union yyalloc *) YYSTACK_ALLOC (YYSTACK_BYTES (yystacksize));
        if (! yyptr)
          goto yyexhaustedlab;
        YYSTACK_RELOCATE (yyss_alloc, yyss);
        YYSTACK_RELOCATE (yyvs_alloc, yyvs);
#  undef YYSTACK_RELOCATE
        if (yyss1 != yyssa)
          YYSTACK_FREE (yyss1);
      }
# endif
#endif /* no yyoverflow */

      yyssp = yyss + yysize - 1;
      yyvsp = yyvs + yysize - 1;

      YYDPRINTF ((stderr, "Stack size increased to %lu\n",
                  (unsigned long int) yystacksize));

      if (yyss + yystacksize - 1 <= yyssp)
        YYABORT;
    }

  YYDPRINTF ((stderr, "Entering state %d\n", yystate));

  if (yystate == YYFINAL)
    YYACCEPT;

  goto yybackup;

/*-----------.
| yybackup.  |
`-----------*/
yybackup:

  /* Do appropriate processing given the current state.  Read a
     lookahead token if we need one and don't already have one.  */

  /* First try to decide what to do without reference to lookahead token.  */
  yyn = yypact[yystate];
  if (yypact_value_is_default (yyn))
    goto yydefault;

  /* Not known => get a lookahead token if don't already have one.  */

  /* YYCHAR is either YYEMPTY or YYEOF or a valid lookahead symbol.  */
  if (yychar == YYEMPTY)
    {
      YYDPRINTF ((stderr, "Reading a token: "));
      yychar = yylex ();
    }

  if (yychar <= YYEOF)
    {
      yychar = yytoken = YYEOF;
      YYDPRINTF ((stderr, "Now at end of input.\n"));
    }
  else
    {
      yytoken = YYTRANSLATE (yychar);
      YY_SYMBOL_PRINT ("Next token is", yytoken, &yylval, &yylloc);
    }

  /* If the proper action on seeing token YYTOKEN is to reduce or to
     detect an error, take that action.  */
  yyn += yytoken;
  if (yyn < 0 || YYLAST < yyn || yycheck[yyn] != yytoken)
    goto yydefault;
  yyn = yytable[yyn];
  if (yyn <= 0)
    {
      if (yytable_value_is_error (yyn))
        goto yyerrlab;
      yyn = -yyn;
      goto yyreduce;
    }

  /* Count tokens shifted since error; after three, turn off error
     status.  */
  if (yyerrstatus)
    yyerrstatus--;

  /* Shift the lookahead token.  */
  YY_SYMBOL_PRINT ("Shifting", yytoken, &yylval, &yylloc);

  /* Discard the shifted token.  */
  yychar = YYEMPTY;

  yystate = yyn;
  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END

  goto yynewstate;


/*-----------------------------------------------------------.
| yydefault -- do the default action for the current state.  |
`-----------------------------------------------------------*/
yydefault:
  yyn = yydefact[yystate];
  if (yyn == 0)
    goto yyerrlab;
  goto yyreduce;


/*-----------------------------.
| yyreduce -- Do a reduction.  |
`-----------------------------*/
yyreduce:
  /* yyn is the number of a rule to reduce with.  */
  yylen = yyr2[yyn];

  /* If YYLEN is nonzero, implement the default value of the action:
     '$$ = $1'.

     Otherwise, the following line sets YYVAL to garbage.
     This behavior is undocumented and Bison
     users should not rely upon it.  Assigning to YYVAL
     unconditionally makes the parser a bit smaller, and it avoids a
     GCC warning that YYVAL may be used uninitialized.  */
  yyval = yyvsp[1-yylen];


  YY_REDUCE_PRINT (yyn);
  switch (yyn)
    {
        case 21:
#line 146 "parser.y" /* yacc.c:1646  */
    {
	(yyval.l) = cb_add_text_list ((yyvsp[-2].l), (yyvsp[-1].l));
	put_exec_list();
}
#line 1527 "parser.c" /* yacc.c:1646  */
    break;

  case 22:
#line 153 "parser.y" /* yacc.c:1646  */
    {(yyval.l) = cb_text_list_add (NULL, (yyvsp[0].s));}
#line 1533 "parser.c" /* yacc.c:1646  */
    break;

  case 23:
#line 159 "parser.y" /* yacc.c:1646  */
    {
	(yyval.l) = cb_add_text_list ((yyvsp[-2].l), (yyvsp[-1].l));
	put_exec_list();
}
#line 1542 "parser.c" /* yacc.c:1646  */
    break;

  case 24:
#line 165 "parser.y" /* yacc.c:1646  */
    {(yyval.l) = cb_text_list_add (NULL, (yyvsp[0].s));}
#line 1548 "parser.c" /* yacc.c:1646  */
    break;

  case 25:
#line 170 "parser.y" /* yacc.c:1646  */
    {
	(yyval.l) = cb_add_text_list ((yyvsp[-2].l), (yyvsp[-1].l));
	put_exec_list();
}
#line 1557 "parser.c" /* yacc.c:1646  */
    break;

  case 26:
#line 177 "parser.y" /* yacc.c:1646  */
    {(yyval.l) = cb_text_list_add (NULL, (yyvsp[0].s));}
#line 1563 "parser.c" /* yacc.c:1646  */
    break;

  case 27:
#line 182 "parser.y" /* yacc.c:1646  */
    {
	(yyval.l) = cb_add_text_list ((yyvsp[-2].l), (yyvsp[-1].l));
	put_exec_list();
}
#line 1572 "parser.c" /* yacc.c:1646  */
    break;

  case 28:
#line 188 "parser.y" /* yacc.c:1646  */
    {(yyval.l) = cb_text_list_add (NULL, (yyvsp[0].s));}
#line 1578 "parser.c" /* yacc.c:1646  */
    break;

  case 29:
#line 189 "parser.y" /* yacc.c:1646  */
    {(yyval.l) = cb_text_list_add ((yyvsp[-1].l), (yyvsp[0].s));}
#line 1584 "parser.c" /* yacc.c:1646  */
    break;

  case 30:
#line 194 "parser.y" /* yacc.c:1646  */
    {
	put_exec_list();
}
#line 1592 "parser.c" /* yacc.c:1646  */
    break;

  case 31:
#line 199 "parser.y" /* yacc.c:1646  */
    {
	put_exec_list();
}
#line 1600 "parser.c" /* yacc.c:1646  */
    break;

  case 32:
#line 206 "parser.y" /* yacc.c:1646  */
    {
	put_exec_list();
}
#line 1608 "parser.c" /* yacc.c:1646  */
    break;

  case 33:
#line 210 "parser.y" /* yacc.c:1646  */
    { cb_set_cursorname((yyvsp[0].s));}
#line 1614 "parser.c" /* yacc.c:1646  */
    break;

  case 34:
#line 213 "parser.y" /* yacc.c:1646  */
    {cb_host_list_add (host_reference_list, (yyvsp[0].s));}
#line 1620 "parser.c" /* yacc.c:1646  */
    break;

  case 36:
#line 215 "parser.y" /* yacc.c:1646  */
    {cb_host_list_add (host_reference_list, (yyvsp[0].s));}
#line 1626 "parser.c" /* yacc.c:1646  */
    break;

  case 37:
#line 218 "parser.y" /* yacc.c:1646  */
    {cb_res_host_list_add (res_host_reference_list, (yyvsp[0].s));}
#line 1632 "parser.c" /* yacc.c:1646  */
    break;

  case 39:
#line 220 "parser.y" /* yacc.c:1646  */
    {cb_res_host_list_add (res_host_reference_list, (yyvsp[0].s));}
#line 1638 "parser.c" /* yacc.c:1646  */
    break;

  case 40:
#line 223 "parser.y" /* yacc.c:1646  */
    {
	cb_set_cursorname((yyvsp[-1].s));
	put_exec_list();
}
#line 1647 "parser.c" /* yacc.c:1646  */
    break;

  case 41:
#line 229 "parser.y" /* yacc.c:1646  */
    {
	cb_set_cursorname((yyvsp[-1].s));
	put_exec_list();
}
#line 1656 "parser.c" /* yacc.c:1646  */
    break;

  case 42:
#line 233 "parser.y" /* yacc.c:1646  */
    {
	cb_set_cursorname((yyvsp[-3].s));
	put_exec_list();
}
#line 1665 "parser.c" /* yacc.c:1646  */
    break;

  case 44:
#line 239 "parser.y" /* yacc.c:1646  */
    { }
#line 1671 "parser.c" /* yacc.c:1646  */
    break;

  case 45:
#line 242 "parser.y" /* yacc.c:1646  */
    {
	cb_set_dbname((yyvsp[0].s));
}
#line 1679 "parser.c" /* yacc.c:1646  */
    break;

  case 46:
#line 247 "parser.y" /* yacc.c:1646  */
    { put_exec_list(); }
#line 1685 "parser.c" /* yacc.c:1646  */
    break;

  case 47:
#line 248 "parser.y" /* yacc.c:1646  */
    { put_exec_list(); }
#line 1691 "parser.c" /* yacc.c:1646  */
    break;

  case 48:
#line 249 "parser.y" /* yacc.c:1646  */
    { put_exec_list(); }
#line 1697 "parser.c" /* yacc.c:1646  */
    break;

  case 49:
#line 250 "parser.y" /* yacc.c:1646  */
    { put_exec_list(); }
#line 1703 "parser.c" /* yacc.c:1646  */
    break;

  case 50:
#line 253 "parser.y" /* yacc.c:1646  */
    {
	(yyval.l) = cb_add_text_list(cb_text_list_add(NULL, (yyvsp[-2].s)), (yyvsp[-1].l));
	put_exec_list();
}
#line 1712 "parser.c" /* yacc.c:1646  */
    break;

  case 51:
#line 259 "parser.y" /* yacc.c:1646  */
    {
	cb_host_list_add (host_reference_list, (yyvsp[0].s));
}
#line 1720 "parser.c" /* yacc.c:1646  */
    break;

  case 52:
#line 264 "parser.y" /* yacc.c:1646  */
    {
	cb_host_list_add (host_reference_list, (yyvsp[0].s));
}
#line 1728 "parser.c" /* yacc.c:1646  */
    break;

  case 53:
#line 269 "parser.y" /* yacc.c:1646  */
    {
	cb_host_list_add (host_reference_list, (yyvsp[0].s));
}
#line 1736 "parser.c" /* yacc.c:1646  */
    break;

  case 54:
#line 274 "parser.y" /* yacc.c:1646  */
    {
	put_exec_list();
}
#line 1744 "parser.c" /* yacc.c:1646  */
    break;

  case 55:
#line 279 "parser.y" /* yacc.c:1646  */
    {
	put_exec_list();
}
#line 1752 "parser.c" /* yacc.c:1646  */
    break;

  case 56:
#line 284 "parser.y" /* yacc.c:1646  */
    {
	put_exec_list();
}
#line 1760 "parser.c" /* yacc.c:1646  */
    break;

  case 57:
#line 289 "parser.y" /* yacc.c:1646  */
    {
	put_exec_list();
}
#line 1768 "parser.c" /* yacc.c:1646  */
    break;

  case 58:
#line 292 "parser.y" /* yacc.c:1646  */
    {
	put_exec_list();
}
#line 1776 "parser.c" /* yacc.c:1646  */
    break;

  case 59:
#line 297 "parser.y" /* yacc.c:1646  */
    {
	(yyval.l) = cb_add_text_list(cb_text_list_add(NULL, (yyvsp[-6].s)), (yyvsp[-5].l));
	cb_add_text_list((yyval.l), cb_text_list_add(NULL, (yyvsp[-2].s)));
	cb_add_text_list((yyval.l), (yyvsp[-1].l));
	put_exec_list();
}
#line 1787 "parser.c" /* yacc.c:1646  */
    break;

  case 60:
#line 303 "parser.y" /* yacc.c:1646  */
    {
	(yyval.l) = cb_add_text_list(cb_text_list_add(NULL, (yyvsp[-4].s)), (yyvsp[-3].l));
	put_exec_list();
}
#line 1796 "parser.c" /* yacc.c:1646  */
    break;

  case 61:
#line 310 "parser.y" /* yacc.c:1646  */
    { put_exec_list(); }
#line 1802 "parser.c" /* yacc.c:1646  */
    break;

  case 62:
#line 311 "parser.y" /* yacc.c:1646  */
    { put_exec_list(); }
#line 1808 "parser.c" /* yacc.c:1646  */
    break;

  case 63:
#line 314 "parser.y" /* yacc.c:1646  */
    { cb_set_prepname((yyvsp[0].s)); }
#line 1814 "parser.c" /* yacc.c:1646  */
    break;

  case 64:
#line 317 "parser.y" /* yacc.c:1646  */
    { cb_host_list_add (host_reference_list, (yyvsp[0].s)); }
#line 1820 "parser.c" /* yacc.c:1646  */
    break;

  case 65:
#line 320 "parser.y" /* yacc.c:1646  */
    { (yyval.l) = cb_add_text_list (cb_text_list_add (NULL, (yyvsp[-1].s)), (yyvsp[0].l));}
#line 1826 "parser.c" /* yacc.c:1646  */
    break;

  case 66:
#line 323 "parser.y" /* yacc.c:1646  */
    { cb_set_cursorname((yyvsp[-2].s));}
#line 1832 "parser.c" /* yacc.c:1646  */
    break;

  case 67:
#line 326 "parser.y" /* yacc.c:1646  */
    {      (yyval.l) = cb_text_list_add (NULL, (yyvsp[0].s));}
#line 1838 "parser.c" /* yacc.c:1646  */
    break;

  case 68:
#line 327 "parser.y" /* yacc.c:1646  */
    {      (yyval.l) = cb_text_list_add ((yyvsp[-1].l), (yyvsp[0].s));}
#line 1844 "parser.c" /* yacc.c:1646  */
    break;

  case 69:
#line 328 "parser.y" /* yacc.c:1646  */
    {
	(yyval.l) = cb_text_list_add ((yyvsp[-1].l), cb_host_list_add (host_reference_list, (yyvsp[0].s)));
}
#line 1852 "parser.c" /* yacc.c:1646  */
    break;

  case 70:
#line 331 "parser.y" /* yacc.c:1646  */
    {
	     (yyval.l) = cb_text_list_add((yyvsp[-2].l), "WHERE CURRENT OF");
	     cb_set_cursorname((yyvsp[0].s));
	     (yyval.l) = cb_text_list_add((yyvsp[-2].l), cursorname);
}
#line 1862 "parser.c" /* yacc.c:1646  */
    break;

  case 71:
#line 338 "parser.y" /* yacc.c:1646  */
    {}
#line 1868 "parser.c" /* yacc.c:1646  */
    break;

  case 72:
#line 340 "parser.y" /* yacc.c:1646  */
    {}
#line 1874 "parser.c" /* yacc.c:1646  */
    break;

  case 73:
#line 341 "parser.y" /* yacc.c:1646  */
    {}
#line 1880 "parser.c" /* yacc.c:1646  */
    break;

  case 74:
#line 342 "parser.y" /* yacc.c:1646  */
    {}
#line 1886 "parser.c" /* yacc.c:1646  */
    break;

  case 75:
#line 343 "parser.y" /* yacc.c:1646  */
    {}
#line 1892 "parser.c" /* yacc.c:1646  */
    break;

  case 76:
#line 345 "parser.y" /* yacc.c:1646  */
    {
	current_field = NULL;
	description_field = NULL;
	put_exec_list();
}
#line 1902 "parser.c" /* yacc.c:1646  */
    break;

  case 77:
#line 352 "parser.y" /* yacc.c:1646  */
    {
	// check host_variable
	put_exec_list();
}
#line 1911 "parser.c" /* yacc.c:1646  */
    break;

  case 83:
#line 363 "parser.y" /* yacc.c:1646  */
    { put_exec_list(); }
#line 1917 "parser.c" /* yacc.c:1646  */
    break;

  case 84:
#line 364 "parser.y" /* yacc.c:1646  */
    { put_exec_list(); }
#line 1923 "parser.c" /* yacc.c:1646  */
    break;

  case 85:
#line 368 "parser.y" /* yacc.c:1646  */
    {
	struct cb_field *x;

	x =  cb_build_field_tree( (yyvsp[-1].ld), (yyvsp[0].s) , current_field);
	if( x != NULL)
	{
		if( x->level != 78)
			current_field = x;
	}
}
#line 1938 "parser.c" /* yacc.c:1646  */
    break;

  case 86:
#line 379 "parser.y" /* yacc.c:1646  */
    {
	if (description_field == NULL)
		description_field = current_field;
}
#line 1947 "parser.c" /* yacc.c:1646  */
    break;

  case 87:
#line 383 "parser.y" /* yacc.c:1646  */
    {
	struct cb_field *x;

	x =  cb_build_field_tree( (yyvsp[0].ld), "" , current_field); // regist dummy name
	if( x != NULL){
		if( x->level != 78)
			current_field = x;
	}
}
#line 1961 "parser.c" /* yacc.c:1646  */
    break;

  case 88:
#line 393 "parser.y" /* yacc.c:1646  */
    {
	if (description_field == NULL)
		description_field = current_field;
}
#line 1970 "parser.c" /* yacc.c:1646  */
    break;

  case 89:
#line 401 "parser.y" /* yacc.c:1646  */
    {}
#line 1976 "parser.c" /* yacc.c:1646  */
    break;

  case 90:
#line 403 "parser.y" /* yacc.c:1646  */
    {}
#line 1982 "parser.c" /* yacc.c:1646  */
    break;

  case 98:
#line 417 "parser.y" /* yacc.c:1646  */
    {  build_picture( (yyvsp[0].s),current_field); }
#line 1988 "parser.c" /* yacc.c:1646  */
    break;

  case 101:
#line 426 "parser.y" /* yacc.c:1646  */
    { current_field->usage = USAGE_FLOAT;   }
#line 1994 "parser.c" /* yacc.c:1646  */
    break;

  case 102:
#line 427 "parser.y" /* yacc.c:1646  */
    { current_field->usage = USAGE_DOUBLE; }
#line 2000 "parser.c" /* yacc.c:1646  */
    break;

  case 103:
#line 428 "parser.y" /* yacc.c:1646  */
    { current_field->usage = USAGE_PACKED; }
#line 2006 "parser.c" /* yacc.c:1646  */
    break;

  case 104:
#line 429 "parser.y" /* yacc.c:1646  */
    { current_field->usage = USAGE_OTHER; }
#line 2012 "parser.c" /* yacc.c:1646  */
    break;

  case 105:
#line 434 "parser.y" /* yacc.c:1646  */
    {
	if(current_field->pictype != PIC_ALPHANUMERIC &&
		current_field->pictype != PIC_NATIONAL){
		printmsg("parse error: %s specified the data types are not available to VARYING\n",
		       current_field->sname);
		exit(-1);
	}

	var_varying = current_field;
	put_exec_list();
}
#line 2028 "parser.c" /* yacc.c:1646  */
    break;

  case 106:
#line 447 "parser.y" /* yacc.c:1646  */
    {}
#line 2034 "parser.c" /* yacc.c:1646  */
    break;

  case 107:
#line 449 "parser.y" /* yacc.c:1646  */
    {}
#line 2040 "parser.c" /* yacc.c:1646  */
    break;

  case 108:
#line 450 "parser.y" /* yacc.c:1646  */
    {}
#line 2046 "parser.c" /* yacc.c:1646  */
    break;

  case 109:
#line 451 "parser.y" /* yacc.c:1646  */
    {}
#line 2052 "parser.c" /* yacc.c:1646  */
    break;

  case 110:
#line 455 "parser.y" /* yacc.c:1646  */
    {
	current_field->sign_leading = SIGNLEADING;
}
#line 2060 "parser.c" /* yacc.c:1646  */
    break;

  case 111:
#line 459 "parser.y" /* yacc.c:1646  */
    {

}
#line 2068 "parser.c" /* yacc.c:1646  */
    break;

  case 112:
#line 464 "parser.y" /* yacc.c:1646  */
    {}
#line 2074 "parser.c" /* yacc.c:1646  */
    break;

  case 113:
#line 465 "parser.y" /* yacc.c:1646  */
    {}
#line 2080 "parser.c" /* yacc.c:1646  */
    break;

  case 115:
#line 468 "parser.y" /* yacc.c:1646  */
    { current_field->separate = SIGN_SEPARATE; }
#line 2086 "parser.c" /* yacc.c:1646  */
    break;

  case 116:
#line 473 "parser.y" /* yacc.c:1646  */
    {
	current_field->occurs = (int)(yyvsp[-1].ld);
}
#line 2094 "parser.c" /* yacc.c:1646  */
    break;

  case 117:
#line 479 "parser.y" /* yacc.c:1646  */
    {}
#line 2100 "parser.c" /* yacc.c:1646  */
    break;


#line 2104 "parser.c" /* yacc.c:1646  */
      default: break;
    }
  /* User semantic actions sometimes alter yychar, and that requires
     that yytoken be updated with the new translation.  We take the
     approach of translating immediately before every use of yytoken.
     One alternative is translating here after every semantic action,
     but that translation would be missed if the semantic action invokes
     YYABORT, YYACCEPT, or YYERROR immediately after altering yychar or
     if it invokes YYBACKUP.  In the case of YYABORT or YYACCEPT, an
     incorrect destructor might then be invoked immediately.  In the
     case of YYERROR or YYBACKUP, subsequent parser actions might lead
     to an incorrect destructor call or verbose syntax error message
     before the lookahead is translated.  */
  YY_SYMBOL_PRINT ("-> $$ =", yyr1[yyn], &yyval, &yyloc);

  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);

  *++yyvsp = yyval;

  /* Now 'shift' the result of the reduction.  Determine what state
     that goes to, based on the state we popped back to and the rule
     number reduced by.  */

  yyn = yyr1[yyn];

  yystate = yypgoto[yyn - YYNTOKENS] + *yyssp;
  if (0 <= yystate && yystate <= YYLAST && yycheck[yystate] == *yyssp)
    yystate = yytable[yystate];
  else
    yystate = yydefgoto[yyn - YYNTOKENS];

  goto yynewstate;


/*--------------------------------------.
| yyerrlab -- here on detecting error.  |
`--------------------------------------*/
yyerrlab:
  /* Make sure we have latest lookahead translation.  See comments at
     user semantic actions for why this is necessary.  */
  yytoken = yychar == YYEMPTY ? YYEMPTY : YYTRANSLATE (yychar);

  /* If not already recovering from an error, report this error.  */
  if (!yyerrstatus)
    {
      ++yynerrs;
#if ! YYERROR_VERBOSE
      yyerror (YY_("syntax error"));
#else
# define YYSYNTAX_ERROR yysyntax_error (&yymsg_alloc, &yymsg, \
                                        yyssp, yytoken)
      {
        char const *yymsgp = YY_("syntax error");
        int yysyntax_error_status;
        yysyntax_error_status = YYSYNTAX_ERROR;
        if (yysyntax_error_status == 0)
          yymsgp = yymsg;
        else if (yysyntax_error_status == 1)
          {
            if (yymsg != yymsgbuf)
              YYSTACK_FREE (yymsg);
            yymsg = (char *) YYSTACK_ALLOC (yymsg_alloc);
            if (!yymsg)
              {
                yymsg = yymsgbuf;
                yymsg_alloc = sizeof yymsgbuf;
                yysyntax_error_status = 2;
              }
            else
              {
                yysyntax_error_status = YYSYNTAX_ERROR;
                yymsgp = yymsg;
              }
          }
        yyerror (yymsgp);
        if (yysyntax_error_status == 2)
          goto yyexhaustedlab;
      }
# undef YYSYNTAX_ERROR
#endif
    }



  if (yyerrstatus == 3)
    {
      /* If just tried and failed to reuse lookahead token after an
         error, discard it.  */

      if (yychar <= YYEOF)
        {
          /* Return failure if at end of input.  */
          if (yychar == YYEOF)
            YYABORT;
        }
      else
        {
          yydestruct ("Error: discarding",
                      yytoken, &yylval);
          yychar = YYEMPTY;
        }
    }

  /* Else will try to reuse lookahead token after shifting the error
     token.  */
  goto yyerrlab1;


/*---------------------------------------------------.
| yyerrorlab -- error raised explicitly by YYERROR.  |
`---------------------------------------------------*/
yyerrorlab:

  /* Pacify compilers like GCC when the user code never invokes
     YYERROR and the label yyerrorlab therefore never appears in user
     code.  */
  if (/*CONSTCOND*/ 0)
     goto yyerrorlab;

  /* Do not reclaim the symbols of the rule whose action triggered
     this YYERROR.  */
  YYPOPSTACK (yylen);
  yylen = 0;
  YY_STACK_PRINT (yyss, yyssp);
  yystate = *yyssp;
  goto yyerrlab1;


/*-------------------------------------------------------------.
| yyerrlab1 -- common code for both syntax error and YYERROR.  |
`-------------------------------------------------------------*/
yyerrlab1:
  yyerrstatus = 3;      /* Each real token shifted decrements this.  */

  for (;;)
    {
      yyn = yypact[yystate];
      if (!yypact_value_is_default (yyn))
        {
          yyn += YYTERROR;
          if (0 <= yyn && yyn <= YYLAST && yycheck[yyn] == YYTERROR)
            {
              yyn = yytable[yyn];
              if (0 < yyn)
                break;
            }
        }

      /* Pop the current state because it cannot handle the error token.  */
      if (yyssp == yyss)
        YYABORT;


      yydestruct ("Error: popping",
                  yystos[yystate], yyvsp);
      YYPOPSTACK (1);
      yystate = *yyssp;
      YY_STACK_PRINT (yyss, yyssp);
    }

  YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
  *++yyvsp = yylval;
  YY_IGNORE_MAYBE_UNINITIALIZED_END


  /* Shift the error token.  */
  YY_SYMBOL_PRINT ("Shifting", yystos[yyn], yyvsp, yylsp);

  yystate = yyn;
  goto yynewstate;


/*-------------------------------------.
| yyacceptlab -- YYACCEPT comes here.  |
`-------------------------------------*/
yyacceptlab:
  yyresult = 0;
  goto yyreturn;

/*-----------------------------------.
| yyabortlab -- YYABORT comes here.  |
`-----------------------------------*/
yyabortlab:
  yyresult = 1;
  goto yyreturn;

#if !defined yyoverflow || YYERROR_VERBOSE
/*-------------------------------------------------.
| yyexhaustedlab -- memory exhaustion comes here.  |
`-------------------------------------------------*/
yyexhaustedlab:
  yyerror (YY_("memory exhausted"));
  yyresult = 2;
  /* Fall through.  */
#endif

yyreturn:
  if (yychar != YYEMPTY)
    {
      /* Make sure we have latest lookahead translation.  See comments at
         user semantic actions for why this is necessary.  */
      yytoken = YYTRANSLATE (yychar);
      yydestruct ("Cleanup: discarding lookahead",
                  yytoken, &yylval);
    }
  /* Do not reclaim the symbols of the rule whose action triggered
     this YYABORT or YYACCEPT.  */
  YYPOPSTACK (yylen);
  YY_STACK_PRINT (yyss, yyssp);
  while (yyssp != yyss)
    {
      yydestruct ("Cleanup: popping",
                  yystos[*yyssp], yyvsp);
      YYPOPSTACK (1);
    }
#ifndef yyoverflow
  if (yyss != yyssa)
    YYSTACK_FREE (yyss);
#endif
#if YYERROR_VERBOSE
  if (yymsg != yymsgbuf)
    YYSTACK_FREE (yymsg);
#endif
  return yyresult;
}
#line 487 "parser.y" /* yacc.c:1906  */



static void
put_exec_list()
{
	struct cb_exec_list *l;
	struct cb_exec_list *p;

	struct cb_hostreference_list *h;
	h = host_reference_list;

	for(; h; h = h->next)
	{
		hostreferenceCount++;
	}

	l = malloc (sizeof (struct cb_exec_list));

	l->startLine = startlineno;
	l->endLine = endlineno;
	l->period = period;
	l->host_list = host_reference_list;
	l->hostreferenceCount =hostreferenceCount;
	l->res_host_list = res_host_reference_list;
	l->conn_use_other_db = conn_use_other_db;
	l->sql_list = sql_list;
	l->dbName = com_strdup(dbname);
	l->prepareName = com_strdup(prepname);
	l->cursorName = com_strdup(cursorname);
	l->commandName = com_strdup(commandname);
	l->command_putother = command_putother;
	l->sqlName = com_strdup(sqlname);
	l->incfileName = com_strdup(incfilename);
	l->varname = var_varying;
	l->next = NULL;

	if (exec_list == NULL)
	{
		exec_list = l;
	}else{
		p = exec_list;
		for (; p->next ; p = p->next);
		p->next = l;

	}

}


static  int  xxx =0;

struct cb_field *getfieldbynamefrom(char *name , struct cb_field *field)
{
	struct cb_field * p;

	if( field == NULL)
		return NULL;

	xxx++;

	if(strcmp(name,field->sname) == 0 ){
		return field;
	}

	p = getfieldbynamefrom(name, field->sister);
	if( p != NULL )
		return p;


	p = getfieldbynamefrom(name, field->children);
	if( p != NULL )
		return p;

	return NULL;

}

struct cb_field * getfieldbyname(char *name )
{
	return getfieldbynamefrom(name, description_field);
}

int gethostvarianttype(char *name,  int *type, int *digits, int *scale)
{
	struct cb_field * p;
	int tmp_type,tmp_dig,tmp_scl;
	p = getfieldbyname(name);
	if( p == NULL){
		return ERR_NOTDEF_WORKING;
	}
	*digits = tmp_dig = p->picnsize;
	*scale = tmp_scl = -(p->scale);
	if(  p->pictype != 0 ){
		switch(p->pictype){
		case PIC_ALPHANUMERIC:
			tmp_type =  HVARTYPE_ALPHABETIC;
			break;
		case PIC_NATIONAL:
			tmp_type = HVARTYPE_NATIONAL;
			break;
		case PIC_NUMERIC:
			if(p->have_sign){
				if(p->usage){
					switch(p->usage){
					case USAGE_PACKED:
						tmp_type = HVARTYPE_SIGNED_PACKED;
						break;
					case USAGE_BINARY_NATIVE:
						tmp_type = HVARTYPE_SIGNED_BINARY_NATIVE;
						break;
					default:
						return ERR_NOT_SUPPORTED_USAGE;
					}
				}else if(p->sign_leading){
					if(p->separate){
						tmp_type = HVARTYPE_SIGNED_LEADING_SEPARATE;
					}else{
						tmp_type = HVARTYPE_SIGNED_LEADING_COMBINED;
					}
				}else{
					if(p->separate){
						tmp_type = HVARTYPE_SIGNED_TRAILING_SEPARATE;
					}else{
						tmp_type = HVARTYPE_SIGNED_TRAILING_COMBINED;
					}
				}
			}else{
				if(p->usage){
					switch(p->usage){
					case USAGE_PACKED:
						tmp_type = HVARTYPE_UNSIGNED_PACKED;
						break;
					case USAGE_BINARY_NATIVE:
						tmp_type = HVARTYPE_UNSIGNED_BINARY_NATIVE;
						break;
					default:
						return ERR_NOT_SUPPORTED_USAGE;
					}
				}else{
					tmp_type = HVARTYPE_UNSIGNED_NUMERIC;
				}
			}
			break;
		case PIC_ALPHANUMERIC_VARYING:
			tmp_type =  HVARTYPE_ALPHANUMERIC_VARYING;
			break;
		case PIC_NATIONAL_VARYING:
			tmp_type =  HVARTYPE_JAPANESE_VARYING;
			break;
		default:
			break;
		}
		*type = tmp_type;
		return 0;
	} else { // Group data
		if(p->occurs > 0){
			struct cb_field * c;

			c = p->children;
			while(c != NULL){
				if(c->children){
					return ERR_NOTDEF_CONVERSION;
				}
				c = c->sister;
			}
		}
		*type = HVARTYPE_GROUP;
		return 0;
	}
	if(p->usage){
		switch(p->usage){
		case USAGE_FLOAT:
			tmp_type = HVARTYPE_FLOAT;
			break;
		case USAGE_DOUBLE:
			tmp_type = HVARTYPE_FLOAT;
			break;
		default:
			return ERR_NOT_SUPPORTED_USAGE;
		}
		*type = tmp_type;
		return 0;
	}
	return ERR_NOTDEF_CONVERSION;
}

int cb_get_level (int val)
{
	int level = val;

	/* check level */
	switch (level) {
	case 66:
	case 77:
	case 78:
	case 88:
		break;
	default:
		if (level < 1 || level > 49) {
			goto level_error;
		}
		break;
	}

	return level;

	level_error:

	return 0;
}

struct cb_field *
cb_field_founder (struct cb_field *f)
{
     while (f->parent) {
		f = f->parent;
	}
	return f;
}
struct cb_field * cb_build_field_tree(int level, char *name , struct cb_field *last_field)
{
	int lv;
	struct cb_field *f, *p;

	if(name == NULL)
		return NULL;

	lv = cb_get_level (level);
	if (!lv) {
		return NULL;
	}

	f = malloc(sizeof(struct  cb_field));
	if( f == NULL )
		return NULL;

	memset(f, 0 ,sizeof(struct cb_field));

	f->sname = com_strdup(name);

	if (lv == 78) {
		f->level = 1;
	} else{
		f->level = lv;
	}

	if (last_field) {
		if (last_field->level == 77 && f->level != 01 &&
				f->level != 77 && f->level != 66 && f->level != 88) {
			return NULL;
		}
	}

	if (f->level == 1 || f->level == 77) {
		/* top level */
		if (last_field) {
			cb_field_founder (last_field)->sister = f;
		}
	} else {
		if(last_field == NULL){
			printmsg("parse error: %s level should start from 01 or 66 or 77 or 88\n", name);
			exit(-1);
			return NULL;
		}

		if (f->level == 66) {
			/* level 66 */
			f->parent = cb_field_founder (last_field);
			for (p = f->parent->children; p && p->sister; p = p->sister) ;
			if (p) {
				p->sister = f;
			}
		} else if (f->level == 88) {
			/* level 88 */
			f->parent = last_field;
		}else if (f->level > last_field->level) {
			/* lower level */
			last_field->children = f;
			f->parent = last_field;
		} else if (f->level == last_field->level) {
			/* same level */
			same_level:
			last_field->sister = f;
			f->parent = last_field->parent;
		} else {
			/* upper level */
			for (p = last_field->parent; p; p = p->parent) {
				if (p->level == f->level) {
					last_field = p;
					goto same_level;
				}
				if ( p->level < f->level) {
				     break;
				}
			}
			return NULL;
		}
	}

	return f;
}

int  build_picture (const char *str,struct cb_field * pic){
	const char		*p;

	int			i;
	int			n;
	unsigned char		c;

	int	category = 0;
	int s_count = 0;
	int v_count = 0;
	int idx = 0;
	int digits = 0;
	int scale = 0;
	int allocated = 0;

	if (strlen (str) > 50) {
		return 0;
	}

	for(p = str; *p; p++){
		n=1;
		c=*p;
	while(1){
		while(p[1]==c){
			p++; n++;
		}

		if(p[1] == '('){
			i=0;
			p += 2;
			allocated = 0;
			for(;*p == '0';p++){
				;
			}
			for(;*p != ')';p++){
				if(!isdigit(*p)){
					return 0;
				} else {
					allocated++;
					if(allocated > 9){
						return 0;
					}
					i = i * 10 + (*p - '0');
				}
			}
			if(i==0){
				return 0;
			}
			n+=i-1;
			continue;
		}
		break;
		}


		switch(c){
		case 'X':
			if(s_count | v_count){
				return 0;
			}
			category |=  PIC_ALPHANUMERIC;
			digits += n;
			break;
		case '9':
			category |= PIC_NUMERIC;
			digits += n;
			if(v_count){
				scale += n;
			}
			break;
		case 'N':
			if(s_count | v_count){
				return 0;
			}
			category |=  PIC_NATIONAL;
			digits += n;
			break;
		case 'S':
			category |= PIC_NUMERIC;
			if(category & PIC_ALPHABETIC) {
				return 0;
			}
			s_count += n;
			if(s_count > 1 || idx !=0){
				return 0;
			}
			continue;
		case 'V':
			category |= PIC_NUMERIC;
			if(category & PIC_ALPHABETIC) {
				return 0;
			}
			v_count += n;
			if(v_count > 1){
				return 0;
			}
			break;
		default:
			break;
		}
		idx += sizeof(int);
	}

	pic->picnsize = digits;
	pic->scale = scale;
	pic->have_sign = (unsigned char)s_count;
	pic->pictype = category;
	return 1;
}

int
check_has_occurs_children(struct cb_field *field){
	int ret;

	if(field == NULL)
		return 0;

	printmsg("CHILDR:sname=%s, level=%d, occurs=%d, children=%d",
	       field->sname, field->level, field->occurs, field->children);

	if(field->occurs != 0){
		return 1;
	}

	if(field->children != NULL){
		return 2;
	}

	ret = check_has_occurs_children(field->sister);
	if(ret) return ret;

	return 0;
}

int
check_host_has_multi_occurs(struct cb_field *field){
	int ret;

	if(field == NULL)
		return 0;

	if(field->occurs != 0){
		ret = check_has_occurs_children(field->children);
		if(ret) return ret;
	}

	ret = check_host_has_multi_occurs(field->children);
	if(ret) return ret;

	ret = check_host_has_multi_occurs(field->sister);
	if(ret) return ret;

	return 0;
}

