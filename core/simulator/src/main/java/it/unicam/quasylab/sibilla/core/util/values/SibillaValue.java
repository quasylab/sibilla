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

package it.unicam.quasylab.sibilla.core.util.values;

import it.unicam.quasylab.sibilla.core.models.slam.data.AgentStore;
import it.unicam.quasylab.sibilla.core.models.slam.data.AgentVariable;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Optional;
import java.util.function.*;
import java.util.stream.DoubleStream;

/**
 * Instances of this interface represent values in a Sibilla model.
 */
public interface SibillaValue {

    SibillaValue ERROR_VALUE = SibillaErrorValue.INSTANCE;

    static SibillaValue and(SibillaValue v1, SibillaValue v2) {
        if ((v1 instanceof SibillaBoolean)&&(v2 instanceof SibillaBoolean)) {
            return ((SibillaBoolean) v1).and((SibillaBoolean) v2);
        } else {
            return SibillaValue.ERROR_VALUE;
        }
    }

    static SibillaValue or(SibillaValue v1, SibillaValue v2) {
        if ((v1 instanceof SibillaBoolean)&&(v2 instanceof SibillaBoolean)) {
            return ((SibillaBoolean) v1).or((SibillaBoolean) v2);
        } else {
            return SibillaValue.ERROR_VALUE;
        }
    }

    static SibillaValue not(SibillaValue v1) {
        if (v1 instanceof SibillaBoolean) {
            return ((SibillaBoolean) v1).not();
        } else {
            return SibillaValue.ERROR_VALUE;
        }
    }

    static SibillaValue sum(SibillaValue v1, SibillaValue v2) {
        if (((v1 instanceof SibillaDouble)&&((v2 instanceof SibillaDouble)||(v2 instanceof SibillaInteger)))||
                ((v2 instanceof SibillaDouble)&&(v1 instanceof SibillaInteger))) {
            return new SibillaDouble(v1.doubleOf()+ v2.doubleOf());
        }
        if ((v1 instanceof SibillaInteger)&&(v2 instanceof SibillaInteger)) {
            return new SibillaInteger(v1.intOf()+v2.intOf());
        }
        return SibillaValue.ERROR_VALUE;
    }

    static SibillaValue sub(SibillaValue v1, SibillaValue v2) {
        if (((v1 instanceof SibillaDouble)&&((v2 instanceof SibillaDouble)||(v2 instanceof SibillaInteger)))||
                ((v2 instanceof SibillaDouble)&&(v1 instanceof SibillaInteger))) {
            return new SibillaDouble(v1.doubleOf()- v2.doubleOf());
        }
        if ((v1 instanceof SibillaInteger)&&(v2 instanceof SibillaInteger)) {
            return new SibillaInteger(v1.intOf()-v2.intOf());
        }
        return SibillaValue.ERROR_VALUE;
    }

    static SibillaValue mod(SibillaValue v1, SibillaValue v2) {
        if (((v1 instanceof SibillaDouble)&&((v2 instanceof SibillaDouble)||(v2 instanceof SibillaInteger)))||
                ((v2 instanceof SibillaDouble)&&(v1 instanceof SibillaInteger))) {
            return new SibillaDouble(v1.doubleOf() % v2.doubleOf());
        }
        if ((v1 instanceof SibillaInteger)&&(v2 instanceof SibillaInteger)) {
            return new SibillaInteger(v1.intOf() % v2.intOf());
        }
        return SibillaValue.ERROR_VALUE;
    }

    static SibillaValue mul(SibillaValue v1, SibillaValue v2) {
        if (((v1 instanceof SibillaDouble)&&((v2 instanceof SibillaDouble)||(v2 instanceof SibillaInteger)))||
                ((v2 instanceof SibillaDouble)&&(v1 instanceof SibillaInteger))) {
            return new SibillaDouble(v1.doubleOf() * v2.doubleOf());
        }
        if ((v1 instanceof SibillaInteger)&&(v2 instanceof SibillaInteger)) {
            return new SibillaInteger(v1.intOf() * v2.intOf());
        }
        return SibillaValue.ERROR_VALUE;
    }

