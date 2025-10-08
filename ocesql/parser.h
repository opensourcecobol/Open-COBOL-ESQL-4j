/* A Bison parser, made by GNU Bison 3.7.4.  */

/* Bison interface for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2015, 2018-2020 Free Software Foundation,
   Inc.

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

/* DO NOT RELY ON FEATURES THAT ARE NOT DOCUMENTED in the manual,
   especially those whose name start with YY_ or yy_.  They are
   private implementation details that can be changed or removed.  */

#ifndef YY_YY_PARSER_H_INCLUDED
# define YY_YY_PARSER_H_INCLUDED
/* Debug traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif
#if YYDEBUG
extern int yydebug;
#endif

/* Token kinds.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
  enum yytokentype
  {
    YYEMPTY = -2,
    YYEOF = 0,                     /* "end of file"  */
    YYerror = 256,                 /* error  */
    YYUNDEF = 257,                 /* "invalid token"  */
    SELECT = 258,                  /* SELECT  */
    SELECTFROM = 259,              /* SELECTFROM  */
    TOKEN = 260,                   /* TOKEN  */
    CURNAME = 261,                 /* CURNAME  */
    HOSTTOKEN = 262,               /* HOSTTOKEN  */
    WORD = 263,                    /* WORD  */
    PICTURE = 264,                 /* PICTURE  */
    INSERT = 265,                  /* INSERT  */
    UPDATE = 266,                  /* UPDATE  */
    DISCONNECT = 267,              /* DISCONNECT  */
    DELETE = 268,                  /* DELETE  */
    EXECUTE = 269,                 /* EXECUTE  */
    OTHERFUNC = 270,               /* OTHERFUNC  */
    INTO = 271,                    /* INTO  */
    NUMERIC = 272,                 /* NUMERIC  */
    END_EXEC = 273,                /* END_EXEC  */
    EXECSQL = 274,                 /* EXECSQL  */
    EXECSQL_INCLUDE = 275,         /* EXECSQL_INCLUDE  */
    PREPARE = 276,                 /* PREPARE  */
    FROM = 277,                    /* FROM  */
    DECLARE = 278,                 /* DECLARE  */
    CURSOR = 279,                  /* CURSOR  */
    FOR = 280,                     /* FOR  */
    WORKINGBEGIN = 281,            /* WORKINGBEGIN  */
    WORKINGEND = 282,              /* WORKINGEND  */
    HOSTVARIANTBEGIN = 283,        /* HOSTVARIANTBEGIN  */
    HOSTVARIANTEND = 284,          /* HOSTVARIANTEND  */
    INCLUDE_FILE = 285,            /* INCLUDE_FILE  */
    INCLUDE_SQLCA = 286,           /* INCLUDE_SQLCA  */
    SQLCA = 287,                   /* SQLCA  */
    IDENTIFIED_BY = 288,           /* IDENTIFIED_BY  */
    COMMIT_WORK = 289,             /* COMMIT_WORK  */
    ROLLBACK_WORK = 290,           /* ROLLBACK_WORK  */
    CONNECT = 291,                 /* CONNECT  */
    USING = 292,                   /* USING  */
    OPEN = 293,                    /* OPEN  */
    CLOSE = 294,                   /* CLOSE  */
    FETCH = 295,                   /* FETCH  */
    TRAILING = 296,                /* TRAILING  */
    COMP_1 = 297,                  /* COMP_1  */
    COMP_2 = 298,                  /* COMP_2  */
    COMP_3 = 299,                  /* COMP_3  */
    USAGE = 300,                   /* USAGE  */
    SIGN = 301,                    /* SIGN  */
    LEADING = 302,                 /* LEADING  */
    SEPARATE = 303,                /* SEPARATE  */
    AT = 304,                      /* AT  */
    IS = 305,                      /* IS  */
    ARE = 306,                     /* ARE  */
    VALUE = 307,                   /* VALUE  */
    VARYING = 308,                 /* VARYING  */
    ALL = 309,                     /* ALL  */
    OCCURS = 310,                  /* OCCURS  */
    EXTERNAL = 311,                /* EXTERNAL  */
    TIMES = 312,                   /* TIMES  */
    CONST = 313,                   /* CONST  */
    WHERECURRENTOF = 314           /* WHERECURRENTOF  */
  };
  typedef enum yytokentype yytoken_kind_t;
#endif
/* Token kinds.  */
#define YYEMPTY -2
#define YYEOF 0
#define YYerror 256
#define YYUNDEF 257
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
#line 51 "parser.y"

	char *s;
	long int ld;
	struct cb_sql_list	*l;
	struct cb_hostreference_list *h;

#line 192 "parser.h"

};
typedef union YYSTYPE YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define YYSTYPE_IS_DECLARED 1
#endif


extern YYSTYPE yylval;

int yyparse (void);

#endif /* !YY_YY_PARSER_H_INCLUDED  */
