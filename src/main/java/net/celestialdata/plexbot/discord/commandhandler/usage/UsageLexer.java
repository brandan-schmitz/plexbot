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
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class UsageLexer extends Lexer {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, PLACEHOLDER_WITH_WHITESPACE = 6,
            PLACEHOLDER = 7, LITERAL = 8, WS = 9;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\13A\b\1\4\2\t\2\4" +
                    "\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\3\2" +
                    "\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\6\7\"\n\7\r\7\16\7#\3\7\3\7\3" +
                    "\7\3\7\3\7\3\b\3\b\6\b-\n\b\r\b\16\b.\3\b\3\b\3\t\3\t\6\t\65\n\t\r\t\16" +
                    "\t\66\3\t\3\t\3\n\6\n<\n\n\r\n\16\n=\3\n\3\n\2\2\13\3\3\5\4\7\5\t\6\13" +
                    "\7\r\b\17\t\21\n\23\13\3\2\6\3\2@@\3\2))\6\2\13\f\16\17\"\"))\5\2\13\f" +
                    "\16\17\"\"\2D\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2" +
                    "\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\3\25\3\2\2\2\5" +
                    "\27\3\2\2\2\7\31\3\2\2\2\t\33\3\2\2\2\13\35\3\2\2\2\r\37\3\2\2\2\17*\3" +
                    "\2\2\2\21\62\3\2\2\2\23;\3\2\2\2\25\26\7]\2\2\26\4\3\2\2\2\27\30\7_\2" +
                    "\2\30\6\3\2\2\2\31\32\7*\2\2\32\b\3\2\2\2\33\34\7~\2\2\34\n\3\2\2\2\35" +
                    "\36\7+\2\2\36\f\3\2\2\2\37!\7>\2\2 \"\n\2\2\2! \3\2\2\2\"#\3\2\2\2#!\3" +
                    "\2\2\2#$\3\2\2\2$%\3\2\2\2%&\7\60\2\2&\'\7\60\2\2\'(\7\60\2\2()\7@\2\2" +
                    ")\16\3\2\2\2*,\7>\2\2+-\n\2\2\2,+\3\2\2\2-.\3\2\2\2.,\3\2\2\2./\3\2\2" +
                    "\2/\60\3\2\2\2\60\61\7@\2\2\61\20\3\2\2\2\62\64\t\3\2\2\63\65\n\4\2\2" +
                    "\64\63\3\2\2\2\65\66\3\2\2\2\66\64\3\2\2\2\66\67\3\2\2\2\678\3\2\2\28" +
                    "9\t\3\2\29\22\3\2\2\2:<\t\5\2\2;:\3\2\2\2<=\3\2\2\2=;\3\2\2\2=>\3\2\2" +
                    "\2>?\3\2\2\2?@\b\n\2\2@\24\3\2\2\2\7\2#.\66=\3\b\2\2";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = makeLiteralNames();
    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);
    public static String[] channelNames = {
            "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };
    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

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

    public UsageLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    private static String[] makeRuleNames() {
        return new String[]{
                "T__0", "T__1", "T__2", "T__3", "T__4", "PLACEHOLDER_WITH_WHITESPACE",
                "PLACEHOLDER", "LITERAL", "WS"
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
    public String[] getChannelNames() {
        return channelNames;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }
}