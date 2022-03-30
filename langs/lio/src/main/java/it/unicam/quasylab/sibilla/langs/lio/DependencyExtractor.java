/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.langs.lio;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class DependencyExtractor extends LIOModelBaseVisitor<Set<String>> {

    @Override
    public Set<String> visitNegationExpression(LIOModelParser.NegationExpressionContext ctx) {
        return super.visitNegationExpression(ctx);
    }

    @Override
    public Set<String> visitExponentExpression(LIOModelParser.ExponentExpressionContext ctx) {
        return collect(ctx.left,ctx.right);
    }

    private Set<String> collect(ParserRuleContext ... args) {
        HashSet<String> set = new HashSet<>();
        Stream.of(args).sequential().forEach(ctx -> set.addAll( ctx.accept(this)));
        return set;
    }

    @Override
    public Set<String> visitReferenceExpression(LIOModelParser.ReferenceExpressionContext ctx) {
        return Set.of(ctx.reference.getText());
    }

    @Override
    public Set<String> visitRelationExpression(LIOModelParser.RelationExpressionContext ctx) {
        return collect(ctx.left,ctx.right);
    }

    @Override
    public Set<String> visitOrExpression(LIOModelParser.OrExpressionContext ctx) {
        return collect(ctx.left,ctx.right);
    }

    @Override
    public Set<String> visitIfThenElseExpression(LIOModelParser.IfThenElseExpressionContext ctx) {
        return collect(ctx.guard, ctx.thenBranch, ctx.elseBranch);
    }

    @Override
    public Set<String> visitAndExpression(LIOModelParser.AndExpressionContext ctx) {
        return collect(ctx.left,ctx.right);
    }

    @Override
    public Set<String> visitMulDivExpression(LIOModelParser.MulDivExpressionContext ctx) {
        return collect(ctx.left,ctx.right);
    }

    @Override
    public Set<String> visitAddSubExpression(LIOModelParser.AddSubExpressionContext ctx) {
        return collect(ctx.left,ctx.right);
    }

    @Override
    public Set<String> visitUnaryExpression(LIOModelParser.UnaryExpressionContext ctx) {
        return ctx.arg.accept(this);
    }

    @Override
    protected Set<String> defaultResult() {
        return new HashSet<>();
    }
}
