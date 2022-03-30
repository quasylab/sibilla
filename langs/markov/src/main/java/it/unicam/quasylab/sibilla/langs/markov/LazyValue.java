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

package it.unicam.quasylab.sibilla.langs.markov;

import it.unicam.quasylab.sibilla.core.models.util.MappingState;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public interface LazyValue<T> {


    static <T> LazyValue<T> evalRelation(LazyValue<T> v1, String op, LazyValue<T> v2) {
        ToDoubleFunction<T> f1 = v1.getToDoubleFunction();
        ToDoubleFunction<T> f2 = v2.getToDoubleFunction();
        BiFunction<Double, Double, Boolean> fOp = Value.getRelationOperator(op);
        return new LazyBoolean<>(arg -> fOp.apply(f1.applyAsDouble(arg),f2.applyAsDouble(arg)));
    }

    static LazyValue<MappingState> apply(LazyValue<MappingState> v1, String op, LazyValue<MappingState> v2) {
        switch (op) {
            case "+": return v1.add(v2);
            case "-": return v1.subtract(v2);
            case "*": return v1.multiply(v2);
            case "/": return v1.divide(v2);
            case "%": return v1.modulo(v2);
            default:
                return null;
        }
    }

    static LazyValue<MappingState> ifThenElse(LazyValue<MappingState> guard, LazyValue<MappingState> thenBranch, LazyValue<MappingState> elseBranch) {
        if (guard.getValueType() == DataType.BOOLEAN) {
            DataType thenType = thenBranch.getValueType();
            DataType elseType = elseBranch.getValueType();
            if ((thenType == DataType.INTEGER)&&(elseType == DataType.INTEGER)) {
                return integerIfThenElse(guard.getToBooleanFunction(), thenBranch.getToIntegerFunction(), elseBranch.getToIntegerFunction());
            } else {
                return doubleIfThenElse(guard.getToBooleanFunction(), thenBranch.getToDoubleFunction(), elseBranch.getToDoubleFunction());
            }
        }
        return null;
    }

    static LazyValue<MappingState> integerIfThenElse(Predicate<MappingState> guardFunction, ToIntFunction<MappingState> thenFunction, ToIntFunction<MappingState> elseFunction) {
        return new LazyInteger<>(arg -> (guardFunction.test(arg)?thenFunction.applyAsInt(arg):elseFunction.applyAsInt(arg)));
    }

    static LazyValue<MappingState> doubleIfThenElse(Predicate<MappingState> guardFunction, ToDoubleFunction<MappingState> thenFunction, ToDoubleFunction<MappingState> elseFunction) {
        return new LazyReal<>(arg -> (guardFunction.test(arg)?thenFunction.applyAsDouble(arg):elseFunction.applyAsDouble(arg)));
    }

    DataType getValueType();

    double evalToDouble(T arg);

    int evalToInt(T arg);

    boolean evalToBoolean(T arg);

    ToDoubleFunction<T> getToDoubleFunction();

    ToIntFunction<T> getToIntegerFunction();

    Predicate<T> getToBooleanFunction();

    LazyValue<T> add(LazyValue<T> other);

    LazyValue<T> subtract(LazyValue<T> other);

    LazyValue<T> multiply(LazyValue<T> other);

    LazyValue<T> divide(LazyValue<T> other);

    LazyValue<T> modulo(LazyValue<T> other);

    LazyValue<T> pow(LazyValue<T> other);

    LazyValue<T> minus();

    LazyValue<T> plus();

    LazyValue<T> and(LazyValue<T> other);

    LazyValue<T> or(LazyValue<T> other);

    LazyValue<T> not();

    LazyValue<T> cast(DataType type);

    class LazyInteger<T> implements LazyValue<T> {

        private final DataType type;
        private final ToIntFunction<T> function;

        public LazyInteger(ToIntFunction<T> function) {
            this.type = DataType.INTEGER;
            this.function = function;
        }

        @Override
        public DataType getValueType() {
            return type;
        }

        @Override
        public double evalToDouble(T arg) {
            return function.applyAsInt(arg);
        }

        @Override
        public int evalToInt(T arg) {
            return function.applyAsInt(arg);
        }

        @Override
        public boolean evalToBoolean(T arg) {
            return function.applyAsInt(arg)>0;
        }

        @Override
        public ToDoubleFunction<T> getToDoubleFunction() {
            return (arg -> (double) function.applyAsInt(arg));
        }

        @Override
        public ToIntFunction<T> getToIntegerFunction() {
            return function;
        }

        @Override
        public Predicate<T> getToBooleanFunction() {
            return arg -> (function.applyAsInt(arg)>0);
        }

        @Override
        public LazyValue<T> add(LazyValue<T> other) {
            switch (other.getValueType()) {
                case INTEGER:
                    ToIntFunction<T> otherFunction = other.getToIntegerFunction();
                    return new LazyInteger<>(args -> function.applyAsInt(args)+otherFunction.applyAsInt(args));
                case REAL:
                    ToDoubleFunction<T> doubleFunction = getToDoubleFunction();
                    ToDoubleFunction<T> otherDoubleFunction = other.getToDoubleFunction();
                    return new LazyReal<>(args -> doubleFunction.applyAsDouble(args)+otherDoubleFunction.applyAsDouble(args));
            }
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> subtract(LazyValue<T> other) {
            switch (other.getValueType()) {
                case INTEGER:
                    ToIntFunction<T> otherFunction = other.getToIntegerFunction();
                    return new LazyInteger<>(args -> function.applyAsInt(args)-otherFunction.applyAsInt(args));
                case REAL:
                    ToDoubleFunction<T> doubleFunction = getToDoubleFunction();
                    ToDoubleFunction<T> otherDoubleFunction = other.getToDoubleFunction();
                    return new LazyReal<>(args -> doubleFunction.applyAsDouble(args)-otherDoubleFunction.applyAsDouble(args));
            }
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> multiply(LazyValue<T> other) {
            switch (other.getValueType()) {
                case INTEGER:
                    ToIntFunction<T> otherFunction = other.getToIntegerFunction();
                    return new LazyInteger<>(args -> function.applyAsInt(args)*otherFunction.applyAsInt(args));
                case REAL:
                    ToDoubleFunction<T> doubleFunction = getToDoubleFunction();
                    ToDoubleFunction<T> otherDoubleFunction = other.getToDoubleFunction();
                    return new LazyReal<>(args -> doubleFunction.applyAsDouble(args)*otherDoubleFunction.applyAsDouble(args));
            }
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> divide(LazyValue<T> other) {
            switch (other.getValueType()) {
                case INTEGER:
                    ToIntFunction<T> otherFunction = other.getToIntegerFunction();
                    return new LazyInteger<>(args -> function.applyAsInt(args)/otherFunction.applyAsInt(args));
                case REAL:
                    ToDoubleFunction<T> doubleFunction = getToDoubleFunction();
                    ToDoubleFunction<T> otherDoubleFunction = other.getToDoubleFunction();
                    return new LazyReal<>(args -> doubleFunction.applyAsDouble(args)/otherDoubleFunction.applyAsDouble(args));
            }
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> modulo(LazyValue<T> other) {
            switch (other.getValueType()) {
                case INTEGER:
                    ToIntFunction<T> otherFunction = other.getToIntegerFunction();
                    return new LazyInteger<>(args -> function.applyAsInt(args)%otherFunction.applyAsInt(args));
                case REAL:
                    ToDoubleFunction<T> doubleFunction = getToDoubleFunction();
                    ToDoubleFunction<T> otherDoubleFunction = other.getToDoubleFunction();
                    return new LazyReal<>(args -> doubleFunction.applyAsDouble(args)%otherDoubleFunction.applyAsDouble(args));
            }
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> pow(LazyValue<T> other) {
            switch (other.getValueType()) {
                case INTEGER:
                    ToIntFunction<T> otherFunction = other.getToIntegerFunction();
                    return new LazyInteger<>(args -> (int) Math.pow(function.applyAsInt(args),otherFunction.applyAsInt(args)));
                case REAL:
                    ToDoubleFunction<T> doubleFunction = getToDoubleFunction();
                    ToDoubleFunction<T> otherDoubleFunction = other.getToDoubleFunction();
                    return new LazyReal<>(args -> Math.pow(doubleFunction.applyAsDouble(args),otherDoubleFunction.applyAsDouble(args)));
            }
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> minus() {
            return new LazyInteger<>(args -> -function.applyAsInt(args));
        }

        @Override
        public LazyValue<T> plus() {
            return this;
        }

        @Override
        public LazyValue<T> and(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> or(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> not() {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> cast(DataType type) {
            switch (type) {
                case INTEGER: return this;
                case REAL: return new LazyReal<T>(getToDoubleFunction());
                case BOOLEAN: return new LazyBoolean<>(args -> (function.applyAsInt(args)>0));
            }
            return new NoneLazyValue<>();
        }

    }

    class LazyReal<T> implements LazyValue<T> {

        private final ToDoubleFunction<T> function;
        private final DataType type;

        public LazyReal(ToDoubleFunction<T> function) {
            this.function = function;
            this.type = DataType.REAL;
        }

        @Override
        public DataType getValueType() {
            return type;
        }

        @Override
        public double evalToDouble(T arg) {
            return function.applyAsDouble(arg);
        }

        @Override
        public int evalToInt(T arg) {
            return (int) function.applyAsDouble(arg);
        }

        @Override
        public boolean evalToBoolean(T arg) {
            return function.applyAsDouble(arg)>0;
        }

        @Override
        public ToDoubleFunction<T> getToDoubleFunction() {
            return this.function;
        }

        @Override
        public ToIntFunction<T> getToIntegerFunction() {
            return arg -> (int) function.applyAsDouble(arg);
        }

        @Override
        public Predicate<T> getToBooleanFunction() {
            return arg -> (function.applyAsDouble(arg)>0);
        }

        @Override
        public LazyValue<T> add(LazyValue<T> other) {
            if ((other.getValueType()==DataType.INTEGER)||(other.getValueType()==DataType.REAL)) {
                ToDoubleFunction<T> otherFunction = other.getToDoubleFunction();
                return new LazyReal<>(arg -> function.applyAsDouble(arg)+otherFunction.applyAsDouble(arg));
            } else {
                return new NoneLazyValue<>();
            }
        }

        @Override
        public LazyValue<T> subtract(LazyValue<T> other) {
            if ((other.getValueType()==DataType.INTEGER)||(other.getValueType()==DataType.REAL)) {
                ToDoubleFunction<T> otherFunction = other.getToDoubleFunction();
                return new LazyReal<>(arg -> function.applyAsDouble(arg)-otherFunction.applyAsDouble(arg));
            } else {
                return new NoneLazyValue<>();
            }
        }

        @Override
        public LazyValue<T> multiply(LazyValue<T> other) {
            if ((other.getValueType()==DataType.INTEGER)||(other.getValueType()==DataType.REAL)) {
                ToDoubleFunction<T> otherFunction = other.getToDoubleFunction();
                return new LazyReal<>(arg -> function.applyAsDouble(arg)*otherFunction.applyAsDouble(arg));
            } else {
                return new NoneLazyValue<>();
            }
        }

        @Override
        public LazyValue<T> divide(LazyValue<T> other) {
            if ((other.getValueType()==DataType.INTEGER)||(other.getValueType()==DataType.REAL)) {
                ToDoubleFunction<T> otherFunction = other.getToDoubleFunction();
                return new LazyReal<>(arg -> function.applyAsDouble(arg)/otherFunction.applyAsDouble(arg));
            } else {
                return new NoneLazyValue<>();
            }
        }

        @Override
        public LazyValue<T> modulo(LazyValue<T> other) {
            if ((other.getValueType()==DataType.INTEGER)||(other.getValueType()==DataType.REAL)) {
                ToDoubleFunction<T> otherFunction = other.getToDoubleFunction();
                return new LazyReal<>(arg -> function.applyAsDouble(arg)%otherFunction.applyAsDouble(arg));
            } else {
                return new NoneLazyValue<>();
            }
        }

        @Override
        public LazyValue<T> pow(LazyValue<T> other) {
            if ((other.getValueType()==DataType.INTEGER)||(other.getValueType()==DataType.REAL)) {
                ToDoubleFunction<T> otherFunction = other.getToDoubleFunction();
                return new LazyReal<>(arg -> Math.pow(function.applyAsDouble(arg),otherFunction.applyAsDouble(arg)));
            } else {
                return new NoneLazyValue<>();
            }
        }

        @Override
        public LazyValue<T> minus() {
            return new LazyReal<>(arg -> -function.applyAsDouble(arg));
        }

        @Override
        public LazyValue<T> plus() {
            return this;
        }

        @Override
        public LazyValue<T> and(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> or(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> not() {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> cast(DataType type) {
            switch (type) {
                case REAL: return this;
                case INTEGER: return new LazyInteger<>(this.getToIntegerFunction());
                case BOOLEAN: return new LazyBoolean<>(this.getToBooleanFunction());
            }
            return new NoneLazyValue<>();
        }
    }

    class LazyBoolean<T> implements LazyValue<T> {

        private final Predicate<T> function;
        private final DataType type;

        public LazyBoolean(Predicate<T> function) {
            this.function = function;
            this.type = DataType.BOOLEAN;
        }

        @Override
        public DataType getValueType() {
            return type;
        }

        @Override
        public double evalToDouble(T arg) {
            return (function.test(arg)?1.0:0.0);
        }

        @Override
        public int evalToInt(T arg) {
            return (function.test(arg)?1:0);
        }

        @Override
        public boolean evalToBoolean(T arg) {
            return function.test(arg);
        }

        @Override
        public ToDoubleFunction<T> getToDoubleFunction() {
            return this::evalToDouble;
        }

        @Override
        public ToIntFunction<T> getToIntegerFunction() {
            return this::evalToInt;
        }

        @Override
        public Predicate<T> getToBooleanFunction() {
            return function;
        }

        @Override
        public LazyValue<T> add(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> subtract(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> multiply(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> divide(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> modulo(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> pow(LazyValue<T> other) {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> minus() {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> plus() {
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> and(LazyValue<T> other) {
            if (other.getValueType()==DataType.BOOLEAN) {
                return new LazyBoolean<>(function.and(other.getToBooleanFunction()));
            }
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> or(LazyValue<T> other) {
            if (other.getValueType()==DataType.BOOLEAN) {
                return new LazyBoolean<>(function.or(other.getToBooleanFunction()));
            }
            return new NoneLazyValue<>();
        }

        @Override
        public LazyValue<T> not() {
            return new LazyBoolean<>(function.negate());
        }

        @Override
        public LazyValue<T> cast(DataType type) {
            switch (type) {
                case BOOLEAN: return this;
                case INTEGER: return new LazyInteger<>(getToIntegerFunction());
                case REAL: return new LazyReal<>(getToDoubleFunction());
                default: return new NoneLazyValue<>();
            }
        }
    }

    class NoneLazyValue<T> implements LazyValue<T> {

        @Override
        public DataType getValueType() {
            return DataType.NONE;
        }

        @Override
        public double evalToDouble(T arg) {
            return Double.NaN;
        }

        @Override
        public int evalToInt(T arg) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean evalToBoolean(T arg) {
            return false;
        }

        @Override
        public ToDoubleFunction<T> getToDoubleFunction() {
            return s -> Double.NaN;
        }

        @Override
        public ToIntFunction<T> getToIntegerFunction() {
            return s -> Integer.MAX_VALUE;
        }

        @Override
        public Predicate<T> getToBooleanFunction() {
            return s -> false;
        }

        @Override
        public LazyValue<T> add(LazyValue<T> other) {
            return this;
        }

        @Override
        public LazyValue<T> subtract(LazyValue<T> other) {
            return this;
        }

        @Override
        public LazyValue<T> multiply(LazyValue<T> other) {
            return this;
        }

        @Override
        public LazyValue<T> divide(LazyValue<T> other) {
            return this;
        }

        @Override
        public LazyValue<T> modulo(LazyValue<T> other) {
            return this;
        }

        @Override
        public LazyValue<T> pow(LazyValue<T> other) {
            return this;
        }

        @Override
        public LazyValue<T> minus() {
            return this;
        }

        @Override
        public LazyValue<T> plus() {
            return this;
        }

        @Override
        public LazyValue<T> and(LazyValue<T> other) {
            return this;
        }

        @Override
        public LazyValue<T> or(LazyValue<T> other) {
            return this;
        }

        @Override
        public LazyValue<T> not() {
            return this;
        }

        @Override
        public LazyValue<T> cast(DataType type) {
            return this;
        }
    }
}
