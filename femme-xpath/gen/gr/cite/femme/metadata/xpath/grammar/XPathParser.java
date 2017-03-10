// Generated from /home/kapostolopoulos/repositories/git/earthserver/femme/femme-xpath/src/main/antlr4/gr/cite/femme/metadata/xpath/grammar/XPath.g4 by ANTLR 4.6
package gr.cite.femme.metadata.xpath.grammar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class XPathParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.6", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, NodeType=7, AxisNameXpath=8, 
		FOR=9, ABSOLUTE_VALUE=10, ADD=11, ALL=12, AND=13, ARCSIN=14, ARCCOS=15, 
		ARCTAN=16, AVG=17, BIT=18, COLON=19, COMMA=20, CONDENSE=21, COS=22, COSH=23, 
		COUNT=24, COVERAGE=25, COVERAGE_VARIABLE_NAME_PREFIX=26, CRS_TRANSFORM=27, 
		DECODE=28, DESCRIBE_COVERAGE=29, DIV=30, DIVISION=31, DOT=32, ENCODE=33, 
		EQUAL=34, EXP=35, EXTEND=36, FALSE=37, GREATER_THAN=38, GREATER_THAN_SLASH=39, 
		GREATER_OR_EQUAL_THAN=40, IMAGINARY_PART=41, IMGCRSDOMAIN=42, IN=43, LEFT_BRACE=44, 
		LEFT_BRACKET=45, LEFT_PARANTHESIS=46, LET=47, LN=48, LIST=49, LOG=50, 
		LOWER_THAN=51, LOWER_THAN_SLASH=52, LOWER_OR_EQUAL_THAN=53, MAX=54, MIN=55, 
		MINUS=56, MIXED=57, MOD=58, MULTIPLICATION=59, NOT=60, NOT_EQUAL=61, OR=62, 
		OVER=63, OVERLAY=64, PLUS=65, POWER=66, REAL_PART=67, ROUND=68, RETURN=69, 
		RIGHT_BRACE=70, RIGHT_BRACKET=71, RIGHT_PARANTHESIS=72, SCALE=73, SEMICOLON=74, 
		SIN=75, SINH=76, SLICE=77, SOME=78, SQUARE_ROOT=79, STRUCT=80, TAN=81, 
		TANH=82, TRIM=83, TRUE=84, USING=85, VALUE=86, VALUES=87, WHERE=88, WRAP_RESULT=89, 
		XOR=90, REAL_NUMBER_CONSTANT=91, SIMPLE_IDENTIFIER=92, SIMPLE_IDENTIFIER_WITH_NUMBERS=93, 
		IDENTIFIER=94, NAME=95, STRING_LITERAL=96, WS=97, XPATH_LITERAL=98, NCName=99;
	public static final int
		RULE_xpath = 0, RULE_locationPath = 1, RULE_absoluteLocationPathNoroot = 2, 
		RULE_relativeLocationPath = 3, RULE_step = 4, RULE_axisSpecifier = 5, 
		RULE_nodeTest = 6, RULE_predicate = 7, RULE_abbreviatedStep = 8, RULE_expr = 9, 
		RULE_primaryExpr = 10, RULE_functionCall = 11, RULE_unionExprNoRoot = 12, 
		RULE_pathExprNoRoot = 13, RULE_filterExpr = 14, RULE_orExpr = 15, RULE_andExpr = 16, 
		RULE_equalityExpr = 17, RULE_relationalExpr = 18, RULE_additiveExpr = 19, 
		RULE_multiplicativeExpr = 20, RULE_unaryExprNoRoot = 21, RULE_qName = 22, 
		RULE_functionName = 23, RULE_variableReference = 24, RULE_nameTest = 25, 
		RULE_nCName = 26;
	public static final String[] ruleNames = {
		"xpath", "locationPath", "absoluteLocationPathNoroot", "relativeLocationPath", 
		"step", "axisSpecifier", "nodeTest", "predicate", "abbreviatedStep", "expr", 
		"primaryExpr", "functionCall", "unionExprNoRoot", "pathExprNoRoot", "filterExpr", 
		"orExpr", "andExpr", "equalityExpr", "relationalExpr", "additiveExpr", 
		"multiplicativeExpr", "unaryExprNoRoot", "qName", "functionName", "variableReference", 
		"nameTest", "nCName"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'//'", "'::'", "'@'", "'processing-instruction'", "'..'", "'|'", 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		"':'", "','", null, null, null, null, null, "'$'", null, null, null, null, 
		"'/'", "'.'", null, "'='", null, null, null, "'>'", "'/>'", "'>='", null, 
		null, null, "'{'", "'['", "'('", null, null, null, null, "'<'", "'</'", 
		"'<='", null, null, "'-'", null, null, "'*'", null, "'!='", null, null, 
		null, "'+'", null, null, null, null, "'}'", "']'", "')'", null, "';'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, "NodeType", "AxisNameXpath", 
		"FOR", "ABSOLUTE_VALUE", "ADD", "ALL", "AND", "ARCSIN", "ARCCOS", "ARCTAN", 
		"AVG", "BIT", "COLON", "COMMA", "CONDENSE", "COS", "COSH", "COUNT", "COVERAGE", 
		"COVERAGE_VARIABLE_NAME_PREFIX", "CRS_TRANSFORM", "DECODE", "DESCRIBE_COVERAGE", 
		"DIV", "DIVISION", "DOT", "ENCODE", "EQUAL", "EXP", "EXTEND", "FALSE", 
		"GREATER_THAN", "GREATER_THAN_SLASH", "GREATER_OR_EQUAL_THAN", "IMAGINARY_PART", 
		"IMGCRSDOMAIN", "IN", "LEFT_BRACE", "LEFT_BRACKET", "LEFT_PARANTHESIS", 
		"LET", "LN", "LIST", "LOG", "LOWER_THAN", "LOWER_THAN_SLASH", "LOWER_OR_EQUAL_THAN", 
		"MAX", "MIN", "MINUS", "MIXED", "MOD", "MULTIPLICATION", "NOT", "NOT_EQUAL", 
		"OR", "OVER", "OVERLAY", "PLUS", "POWER", "REAL_PART", "ROUND", "RETURN", 
		"RIGHT_BRACE", "RIGHT_BRACKET", "RIGHT_PARANTHESIS", "SCALE", "SEMICOLON", 
		"SIN", "SINH", "SLICE", "SOME", "SQUARE_ROOT", "STRUCT", "TAN", "TANH", 
		"TRIM", "TRUE", "USING", "VALUE", "VALUES", "WHERE", "WRAP_RESULT", "XOR", 
		"REAL_NUMBER_CONSTANT", "SIMPLE_IDENTIFIER", "SIMPLE_IDENTIFIER_WITH_NUMBERS", 
		"IDENTIFIER", "NAME", "STRING_LITERAL", "WS", "XPATH_LITERAL", "NCName"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "XPath.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public XPathParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class XpathContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public XpathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_xpath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterXpath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitXpath(this);
		}
	}

	public final XpathContext xpath() throws RecognitionException {
		XpathContext _localctx = new XpathContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_xpath);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			expr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LocationPathContext extends ParserRuleContext {
		public RelativeLocationPathContext relativeLocationPath() {
			return getRuleContext(RelativeLocationPathContext.class,0);
		}
		public AbsoluteLocationPathNorootContext absoluteLocationPathNoroot() {
			return getRuleContext(AbsoluteLocationPathNorootContext.class,0);
		}
		public LocationPathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_locationPath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterLocationPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitLocationPath(this);
		}
	}

	public final LocationPathContext locationPath() throws RecognitionException {
		LocationPathContext _localctx = new LocationPathContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_locationPath);
		try {
			setState(58);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(56);
				relativeLocationPath();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(57);
				absoluteLocationPathNoroot();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AbsoluteLocationPathNorootContext extends ParserRuleContext {
		public RelativeLocationPathContext relativeLocationPath() {
			return getRuleContext(RelativeLocationPathContext.class,0);
		}
		public AbsoluteLocationPathNorootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_absoluteLocationPathNoroot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterAbsoluteLocationPathNoroot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitAbsoluteLocationPathNoroot(this);
		}
	}

	public final AbsoluteLocationPathNorootContext absoluteLocationPathNoroot() throws RecognitionException {
		AbsoluteLocationPathNorootContext _localctx = new AbsoluteLocationPathNorootContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_absoluteLocationPathNoroot);
		try {
			setState(67);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DIVISION:
				enterOuterAlt(_localctx, 1);
				{
				setState(60);
				match(DIVISION);
				setState(62);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
				case 1:
					{
					setState(61);
					relativeLocationPath();
					}
					break;
				}
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(64);
				match(T__0);
				setState(65);
				relativeLocationPath();
				}
				break;
			case T__2:
			case T__3:
			case T__4:
			case NodeType:
			case AxisNameXpath:
			case DOT:
			case MULTIPLICATION:
			case SIMPLE_IDENTIFIER:
			case SIMPLE_IDENTIFIER_WITH_NUMBERS:
			case NCName:
				enterOuterAlt(_localctx, 3);
				{
				setState(66);
				relativeLocationPath();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelativeLocationPathContext extends ParserRuleContext {
		public List<StepContext> step() {
			return getRuleContexts(StepContext.class);
		}
		public StepContext step(int i) {
			return getRuleContext(StepContext.class,i);
		}
		public RelativeLocationPathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relativeLocationPath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterRelativeLocationPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitRelativeLocationPath(this);
		}
	}

	public final RelativeLocationPathContext relativeLocationPath() throws RecognitionException {
		RelativeLocationPathContext _localctx = new RelativeLocationPathContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_relativeLocationPath);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			step();
			setState(74);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0 || _la==DIVISION) {
				{
				{
				setState(70);
				_la = _input.LA(1);
				if ( !(_la==T__0 || _la==DIVISION) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(71);
				step();
				}
				}
				setState(76);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StepContext extends ParserRuleContext {
		public AxisSpecifierContext axisSpecifier() {
			return getRuleContext(AxisSpecifierContext.class,0);
		}
		public NodeTestContext nodeTest() {
			return getRuleContext(NodeTestContext.class,0);
		}
		public List<PredicateContext> predicate() {
			return getRuleContexts(PredicateContext.class);
		}
		public PredicateContext predicate(int i) {
			return getRuleContext(PredicateContext.class,i);
		}
		public AbbreviatedStepContext abbreviatedStep() {
			return getRuleContext(AbbreviatedStepContext.class,0);
		}
		public StepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_step; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterStep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitStep(this);
		}
	}

	public final StepContext step() throws RecognitionException {
		StepContext _localctx = new StepContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_step);
		int _la;
		try {
			setState(86);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__2:
			case T__3:
			case NodeType:
			case AxisNameXpath:
			case MULTIPLICATION:
			case SIMPLE_IDENTIFIER:
			case SIMPLE_IDENTIFIER_WITH_NUMBERS:
			case NCName:
				enterOuterAlt(_localctx, 1);
				{
				setState(77);
				axisSpecifier();
				setState(78);
				nodeTest();
				setState(82);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==LEFT_BRACKET) {
					{
					{
					setState(79);
					predicate();
					}
					}
					setState(84);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case T__4:
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(85);
				abbreviatedStep();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AxisSpecifierContext extends ParserRuleContext {
		public TerminalNode AxisNameXpath() { return getToken(XPathParser.AxisNameXpath, 0); }
		public AxisSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_axisSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterAxisSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitAxisSpecifier(this);
		}
	}

	public final AxisSpecifierContext axisSpecifier() throws RecognitionException {
		AxisSpecifierContext _localctx = new AxisSpecifierContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_axisSpecifier);
		int _la;
		try {
			setState(93);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(88);
				match(AxisNameXpath);
				setState(89);
				match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(91);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__2) {
					{
					setState(90);
					match(T__2);
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NodeTestContext extends ParserRuleContext {
		public NameTestContext nameTest() {
			return getRuleContext(NameTestContext.class,0);
		}
		public TerminalNode NodeType() { return getToken(XPathParser.NodeType, 0); }
		public TerminalNode XPATH_LITERAL() { return getToken(XPathParser.XPATH_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(XPathParser.STRING_LITERAL, 0); }
		public NodeTestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeTest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterNodeTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitNodeTest(this);
		}
	}

	public final NodeTestContext nodeTest() throws RecognitionException {
		NodeTestContext _localctx = new NodeTestContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_nodeTest);
		int _la;
		try {
			setState(103);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AxisNameXpath:
			case MULTIPLICATION:
			case SIMPLE_IDENTIFIER:
			case SIMPLE_IDENTIFIER_WITH_NUMBERS:
			case NCName:
				enterOuterAlt(_localctx, 1);
				{
				setState(95);
				nameTest();
				}
				break;
			case NodeType:
				enterOuterAlt(_localctx, 2);
				{
				setState(96);
				match(NodeType);
				setState(97);
				match(LEFT_PARANTHESIS);
				setState(98);
				match(RIGHT_PARANTHESIS);
				}
				break;
			case T__3:
				enterOuterAlt(_localctx, 3);
				{
				setState(99);
				match(T__3);
				setState(100);
				match(LEFT_PARANTHESIS);
				setState(101);
				_la = _input.LA(1);
				if ( !(_la==STRING_LITERAL || _la==XPATH_LITERAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(102);
				match(RIGHT_PARANTHESIS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PredicateContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitPredicate(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_predicate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			match(LEFT_BRACKET);
			setState(106);
			expr();
			setState(107);
			match(RIGHT_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AbbreviatedStepContext extends ParserRuleContext {
		public AbbreviatedStepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_abbreviatedStep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterAbbreviatedStep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitAbbreviatedStep(this);
		}
	}

	public final AbbreviatedStepContext abbreviatedStep() throws RecognitionException {
		AbbreviatedStepContext _localctx = new AbbreviatedStepContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_abbreviatedStep);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			_la = _input.LA(1);
			if ( !(_la==T__4 || _la==DOT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public OrExprContext orExpr() {
			return getRuleContext(OrExprContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			orExpr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrimaryExprContext extends ParserRuleContext {
		public VariableReferenceContext variableReference() {
			return getRuleContext(VariableReferenceContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode XPATH_LITERAL() { return getToken(XPathParser.XPATH_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(XPathParser.STRING_LITERAL, 0); }
		public TerminalNode REAL_NUMBER_CONSTANT() { return getToken(XPathParser.REAL_NUMBER_CONSTANT, 0); }
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public PrimaryExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterPrimaryExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitPrimaryExpr(this);
		}
	}

	public final PrimaryExprContext primaryExpr() throws RecognitionException {
		PrimaryExprContext _localctx = new PrimaryExprContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_primaryExpr);
		int _la;
		try {
			setState(121);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case COVERAGE_VARIABLE_NAME_PREFIX:
				enterOuterAlt(_localctx, 1);
				{
				setState(113);
				variableReference();
				}
				break;
			case LEFT_PARANTHESIS:
				enterOuterAlt(_localctx, 2);
				{
				setState(114);
				match(LEFT_PARANTHESIS);
				setState(115);
				expr();
				setState(116);
				match(RIGHT_PARANTHESIS);
				}
				break;
			case STRING_LITERAL:
			case XPATH_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(118);
				_la = _input.LA(1);
				if ( !(_la==STRING_LITERAL || _la==XPATH_LITERAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case REAL_NUMBER_CONSTANT:
				enterOuterAlt(_localctx, 4);
				{
				setState(119);
				match(REAL_NUMBER_CONSTANT);
				}
				break;
			case AxisNameXpath:
			case SIMPLE_IDENTIFIER:
			case SIMPLE_IDENTIFIER_WITH_NUMBERS:
			case NCName:
				enterOuterAlt(_localctx, 5);
				{
				setState(120);
				functionCall();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionCallContext extends ParserRuleContext {
		public FunctionNameContext functionName() {
			return getRuleContext(FunctionNameContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitFunctionCall(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			functionName();
			setState(124);
			match(LEFT_PARANTHESIS);
			setState(133);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << NodeType) | (1L << AxisNameXpath) | (1L << COVERAGE_VARIABLE_NAME_PREFIX) | (1L << DIVISION) | (1L << DOT) | (1L << LEFT_PARANTHESIS) | (1L << MINUS) | (1L << MULTIPLICATION))) != 0) || ((((_la - 91)) & ~0x3f) == 0 && ((1L << (_la - 91)) & ((1L << (REAL_NUMBER_CONSTANT - 91)) | (1L << (SIMPLE_IDENTIFIER - 91)) | (1L << (SIMPLE_IDENTIFIER_WITH_NUMBERS - 91)) | (1L << (STRING_LITERAL - 91)) | (1L << (XPATH_LITERAL - 91)) | (1L << (NCName - 91)))) != 0)) {
				{
				setState(125);
				expr();
				setState(130);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(126);
					match(COMMA);
					setState(127);
					expr();
					}
					}
					setState(132);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(135);
			match(RIGHT_PARANTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnionExprNoRootContext extends ParserRuleContext {
		public PathExprNoRootContext pathExprNoRoot() {
			return getRuleContext(PathExprNoRootContext.class,0);
		}
		public UnionExprNoRootContext unionExprNoRoot() {
			return getRuleContext(UnionExprNoRootContext.class,0);
		}
		public UnionExprNoRootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionExprNoRoot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterUnionExprNoRoot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitUnionExprNoRoot(this);
		}
	}

	public final UnionExprNoRootContext unionExprNoRoot() throws RecognitionException {
		UnionExprNoRootContext _localctx = new UnionExprNoRootContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_unionExprNoRoot);
		int _la;
		try {
			setState(145);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(137);
				pathExprNoRoot();
				setState(140);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__5) {
					{
					setState(138);
					match(T__5);
					setState(139);
					unionExprNoRoot();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(142);
				match(DIVISION);
				setState(143);
				match(T__5);
				setState(144);
				unionExprNoRoot();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PathExprNoRootContext extends ParserRuleContext {
		public LocationPathContext locationPath() {
			return getRuleContext(LocationPathContext.class,0);
		}
		public FilterExprContext filterExpr() {
			return getRuleContext(FilterExprContext.class,0);
		}
		public RelativeLocationPathContext relativeLocationPath() {
			return getRuleContext(RelativeLocationPathContext.class,0);
		}
		public PathExprNoRootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathExprNoRoot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterPathExprNoRoot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitPathExprNoRoot(this);
		}
	}

	public final PathExprNoRootContext pathExprNoRoot() throws RecognitionException {
		PathExprNoRootContext _localctx = new PathExprNoRootContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_pathExprNoRoot);
		int _la;
		try {
			setState(153);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(147);
				locationPath();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(148);
				filterExpr();
				setState(151);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0 || _la==DIVISION) {
					{
					setState(149);
					_la = _input.LA(1);
					if ( !(_la==T__0 || _la==DIVISION) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(150);
					relativeLocationPath();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FilterExprContext extends ParserRuleContext {
		public PrimaryExprContext primaryExpr() {
			return getRuleContext(PrimaryExprContext.class,0);
		}
		public List<PredicateContext> predicate() {
			return getRuleContexts(PredicateContext.class);
		}
		public PredicateContext predicate(int i) {
			return getRuleContext(PredicateContext.class,i);
		}
		public FilterExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterFilterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitFilterExpr(this);
		}
	}

	public final FilterExprContext filterExpr() throws RecognitionException {
		FilterExprContext _localctx = new FilterExprContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_filterExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			primaryExpr();
			setState(159);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LEFT_BRACKET) {
				{
				{
				setState(156);
				predicate();
				}
				}
				setState(161);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrExprContext extends ParserRuleContext {
		public List<AndExprContext> andExpr() {
			return getRuleContexts(AndExprContext.class);
		}
		public AndExprContext andExpr(int i) {
			return getRuleContext(AndExprContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(XPathParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(XPathParser.OR, i);
		}
		public OrExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterOrExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitOrExpr(this);
		}
	}

	public final OrExprContext orExpr() throws RecognitionException {
		OrExprContext _localctx = new OrExprContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_orExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			andExpr();
			setState(167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(163);
				match(OR);
				setState(164);
				andExpr();
				}
				}
				setState(169);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AndExprContext extends ParserRuleContext {
		public List<EqualityExprContext> equalityExpr() {
			return getRuleContexts(EqualityExprContext.class);
		}
		public EqualityExprContext equalityExpr(int i) {
			return getRuleContext(EqualityExprContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(XPathParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(XPathParser.AND, i);
		}
		public AndExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_andExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterAndExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitAndExpr(this);
		}
	}

	public final AndExprContext andExpr() throws RecognitionException {
		AndExprContext _localctx = new AndExprContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_andExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(170);
			equalityExpr();
			setState(175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(171);
				match(AND);
				setState(172);
				equalityExpr();
				}
				}
				setState(177);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EqualityExprContext extends ParserRuleContext {
		public List<RelationalExprContext> relationalExpr() {
			return getRuleContexts(RelationalExprContext.class);
		}
		public RelationalExprContext relationalExpr(int i) {
			return getRuleContext(RelationalExprContext.class,i);
		}
		public EqualityExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equalityExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterEqualityExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitEqualityExpr(this);
		}
	}

	public final EqualityExprContext equalityExpr() throws RecognitionException {
		EqualityExprContext _localctx = new EqualityExprContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_equalityExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			relationalExpr();
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==EQUAL || _la==NOT_EQUAL) {
				{
				{
				setState(179);
				_la = _input.LA(1);
				if ( !(_la==EQUAL || _la==NOT_EQUAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(180);
				relationalExpr();
				}
				}
				setState(185);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationalExprContext extends ParserRuleContext {
		public List<AdditiveExprContext> additiveExpr() {
			return getRuleContexts(AdditiveExprContext.class);
		}
		public AdditiveExprContext additiveExpr(int i) {
			return getRuleContext(AdditiveExprContext.class,i);
		}
		public List<TerminalNode> LOWER_THAN() { return getTokens(XPathParser.LOWER_THAN); }
		public TerminalNode LOWER_THAN(int i) {
			return getToken(XPathParser.LOWER_THAN, i);
		}
		public List<TerminalNode> GREATER_THAN() { return getTokens(XPathParser.GREATER_THAN); }
		public TerminalNode GREATER_THAN(int i) {
			return getToken(XPathParser.GREATER_THAN, i);
		}
		public List<TerminalNode> LOWER_OR_EQUAL_THAN() { return getTokens(XPathParser.LOWER_OR_EQUAL_THAN); }
		public TerminalNode LOWER_OR_EQUAL_THAN(int i) {
			return getToken(XPathParser.LOWER_OR_EQUAL_THAN, i);
		}
		public List<TerminalNode> GREATER_OR_EQUAL_THAN() { return getTokens(XPathParser.GREATER_OR_EQUAL_THAN); }
		public TerminalNode GREATER_OR_EQUAL_THAN(int i) {
			return getToken(XPathParser.GREATER_OR_EQUAL_THAN, i);
		}
		public RelationalExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationalExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterRelationalExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitRelationalExpr(this);
		}
	}

	public final RelationalExprContext relationalExpr() throws RecognitionException {
		RelationalExprContext _localctx = new RelationalExprContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_relationalExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
			additiveExpr();
			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GREATER_THAN) | (1L << GREATER_OR_EQUAL_THAN) | (1L << LOWER_THAN) | (1L << LOWER_OR_EQUAL_THAN))) != 0)) {
				{
				{
				setState(187);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GREATER_THAN) | (1L << GREATER_OR_EQUAL_THAN) | (1L << LOWER_THAN) | (1L << LOWER_OR_EQUAL_THAN))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(188);
				additiveExpr();
				}
				}
				setState(193);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AdditiveExprContext extends ParserRuleContext {
		public List<MultiplicativeExprContext> multiplicativeExpr() {
			return getRuleContexts(MultiplicativeExprContext.class);
		}
		public MultiplicativeExprContext multiplicativeExpr(int i) {
			return getRuleContext(MultiplicativeExprContext.class,i);
		}
		public AdditiveExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_additiveExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterAdditiveExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitAdditiveExpr(this);
		}
	}

	public final AdditiveExprContext additiveExpr() throws RecognitionException {
		AdditiveExprContext _localctx = new AdditiveExprContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_additiveExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			multiplicativeExpr();
			setState(199);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MINUS || _la==PLUS) {
				{
				{
				setState(195);
				_la = _input.LA(1);
				if ( !(_la==MINUS || _la==PLUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(196);
				multiplicativeExpr();
				}
				}
				setState(201);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultiplicativeExprContext extends ParserRuleContext {
		public UnaryExprNoRootContext unaryExprNoRoot() {
			return getRuleContext(UnaryExprNoRootContext.class,0);
		}
		public MultiplicativeExprContext multiplicativeExpr() {
			return getRuleContext(MultiplicativeExprContext.class,0);
		}
		public TerminalNode DIV() { return getToken(XPathParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(XPathParser.MOD, 0); }
		public MultiplicativeExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiplicativeExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterMultiplicativeExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitMultiplicativeExpr(this);
		}
	}

	public final MultiplicativeExprContext multiplicativeExpr() throws RecognitionException {
		MultiplicativeExprContext _localctx = new MultiplicativeExprContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_multiplicativeExpr);
		int _la;
		try {
			setState(212);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(202);
				unaryExprNoRoot();
				setState(205);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DIV) | (1L << MOD) | (1L << MULTIPLICATION))) != 0)) {
					{
					setState(203);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DIV) | (1L << MOD) | (1L << MULTIPLICATION))) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(204);
					multiplicativeExpr();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(207);
				match(DIVISION);
				setState(210);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DIV || _la==MOD) {
					{
					setState(208);
					_la = _input.LA(1);
					if ( !(_la==DIV || _la==MOD) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(209);
					multiplicativeExpr();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnaryExprNoRootContext extends ParserRuleContext {
		public UnionExprNoRootContext unionExprNoRoot() {
			return getRuleContext(UnionExprNoRootContext.class,0);
		}
		public UnaryExprNoRootContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryExprNoRoot; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterUnaryExprNoRoot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitUnaryExprNoRoot(this);
		}
	}

	public final UnaryExprNoRootContext unaryExprNoRoot() throws RecognitionException {
		UnaryExprNoRootContext _localctx = new UnaryExprNoRootContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_unaryExprNoRoot);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(217);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MINUS) {
				{
				{
				setState(214);
				match(MINUS);
				}
				}
				setState(219);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(220);
			unionExprNoRoot();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QNameContext extends ParserRuleContext {
		public List<NCNameContext> nCName() {
			return getRuleContexts(NCNameContext.class);
		}
		public NCNameContext nCName(int i) {
			return getRuleContext(NCNameContext.class,i);
		}
		public QNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterQName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitQName(this);
		}
	}

	public final QNameContext qName() throws RecognitionException {
		QNameContext _localctx = new QNameContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_qName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(222);
			nCName();
			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(223);
				match(COLON);
				setState(224);
				nCName();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionNameContext extends ParserRuleContext {
		public QNameContext qName() {
			return getRuleContext(QNameContext.class,0);
		}
		public FunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitFunctionName(this);
		}
	}

	public final FunctionNameContext functionName() throws RecognitionException {
		FunctionNameContext _localctx = new FunctionNameContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_functionName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(227);
			qName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableReferenceContext extends ParserRuleContext {
		public QNameContext qName() {
			return getRuleContext(QNameContext.class,0);
		}
		public VariableReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterVariableReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitVariableReference(this);
		}
	}

	public final VariableReferenceContext variableReference() throws RecognitionException {
		VariableReferenceContext _localctx = new VariableReferenceContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_variableReference);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			match(COVERAGE_VARIABLE_NAME_PREFIX);
			setState(230);
			qName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NameTestContext extends ParserRuleContext {
		public NCNameContext nCName() {
			return getRuleContext(NCNameContext.class,0);
		}
		public QNameContext qName() {
			return getRuleContext(QNameContext.class,0);
		}
		public NameTestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nameTest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterNameTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitNameTest(this);
		}
	}

	public final NameTestContext nameTest() throws RecognitionException {
		NameTestContext _localctx = new NameTestContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_nameTest);
		try {
			setState(238);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(232);
				match(MULTIPLICATION);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(233);
				nCName();
				setState(234);
				match(COLON);
				setState(235);
				match(MULTIPLICATION);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(237);
				qName();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NCNameContext extends ParserRuleContext {
		public TerminalNode NCName() { return getToken(XPathParser.NCName, 0); }
		public TerminalNode SIMPLE_IDENTIFIER_WITH_NUMBERS() { return getToken(XPathParser.SIMPLE_IDENTIFIER_WITH_NUMBERS, 0); }
		public TerminalNode SIMPLE_IDENTIFIER() { return getToken(XPathParser.SIMPLE_IDENTIFIER, 0); }
		public TerminalNode AxisNameXpath() { return getToken(XPathParser.AxisNameXpath, 0); }
		public NCNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nCName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).enterNCName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof XPathListener ) ((XPathListener)listener).exitNCName(this);
		}
	}

	public final NCNameContext nCName() throws RecognitionException {
		NCNameContext _localctx = new NCNameContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_nCName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(240);
			_la = _input.LA(1);
			if ( !(_la==AxisNameXpath || ((((_la - 92)) & ~0x3f) == 0 && ((1L << (_la - 92)) & ((1L << (SIMPLE_IDENTIFIER - 92)) | (1L << (SIMPLE_IDENTIFIER_WITH_NUMBERS - 92)) | (1L << (NCName - 92)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3e\u00f5\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\3\2\3\2\3\3\3\3\5\3=\n\3\3\4\3\4\5\4A\n"+
		"\4\3\4\3\4\3\4\5\4F\n\4\3\5\3\5\3\5\7\5K\n\5\f\5\16\5N\13\5\3\6\3\6\3"+
		"\6\7\6S\n\6\f\6\16\6V\13\6\3\6\5\6Y\n\6\3\7\3\7\3\7\5\7^\n\7\5\7`\n\7"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\bj\n\b\3\t\3\t\3\t\3\t\3\n\3\n\3\13"+
		"\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\5\f|\n\f\3\r\3\r\3\r\3\r\3\r\7\r"+
		"\u0083\n\r\f\r\16\r\u0086\13\r\5\r\u0088\n\r\3\r\3\r\3\16\3\16\3\16\5"+
		"\16\u008f\n\16\3\16\3\16\3\16\5\16\u0094\n\16\3\17\3\17\3\17\3\17\5\17"+
		"\u009a\n\17\5\17\u009c\n\17\3\20\3\20\7\20\u00a0\n\20\f\20\16\20\u00a3"+
		"\13\20\3\21\3\21\3\21\7\21\u00a8\n\21\f\21\16\21\u00ab\13\21\3\22\3\22"+
		"\3\22\7\22\u00b0\n\22\f\22\16\22\u00b3\13\22\3\23\3\23\3\23\7\23\u00b8"+
		"\n\23\f\23\16\23\u00bb\13\23\3\24\3\24\3\24\7\24\u00c0\n\24\f\24\16\24"+
		"\u00c3\13\24\3\25\3\25\3\25\7\25\u00c8\n\25\f\25\16\25\u00cb\13\25\3\26"+
		"\3\26\3\26\5\26\u00d0\n\26\3\26\3\26\3\26\5\26\u00d5\n\26\5\26\u00d7\n"+
		"\26\3\27\7\27\u00da\n\27\f\27\16\27\u00dd\13\27\3\27\3\27\3\30\3\30\3"+
		"\30\5\30\u00e4\n\30\3\31\3\31\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\5\33\u00f1\n\33\3\34\3\34\3\34\2\2\35\2\4\6\b\n\f\16\20\22\24\26"+
		"\30\32\34\36 \"$&(*,.\60\62\64\66\2\13\4\2\3\3!!\4\2bbdd\4\2\7\7\"\"\4"+
		"\2$$??\6\2((**\65\65\67\67\4\2::CC\4\2  <=\4\2  <<\5\2\n\n^_ee\u00fb\2"+
		"8\3\2\2\2\4<\3\2\2\2\6E\3\2\2\2\bG\3\2\2\2\nX\3\2\2\2\f_\3\2\2\2\16i\3"+
		"\2\2\2\20k\3\2\2\2\22o\3\2\2\2\24q\3\2\2\2\26{\3\2\2\2\30}\3\2\2\2\32"+
		"\u0093\3\2\2\2\34\u009b\3\2\2\2\36\u009d\3\2\2\2 \u00a4\3\2\2\2\"\u00ac"+
		"\3\2\2\2$\u00b4\3\2\2\2&\u00bc\3\2\2\2(\u00c4\3\2\2\2*\u00d6\3\2\2\2,"+
		"\u00db\3\2\2\2.\u00e0\3\2\2\2\60\u00e5\3\2\2\2\62\u00e7\3\2\2\2\64\u00f0"+
		"\3\2\2\2\66\u00f2\3\2\2\289\5\24\13\29\3\3\2\2\2:=\5\b\5\2;=\5\6\4\2<"+
		":\3\2\2\2<;\3\2\2\2=\5\3\2\2\2>@\7!\2\2?A\5\b\5\2@?\3\2\2\2@A\3\2\2\2"+
		"AF\3\2\2\2BC\7\3\2\2CF\5\b\5\2DF\5\b\5\2E>\3\2\2\2EB\3\2\2\2ED\3\2\2\2"+
		"F\7\3\2\2\2GL\5\n\6\2HI\t\2\2\2IK\5\n\6\2JH\3\2\2\2KN\3\2\2\2LJ\3\2\2"+
		"\2LM\3\2\2\2M\t\3\2\2\2NL\3\2\2\2OP\5\f\7\2PT\5\16\b\2QS\5\20\t\2RQ\3"+
		"\2\2\2SV\3\2\2\2TR\3\2\2\2TU\3\2\2\2UY\3\2\2\2VT\3\2\2\2WY\5\22\n\2XO"+
		"\3\2\2\2XW\3\2\2\2Y\13\3\2\2\2Z[\7\n\2\2[`\7\4\2\2\\^\7\5\2\2]\\\3\2\2"+
		"\2]^\3\2\2\2^`\3\2\2\2_Z\3\2\2\2_]\3\2\2\2`\r\3\2\2\2aj\5\64\33\2bc\7"+
		"\t\2\2cd\7\60\2\2dj\7J\2\2ef\7\6\2\2fg\7\60\2\2gh\t\3\2\2hj\7J\2\2ia\3"+
		"\2\2\2ib\3\2\2\2ie\3\2\2\2j\17\3\2\2\2kl\7/\2\2lm\5\24\13\2mn\7I\2\2n"+
		"\21\3\2\2\2op\t\4\2\2p\23\3\2\2\2qr\5 \21\2r\25\3\2\2\2s|\5\62\32\2tu"+
		"\7\60\2\2uv\5\24\13\2vw\7J\2\2w|\3\2\2\2x|\t\3\2\2y|\7]\2\2z|\5\30\r\2"+
		"{s\3\2\2\2{t\3\2\2\2{x\3\2\2\2{y\3\2\2\2{z\3\2\2\2|\27\3\2\2\2}~\5\60"+
		"\31\2~\u0087\7\60\2\2\177\u0084\5\24\13\2\u0080\u0081\7\26\2\2\u0081\u0083"+
		"\5\24\13\2\u0082\u0080\3\2\2\2\u0083\u0086\3\2\2\2\u0084\u0082\3\2\2\2"+
		"\u0084\u0085\3\2\2\2\u0085\u0088\3\2\2\2\u0086\u0084\3\2\2\2\u0087\177"+
		"\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u0089\3\2\2\2\u0089\u008a\7J\2\2\u008a"+
		"\31\3\2\2\2\u008b\u008e\5\34\17\2\u008c\u008d\7\b\2\2\u008d\u008f\5\32"+
		"\16\2\u008e\u008c\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u0094\3\2\2\2\u0090"+
		"\u0091\7!\2\2\u0091\u0092\7\b\2\2\u0092\u0094\5\32\16\2\u0093\u008b\3"+
		"\2\2\2\u0093\u0090\3\2\2\2\u0094\33\3\2\2\2\u0095\u009c\5\4\3\2\u0096"+
		"\u0099\5\36\20\2\u0097\u0098\t\2\2\2\u0098\u009a\5\b\5\2\u0099\u0097\3"+
		"\2\2\2\u0099\u009a\3\2\2\2\u009a\u009c\3\2\2\2\u009b\u0095\3\2\2\2\u009b"+
		"\u0096\3\2\2\2\u009c\35\3\2\2\2\u009d\u00a1\5\26\f\2\u009e\u00a0\5\20"+
		"\t\2\u009f\u009e\3\2\2\2\u00a0\u00a3\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1"+
		"\u00a2\3\2\2\2\u00a2\37\3\2\2\2\u00a3\u00a1\3\2\2\2\u00a4\u00a9\5\"\22"+
		"\2\u00a5\u00a6\7@\2\2\u00a6\u00a8\5\"\22\2\u00a7\u00a5\3\2\2\2\u00a8\u00ab"+
		"\3\2\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa!\3\2\2\2\u00ab"+
		"\u00a9\3\2\2\2\u00ac\u00b1\5$\23\2\u00ad\u00ae\7\17\2\2\u00ae\u00b0\5"+
		"$\23\2\u00af\u00ad\3\2\2\2\u00b0\u00b3\3\2\2\2\u00b1\u00af\3\2\2\2\u00b1"+
		"\u00b2\3\2\2\2\u00b2#\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b4\u00b9\5&\24\2"+
		"\u00b5\u00b6\t\5\2\2\u00b6\u00b8\5&\24\2\u00b7\u00b5\3\2\2\2\u00b8\u00bb"+
		"\3\2\2\2\u00b9\u00b7\3\2\2\2\u00b9\u00ba\3\2\2\2\u00ba%\3\2\2\2\u00bb"+
		"\u00b9\3\2\2\2\u00bc\u00c1\5(\25\2\u00bd\u00be\t\6\2\2\u00be\u00c0\5("+
		"\25\2\u00bf\u00bd\3\2\2\2\u00c0\u00c3\3\2\2\2\u00c1\u00bf\3\2\2\2\u00c1"+
		"\u00c2\3\2\2\2\u00c2\'\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c4\u00c9\5*\26\2"+
		"\u00c5\u00c6\t\7\2\2\u00c6\u00c8\5*\26\2\u00c7\u00c5\3\2\2\2\u00c8\u00cb"+
		"\3\2\2\2\u00c9\u00c7\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca)\3\2\2\2\u00cb"+
		"\u00c9\3\2\2\2\u00cc\u00cf\5,\27\2\u00cd\u00ce\t\b\2\2\u00ce\u00d0\5*"+
		"\26\2\u00cf\u00cd\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\u00d7\3\2\2\2\u00d1"+
		"\u00d4\7!\2\2\u00d2\u00d3\t\t\2\2\u00d3\u00d5\5*\26\2\u00d4\u00d2\3\2"+
		"\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d7\3\2\2\2\u00d6\u00cc\3\2\2\2\u00d6"+
		"\u00d1\3\2\2\2\u00d7+\3\2\2\2\u00d8\u00da\7:\2\2\u00d9\u00d8\3\2\2\2\u00da"+
		"\u00dd\3\2\2\2\u00db\u00d9\3\2\2\2\u00db\u00dc\3\2\2\2\u00dc\u00de\3\2"+
		"\2\2\u00dd\u00db\3\2\2\2\u00de\u00df\5\32\16\2\u00df-\3\2\2\2\u00e0\u00e3"+
		"\5\66\34\2\u00e1\u00e2\7\25\2\2\u00e2\u00e4\5\66\34\2\u00e3\u00e1\3\2"+
		"\2\2\u00e3\u00e4\3\2\2\2\u00e4/\3\2\2\2\u00e5\u00e6\5.\30\2\u00e6\61\3"+
		"\2\2\2\u00e7\u00e8\7\34\2\2\u00e8\u00e9\5.\30\2\u00e9\63\3\2\2\2\u00ea"+
		"\u00f1\7=\2\2\u00eb\u00ec\5\66\34\2\u00ec\u00ed\7\25\2\2\u00ed\u00ee\7"+
		"=\2\2\u00ee\u00f1\3\2\2\2\u00ef\u00f1\5.\30\2\u00f0\u00ea\3\2\2\2\u00f0"+
		"\u00eb\3\2\2\2\u00f0\u00ef\3\2\2\2\u00f1\65\3\2\2\2\u00f2\u00f3\t\n\2"+
		"\2\u00f3\67\3\2\2\2\36<@ELTX]_i{\u0084\u0087\u008e\u0093\u0099\u009b\u00a1"+
		"\u00a9\u00b1\u00b9\u00c1\u00c9\u00cf\u00d4\u00d6\u00db\u00e3\u00f0";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}