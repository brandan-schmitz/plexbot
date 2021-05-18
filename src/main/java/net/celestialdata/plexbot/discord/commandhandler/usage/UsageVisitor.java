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

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link UsageParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public interface UsageVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by {@link UsageParser#usage}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUsage(UsageParser.UsageContext ctx);

    /**
     * Visit a parse tree produced by {@link UsageParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitExpression(UsageParser.ExpressionContext ctx);

    /**
     * Visit a parse tree produced by {@link UsageParser#optionalSubExpression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOptionalSubExpression(UsageParser.OptionalSubExpressionContext ctx);

    /**
     * Visit a parse tree produced by {@link UsageParser#alternativesSubExpression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlternativesSubExpression(UsageParser.AlternativesSubExpressionContext ctx);

    /**
     * Visit a parse tree produced by {@link UsageParser#optional}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOptional(UsageParser.OptionalContext ctx);

    /**
     * Visit a parse tree produced by {@link UsageParser#alternatives}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlternatives(UsageParser.AlternativesContext ctx);

    /**
     * Visit a parse tree produced by {@link UsageParser#placeholder}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPlaceholder(UsageParser.PlaceholderContext ctx);

    /**
     * Visit a parse tree produced by {@link UsageParser#placeholderWithWhitespace}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPlaceholderWithWhitespace(UsageParser.PlaceholderWithWhitespaceContext ctx);

    /**
     * Visit a parse tree produced by {@link UsageParser#literal}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitLiteral(UsageParser.LiteralContext ctx);
}