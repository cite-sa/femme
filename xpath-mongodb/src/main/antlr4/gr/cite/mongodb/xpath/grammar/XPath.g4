grammar XPath;

import XPathLexerTokens;

xpath  :  expr
  ;

locationPath 
  :  relativeLocationPath
  |  absoluteLocationPathNoroot
  ;

absoluteLocationPathNoroot
  :  '/' relativeLocationPath ?
  |  '//' relativeLocationPath
  |  relativeLocationPath
  ;

relativeLocationPath
  :  step (('/'|'//') step)*
  ;

step  :  axisSpecifier nodeTest predicate*
  |  abbreviatedStep
  ;

axisSpecifier
  :  AxisNameXpath '::'
  |  '@'?
  ;

nodeTest:  nameTest
  |  NodeType '(' ')'
  |  'processing-instruction' '(' ( XPATH_LITERAL | STRING_LITERAL) ')'
  ;

predicate
  :  '[' expr ']'
  ;

abbreviatedStep
  :  '.'
  |  '..'
  ;

expr  :  orExpr
  ;

primaryExpr
  :  variableReference
  |  '(' expr ')'
  |  ( XPATH_LITERAL | STRING_LITERAL)
  |  REAL_NUMBER_CONSTANT
  |  functionCall
  ;

functionCall
  :  functionName '(' ( expr ( ',' expr )* )? ')'
  ;

unionExprNoRoot
  :  pathExprNoRoot ('|' unionExprNoRoot)?
  |  '/' '|' unionExprNoRoot
  ;

pathExprNoRoot
  :  locationPath
  |  filterExpr (('/'|'//') relativeLocationPath)?
  ;

filterExpr
  :  primaryExpr predicate*
  ;

orExpr  :  andExpr (OR andExpr)*
  ;

andExpr  :  equalityExpr (AND equalityExpr)*
  ;

equalityExpr
  :  relationalExpr (('='|'!=') relationalExpr)*
  ;

relationalExpr
  :  additiveExpr ((LOWER_THAN | GREATER_THAN | LOWER_OR_EQUAL_THAN | GREATER_OR_EQUAL_THAN) additiveExpr)*
  ;

additiveExpr
  :  multiplicativeExpr (('+'|'-') multiplicativeExpr)*
  ;

multiplicativeExpr
  :  unaryExprNoRoot (( '*' | DIV | MOD ) multiplicativeExpr)?
  |  '/' (( DIV | MOD ) multiplicativeExpr)?
  ;

unaryExprNoRoot
  :  '-'* unionExprNoRoot
  ;

qName  :  nCName (':' nCName)?
  ;

functionName
  :  qName  // Does not match nodeType, as per spec.
  ;

variableReference
  :  '$' qName
  ;

nameTest:  '*'
  |  nCName ':' '*'
  |  qName
  ;

nCName  :  NCName | SIMPLE_IDENTIFIER_WITH_NUMBERS | SIMPLE_IDENTIFIER
  |  AxisNameXpath
  |  wcpsHotWords
  ;

wcpsHotWords:  FOR
	|  ABSOLUTE_VALUE
	|  ADD
	|  ALL
	|  AND
	|  ARCSIN
	|  ARCCOS
	|  ARCTAN
	|  AVG
	|  BIT
	|  CONDENSE
	|  COS
	|  COSH
	|  COUNT
	|  COVERAGE
	|  CRS_TRANSFORM
	|  DECODE
	|  DESCRIBE_COVERAGE
	|  DIV
	|  ENCODE
	|  EXP
	|  EXTEND
	|  FALSE 
	|  IMAGINARY_PART
	|  ID
	|  IMGCRSDOMAIN
	|  IN
	|  LN
	|  LIST
	|  LOG
	|  MAX
	|  MIN
	|  METADATA
	|  MOD
	|  NOT
	|  OR
	|  OVER
	|  OVERLAY
	|  POWER
	|  REAL_PART
	|  ROUND
	|  RETURN
	|  SCALE
	|  SIN
	|  SINH
	|  SLICE
	|  SOME
	|  SQUARE_ROOT
	|  STRUCT
	|  TAN
	|  TANH
	|  TRIM
	|  TRUE
	|  USING
	|  VALUE
	|  VALUES
	|  WHERE
	|  XOR
	;