    static SibillaValue div(SibillaValue v1, SibillaValue v2) {
        if (((v1 instanceof SibillaDouble)&&((v2 instanceof SibillaDouble)||(v2 instanceof SibillaInteger)))||
                ((v2 instanceof SibillaDouble)&&(v1 instanceof SibillaInteger))) {
            return new SibillaDouble(v1.doubleOf() / v2.doubleOf());
        }
        if ((v1 instanceof SibillaInteger)&&(v2 instanceof SibillaInteger)) {
            return new SibillaInteger(v1.intOf() / v2.intOf());
        }
        return SibillaValue.ERROR_VALUE;
    }

    static SibillaValue zeroDiv(SibillaValue v1, SibillaValue v2) {
        if (((v1 instanceof SibillaDouble)&&((v2 instanceof SibillaDouble)||(v2 instanceof SibillaInteger)))||
                ((v2 instanceof SibillaDouble)&&(v1 instanceof SibillaInteger))) {
            if (v2.doubleOf() != 0.0) {
                return new SibillaDouble(v1.doubleOf() / v2.doubleOf());
            } else {
                return new SibillaDouble(0);
            }
        }
        if ((v1 instanceof SibillaInteger)&&(v2 instanceof SibillaInteger)) {
            if (v2.intOf() != 0) {
                return new SibillaInteger(v1.intOf() / v2.intOf());
            } else {
                return new SibillaInteger(0);
            }
        }
        return SibillaValue.ERROR_VALUE;
    }

    static SibillaValue minus(SibillaValue v) {
        if (v instanceof SibillaInteger) {
            return new SibillaInteger(-v.intOf());
        }
        if (v instanceof SibillaDouble) {
            return new SibillaDouble(-v.doubleOf());
        }
        return SibillaValue.ERROR_VALUE;
    }

    static SibillaValue abs(SibillaValue v){
        if (v instanceof SibillaInteger) {
            if (v.intOf() < 0) {
                return new SibillaInteger(-v.intOf());
            } else {
                return new SibillaInteger(v.intOf());
            }
        }
        if (v instanceof SibillaDouble) {
            if (v.doubleOf() < 0.0) {
                return new SibillaDouble(-v.doubleOf());
            } else {
                return new SibillaDouble(v.doubleOf());
            }
        }
        return SibillaValue.ERROR_VALUE;
    }

    static SibillaValue eval(DoubleBinaryOperator op, SibillaValue v1, SibillaValue v2) {
        return new SibillaDouble(op.applyAsDouble(v1.doubleOf(), v2.doubleOf()));
    }

    /**
     * Returns the sibilla value representing the double value <code>v</code>.
     *
     * @param v a double value.
     * @return the sibilla value representing the double value <code>v</code>.
     */
    static SibillaValue of(double v) {
        return new SibillaDouble(v);
    }

    /**
     * Returns the sibilla value representing the int value <code>v</code>.
     *
     * @param v a double value.
     * @return the sibilla value representing the int value <code>v</code>.
     */
    static SibillaValue of(int v) {
        return new SibillaInteger(v);
    }

    /**
     * Returns the sibilla value representing the boolean value <code>v</code>.
     *
     * @param v a double value.
     * @return the sibilla value representing the boolean value <code>v</code>.
     */
    static SibillaValue of(boolean v) {
        if (v) {
            return SibillaBoolean.TRUE;
        } else {
            return SibillaBoolean.FALSE;
        }
    }

    static SibillaValue access(SibillaValue value, String name) {
        if (value instanceof SibillaRecord) {
            return ((SibillaRecord) value).get(name);
        } else {
            return SibillaValue.ERROR_VALUE;
        }
    }

    static <T> Function<T,SibillaValue> access(Function<T, SibillaValue> f, String name) {
        return arg -> SibillaValue.access(f.apply(arg), name);
    }

    static SibillaValue[] of(double[] values) {
        return DoubleStream.of(values).mapToObj(SibillaValue::of).toArray(SibillaValue[]::new);
    }

    static SibillaValue min(SibillaValue v1, SibillaValue v2) {
        if (v1.doubleOf()<v2.doubleOf()) {
            return v1;
        } else {
            return v2;
        }
    }

