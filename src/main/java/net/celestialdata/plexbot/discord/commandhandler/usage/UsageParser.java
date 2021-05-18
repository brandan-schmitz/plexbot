// Generated from net/kautler/command/usage/Usage.g4 by ANTLR 4.7.2

/*
 * Copyright 2019 Björn Kautler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.celestialdata.plexbot.discord.commandhandler.usage;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class UsageParser extends Parser {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, PLACEHOLDER_WITH_WHITESPACE = 6,
            PLACEHOLDER = 7, LITERAL = 8, WS = 9;
    public static final int
            RULE_usage = 0, RULE_expression = 1, RULE_optionalSubExpression = 2, RULE_alternativesSubExpression = 3,
            RULE_optional = 4, RULE_alternatives = 5, RULE_placeholder = 6, RULE_placeholderWithWhitespace = 7,
            RULE_literal = 8;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\13N\4\2\t\2\4\3\t" +
                    "\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\6\2\26" +
                    "\n\2\r\2\16\2\27\3\2\3\2\3\3\3\3\3\3\3\3\3\3\5\3!\n\3\3\4\3\4\6\4%\n\4" +
                    "\r\4\16\4&\3\4\3\4\3\4\3\4\5\4-\n\4\3\5\3\5\6\5\61\n\5\r\5\16\5\62\3\5" +
                    "\3\5\3\5\5\58\n\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\6\7B\n\7\r\7\16\7C\3" +
                    "\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\2\2\13\2\4\6\b\n\f\16\20\22\2\2\2S" +
                    "\2\25\3\2\2\2\4 \3\2\2\2\6,\3\2\2\2\b\67\3\2\2\2\n9\3\2\2\2\f=\3\2\2\2" +
                    "\16G\3\2\2\2\20I\3\2\2\2\22K\3\2\2\2\24\26\5\4\3\2\25\24\3\2\2\2\26\27" +
                    "\3\2\2\2\27\25\3\2\2\2\27\30\3\2\2\2\30\31\3\2\2\2\31\32\7\2\2\3\32\3" +
                    "\3\2\2\2\33!\5\n\6\2\34!\5\16\b\2\35!\5\20\t\2\36!\5\f\7\2\37!\5\22\n" +
                    "\2 \33\3\2\2\2 \34\3\2\2\2 \35\3\2\2\2 \36\3\2\2\2 \37\3\2\2\2!\5\3\2" +
                    "\2\2\"$\5\4\3\2#%\5\4\3\2$#\3\2\2\2%&\3\2\2\2&$\3\2\2\2&\'\3\2\2\2\'-" +
                    "\3\2\2\2(-\5\16\b\2)-\5\20\t\2*-\5\f\7\2+-\5\22\n\2,\"\3\2\2\2,(\3\2\2" +
                    "\2,)\3\2\2\2,*\3\2\2\2,+\3\2\2\2-\7\3\2\2\2.\60\5\4\3\2/\61\5\4\3\2\60" +
                    "/\3\2\2\2\61\62\3\2\2\2\62\60\3\2\2\2\62\63\3\2\2\2\638\3\2\2\2\648\5" +
                    "\16\b\2\658\5\20\t\2\668\5\22\n\2\67.\3\2\2\2\67\64\3\2\2\2\67\65\3\2" +
                    "\2\2\67\66\3\2\2\28\t\3\2\2\29:\7\3\2\2:;\5\6\4\2;<\7\4\2\2<\13\3\2\2" +
                    "\2=>\7\5\2\2>A\5\b\5\2?@\7\6\2\2@B\5\b\5\2A?\3\2\2\2BC\3\2\2\2CA\3\2\2" +
                    "\2CD\3\2\2\2DE\3\2\2\2EF\7\7\2\2F\r\3\2\2\2GH\7\t\2\2H\17\3\2\2\2IJ\7" +
                    "\b\2\2J\21\3\2\2\2KL\7\n\2\2L\23\3\2\2\2\t\27 &,\62\67C";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = makeLiteralNames();
    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    static {
        RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION);
    }

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

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }

    {
        removeErrorListeners();
        addErrorListener(new UsageErrorListener());
    }

    public UsageParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    private static String[] makeRuleNames() {
        return new String[]{
                "usage", "expression", "optionalSubExpression", "alternativesSubExpression",
                "optional", "alternatives", "placeholder", "placeholderWithWhitespace",
                "literal"
        };
    }

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'['", "']'", "'('", "'|'", "')'"
        };
    }

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, null, null, "PLACEHOLDER_WITH_WHITESPACE", "PLACEHOLDER",
                "LITERAL", "WS"
        };
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
    public String getGrammarFileName() {
        return "Usage.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public final UsageContext usage() throws RecognitionException {
        UsageContext _localctx = new UsageContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_usage);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(19);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        {
                            setState(18);
                            expression();
                        }
                    }
                    setState(21);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__2) | (1L << PLACEHOLDER_WITH_WHITESPACE) | (1L << PLACEHOLDER) | (1L << LITERAL))) != 0));
                setState(23);
                match(EOF);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ExpressionContext expression() throws RecognitionException {
        ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_expression);
        try {
            setState(30);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case T__0:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(25);
                    optional();
                }
                break;
                case PLACEHOLDER:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(26);
                    placeholder();
                }
                break;
                case PLACEHOLDER_WITH_WHITESPACE:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(27);
                    placeholderWithWhitespace();
                }
                break;
                case T__2:
                    enterOuterAlt(_localctx, 4);
                {
                    setState(28);
                    alternatives();
                }
                break;
                case LITERAL:
                    enterOuterAlt(_localctx, 5);
                {
                    setState(29);
                    literal();
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final OptionalSubExpressionContext optionalSubExpression() throws RecognitionException {
        OptionalSubExpressionContext _localctx = new OptionalSubExpressionContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_optionalSubExpression);
        int _la;
        try {
            setState(42);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 3, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(32);
                    expression();
                    setState(34);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    do {
                        {
                            {
                                setState(33);
                                expression();
                            }
                        }
                        setState(36);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__2) | (1L << PLACEHOLDER_WITH_WHITESPACE) | (1L << PLACEHOLDER) | (1L << LITERAL))) != 0));
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(38);
                    placeholder();
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(39);
                    placeholderWithWhitespace();
                }
                break;
                case 4:
                    enterOuterAlt(_localctx, 4);
                {
                    setState(40);
                    alternatives();
                }
                break;
                case 5:
                    enterOuterAlt(_localctx, 5);
                {
                    setState(41);
                    literal();
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final AlternativesSubExpressionContext alternativesSubExpression() throws RecognitionException {
        AlternativesSubExpressionContext _localctx = new AlternativesSubExpressionContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_alternativesSubExpression);
        int _la;
        try {
            setState(53);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 5, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(44);
                    expression();
                    setState(46);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    do {
                        {
                            {
                                setState(45);
                                expression();
                            }
                        }
                        setState(48);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__2) | (1L << PLACEHOLDER_WITH_WHITESPACE) | (1L << PLACEHOLDER) | (1L << LITERAL))) != 0));
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(50);
                    placeholder();
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(51);
                    placeholderWithWhitespace();
                }
                break;
                case 4:
                    enterOuterAlt(_localctx, 4);
                {
                    setState(52);
                    literal();
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final OptionalContext optional() throws RecognitionException {
        OptionalContext _localctx = new OptionalContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_optional);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(55);
                match(T__0);
                setState(56);
                optionalSubExpression();
                setState(57);
                match(T__1);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final AlternativesContext alternatives() throws RecognitionException {
        AlternativesContext _localctx = new AlternativesContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_alternatives);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(59);
                match(T__2);
                setState(60);
                alternativesSubExpression();
                setState(63);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        {
                            setState(61);
                            match(T__3);
                            setState(62);
                            alternativesSubExpression();
                        }
                    }
                    setState(65);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while (_la == T__3);
                setState(67);
                match(T__4);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final PlaceholderContext placeholder() throws RecognitionException {
        PlaceholderContext _localctx = new PlaceholderContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_placeholder);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(69);
                match(PLACEHOLDER);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final PlaceholderWithWhitespaceContext placeholderWithWhitespace() throws RecognitionException {
        PlaceholderWithWhitespaceContext _localctx = new PlaceholderWithWhitespaceContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_placeholderWithWhitespace);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(71);
                match(PLACEHOLDER_WITH_WHITESPACE);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final LiteralContext literal() throws RecognitionException {
        LiteralContext _localctx = new LiteralContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_literal);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(73);
                match(LITERAL);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class UsageContext extends UsageParserRuleContext {
        public UsageContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode EOF() {
            return getToken(UsageParser.EOF, 0);
        }

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_usage;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof UsageVisitor) return ((UsageVisitor<? extends T>) visitor).visitUsage(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ExpressionContext extends UsageParserRuleContext {
        public ExpressionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public OptionalContext optional() {
            return getRuleContext(OptionalContext.class, 0);
        }

        public PlaceholderContext placeholder() {
            return getRuleContext(PlaceholderContext.class, 0);
        }

        public PlaceholderWithWhitespaceContext placeholderWithWhitespace() {
            return getRuleContext(PlaceholderWithWhitespaceContext.class, 0);
        }

        public AlternativesContext alternatives() {
            return getRuleContext(AlternativesContext.class, 0);
        }

        public LiteralContext literal() {
            return getRuleContext(LiteralContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_expression;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof UsageVisitor) return ((UsageVisitor<? extends T>) visitor).visitExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class OptionalSubExpressionContext extends UsageParserRuleContext {
        public OptionalSubExpressionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        public PlaceholderContext placeholder() {
            return getRuleContext(PlaceholderContext.class, 0);
        }

        public PlaceholderWithWhitespaceContext placeholderWithWhitespace() {
            return getRuleContext(PlaceholderWithWhitespaceContext.class, 0);
        }

        public AlternativesContext alternatives() {
            return getRuleContext(AlternativesContext.class, 0);
        }

        public LiteralContext literal() {
            return getRuleContext(LiteralContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_optionalSubExpression;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof UsageVisitor)
                return ((UsageVisitor<? extends T>) visitor).visitOptionalSubExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class AlternativesSubExpressionContext extends UsageParserRuleContext {
        public AlternativesSubExpressionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        public PlaceholderContext placeholder() {
            return getRuleContext(PlaceholderContext.class, 0);
        }

        public PlaceholderWithWhitespaceContext placeholderWithWhitespace() {
            return getRuleContext(PlaceholderWithWhitespaceContext.class, 0);
        }

        public LiteralContext literal() {
            return getRuleContext(LiteralContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_alternativesSubExpression;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof UsageVisitor)
                return ((UsageVisitor<? extends T>) visitor).visitAlternativesSubExpression(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class OptionalContext extends UsageParserRuleContext {
        public OptionalContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public OptionalSubExpressionContext optionalSubExpression() {
            return getRuleContext(OptionalSubExpressionContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_optional;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof UsageVisitor) return ((UsageVisitor<? extends T>) visitor).visitOptional(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class AlternativesContext extends UsageParserRuleContext {
        public AlternativesContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<AlternativesSubExpressionContext> alternativesSubExpression() {
            return getRuleContexts(AlternativesSubExpressionContext.class);
        }

        public AlternativesSubExpressionContext alternativesSubExpression(int i) {
            return getRuleContext(AlternativesSubExpressionContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_alternatives;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof UsageVisitor) return ((UsageVisitor<? extends T>) visitor).visitAlternatives(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class PlaceholderContext extends UsageParserRuleContext {
        public PlaceholderContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode PLACEHOLDER() {
            return getToken(UsageParser.PLACEHOLDER, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_placeholder;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof UsageVisitor) return ((UsageVisitor<? extends T>) visitor).visitPlaceholder(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class PlaceholderWithWhitespaceContext extends UsageParserRuleContext {
        public PlaceholderWithWhitespaceContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode PLACEHOLDER_WITH_WHITESPACE() {
            return getToken(UsageParser.PLACEHOLDER_WITH_WHITESPACE, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_placeholderWithWhitespace;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof UsageVisitor)
                return ((UsageVisitor<? extends T>) visitor).visitPlaceholderWithWhitespace(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class LiteralContext extends UsageParserRuleContext {
        public LiteralContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode LITERAL() {
            return getToken(UsageParser.LITERAL, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_literal;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof UsageVisitor) return ((UsageVisitor<? extends T>) visitor).visitLiteral(this);
            else return visitor.visitChildren(this);
        }
    }
}