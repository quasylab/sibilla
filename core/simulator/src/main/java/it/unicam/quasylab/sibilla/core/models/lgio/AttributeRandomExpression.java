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

package it.unicam.quasylab.sibilla.core.models.lgio;

import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;

@FunctionalInterface
public interface AttributeRandomExpression {

    double eval(RandomGenerator rg, AgentAttributes attributes);

    default AttributeRandomExpression apply(DoubleBinaryOperator op, double value) {
        return (rg, attr) -> op.applyAsDouble(this.eval(rg, attr), value);
    }

    default AttributeRandomExpression apply(DoubleBinaryOperator op, AttributeRandomExpression other) {
        return (rg, attr) -> op.applyAsDouble(this.eval(rg, attr), other.eval(rg, attr));
    }

    default AttributeRandomExpression apply(DoubleUnaryOperator op) {
        return (rg, attr) -> op.applyAsDouble( this.eval(rg, attr) );
    }

    static AttributeRandomExpression ifThenElse(Predicate<AgentAttributes> guard,AttributeRandomExpression thenExpr, AttributeRandomExpression elseExpr) {
        return (rg, attr) -> (guard.test(attr)?thenExpr.eval(rg, attr):elseExpr.eval(rg, attr));
    }

    static AttributeRandomExpression ifThenElse(Predicate<AgentAttributes> guard,double thenValue, double elseValue) {
        return (rg, attr) -> (guard.test(attr)?thenValue:elseValue);
    }


    static AttributeRandomExpression scalar(double value) {
        return (rg, attr) -> value;
    }

}