    static SibillaValue max(SibillaValue v1, SibillaValue v2) {
        if (v1.doubleOf()<v2.doubleOf()) {
            return v2;
        } else {
            return v1;
        }
    }

    static <T,R> BiFunction<T, R, SibillaValue> apply(DoubleUnaryOperator op, BiFunction<T, R, SibillaValue> function) {
        return (t, r) -> SibillaValue.apply(op, function.apply(t, r));
    }

    static <R, T> BiFunction<R, T, SibillaValue> apply(UnaryOperator<SibillaValue> op, BiFunction<R, T, SibillaValue> function) {
        return (r, t) -> op.apply(function.apply(r, t));
    }


    /**
     * Returns the double representation of this value. {@link Double#NaN} is returned if this
     * value has not a double representation.
     *
     * @return the double representation of this value.
     */
    double doubleOf();

    /**
     * Returns the boolean representation of this value.
     *
     * @return the boolean representation of this value.
     */
    boolean booleanOf();

    /**
     * Returns the integer representation of this value.
     *
     * @return the integer representation of this value.
     */
    int intOf();


    /**
     * Returns the predicate associated with the given comparison operator. Valid symbols are:
     * <code>"<"</code>, <code>"<="</code>, <code>"=="</code>, <code>"!="</code>, <code>">"</code>, <code>">="</code>.
     *
     * @param op a comparison operator.
     * @return the predicate associated with the given comparison operator.
     */
    static BiPredicate<SibillaValue,SibillaValue> getRelationOperator(String op) {
        if (op.equals("<"))  { return (x,y) -> x.doubleOf()<y.doubleOf(); }
        if (op.equals("<="))  { return (x,y) -> x.doubleOf()<=y.doubleOf(); }
        if (op.equals("=="))  { return (x,y) -> x.doubleOf()==y.doubleOf(); }
        if (op.equals("!="))  { return (x,y) -> !x.equals(y); }
        if (op.equals(">"))  { return (x,y) -> x.doubleOf()>y.doubleOf(); }
        if (op.equals(">="))  { return (x,y) -> x.doubleOf()>=y.doubleOf(); }
        return (x,y) -> false;
    }


    /**
     * Returns the binary operator associated with the given symbol. Valid symbols are:
     * <ul>
     *     <li><code>"+"</code>: addition;</li>
     *     <li><code>"-"</code>: subtraction;</li>
     *     <li><code>"%"</code>: modulo;</li>
     *     <li><code>"*"</code>: multiplication;</li>
     *     <li><code>"/"</code>: division;</li>
     *     <li><code>"//"</code>: division if not 0 (returns a/b if b is different from 0, 0 otherwise).</li>
     * </ul>
     *
     * @param op an arithmetic operator.
     * @return the binary operator associated with the given symbol.
     */
    static BinaryOperator<SibillaValue> getOperator(String op) {
        if (op.equals("+")) {return SibillaValue::sum;}
        if (op.equals("-")) {return SibillaValue::sub; }
        if (op.equals("%")) {return SibillaValue::mod; }
        if (op.equals("*")) {return SibillaValue::mul; }
        if (op.equals("/")) {return SibillaValue::div; }
        if (op.equals("//")) {return SibillaValue::zeroDiv; }
        return (x,y) -> SibillaValue.ERROR_VALUE;
    }

    /**
     * Given a unary operator op, and a function f1: T -> SibillaValue, returns the
     * function that given a parameter t returns the result of the application of op to the evaluation
     * of f1(t).
     *
     * @param op a binary operator.
     * @param f1 a function from T to SibillaValue.
     * @return the function that given a parameter t returns the result of the application of op to the evaluation of
     * the given function.
     * @param <T> domain of the given functions.
     */
    static <T> Function<T,SibillaValue> apply(UnaryOperator<SibillaValue> op, Function<T,SibillaValue> f1) {
        return arg -> op.apply(f1.apply(arg));
    }


    /**
     * Given a binary operator op, and two function f1, f2: T -> SibillaValue, returns the
     * function that given a parameter t returns the result of the application of op to the evaluation
     * of f1(t) and f2(t).
     *
     * @param op a binary operator.
     * @param f1 a function from T to SibillaValue.
     * @param f2 a function from T to SibillaValue.
     * @return the function that given a parameter t returns the result of the application of op to the evaluation
     * of the given functions.
     * @param <T> domain of the given functions.
     */
    static <T> Function<T,SibillaValue> apply(BinaryOperator<SibillaValue> op, Function<T,SibillaValue> f1, Function<T, SibillaValue> f2) {
        return arg -> op.apply(f1.apply(arg), f2.apply(arg));
    }

    static SibillaValue apply(BinaryOperator<SibillaValue> op, SibillaValue v1, SibillaValue v2) {
        return op.apply(v1, v2);
    }

    /**
     * Given a binary operator op, and two function f1, f2: T*U -> SibillaValue, returns the
     * function that given a parameters t and u returns the result of the application of op to the evaluation
     * of f1(t, u) and f2(t, u).
     *
     * @param op a binary operator.
     * @param f1 a function from T*U to SibillaValue.
     * @param f2 a function from T*T to SibillaValue.
     * @return the function that given a parameters t and u returns the result of the application of op to
     *  the evaluation of f1(t, u) and f2(t, u).
     * @param <T> domain of the given functions.
     * @param <U> domain of the given functions.
     */
    static <T, U> BiFunction<T,U,SibillaValue> apply(BinaryOperator<SibillaValue> op, BiFunction<T, U, SibillaValue> f1, BiFunction<T, U, SibillaValue> f2) {
        return (t,u) -> op.apply(f1.apply(t, u), f2.apply(t, u));
    }

    /**
     * Returns the application of given unary operator on doubles to the given value.
     * @param op a unary double opeator.
     * @param v a sibilla value.
     * @return the application of given unary operator on doubles to the given value.
     */
    static SibillaValue apply(DoubleUnaryOperator op, SibillaValue v) {
        return SibillaValue.of(op.applyAsDouble(v.doubleOf()));
    }

    static SibillaValue apply(DoubleBinaryOperator op, SibillaValue v1, SibillaValue v2) {
        return SibillaValue.of(op.applyAsDouble(v1.doubleOf(), v2.doubleOf()));
    }


    /**
     * Returns the function that applies the given unary double operator to the evaluation
     * of the given function.
     * @param op a unary double operator
     * @param f a function from T to SibillaValue
     * @return the function that applies the given unary operator to the evaluation of the given function
     * @param <T> domain of the given function
     */
    static <T> Function<T, SibillaValue> apply(DoubleUnaryOperator op, Function<T,SibillaValue> f) {
        return arg -> SibillaValue.of(op.applyAsDouble(f.apply(arg).doubleOf()));
    }


    /**
     * Given a binary double operator op, and two function f1, f2: T -> SibillaValue, returns the
     * function that given a parameter t returns the result of the application of op to the evaluation
     * of f1(t) and f2(t) as doubles.
     *
     * @param op a binary operator.
     * @param f1 a function from T to SibillaValue.
     * @param f2 a function from T to SibillaValue.
     * @return the function that given a parameter t returns the result of the application of op to the evaluation
     * of the given functions.
     * @param <T> domain of the given functions.
     */
    static <T> Function<T, SibillaValue> apply(DoubleBinaryOperator op, Function<T,SibillaValue> f1, Function<T,SibillaValue> f2) {
        return arg -> SibillaValue.of(op.applyAsDouble(f1.apply(arg).doubleOf(), f2.apply(arg).doubleOf()));
    }


    /**
     * Returns the function that applies the given binary double operator to the evaluation of the given functions.
     *
     * @param op a binary double operator
     * @param f1 a funciton from T*U to SibillaValue
     * @param f2 a function from T*U to SibillaValue
     * @return function from T*U to SibillaValue that applies the given binary double operator to the
     * evaluation of the given functions.
     * @param <T> type of the first argument of the given functions
     * @param <U> type of the second argument of the given functions
     */
    static <T, U> BiFunction<T, U, SibillaValue> apply(DoubleBinaryOperator op, BiFunction<T, U, SibillaValue> f1, BiFunction<T, U, SibillaValue> f2) {
        return (t, u) -> SibillaValue.of(op.applyAsDouble(f1.apply(t, u).doubleOf(), f2.apply(t, u).doubleOf()));
    }

}
