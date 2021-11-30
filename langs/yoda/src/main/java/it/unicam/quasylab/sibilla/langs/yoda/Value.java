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

package it.unicam.quasylab.sibilla.langs.yoda;

	import java.util.function.BiFunction;

	public interface Value {

		DataType getType();

		default double getDoubleValue() {
			return Double.NaN;
		}

		default int getIntValue() {
			return Integer.MAX_VALUE;
		}

		default boolean getBooleanValue() {
			return false;
		}

		default Value add(Value value) {
			return NONE;
		}

		default Value subtract(Value value) {
			return NONE;
		}

		default Value multiply(Value value) {
			return NONE;
		}

		default Value divide(Value value) {
			return NONE;
		}

		default Value modulo(Value value) {
			return NONE;
		}

		default Value pow(Value value) {
			return NONE;
		}

		default Value minus() {
			return NONE;
		}

		default Value plus() {
			return NONE;
		}

		default Value and(Value value) {
			return NONE;
		}

		default Value or(Value value) {
			return NONE;
		}

		default Value not() {
			return NONE;
		}

		default Value cast(DataType type){
			if (this.getType()==type){
				return this;
			}
			switch (type){
				case INTEGER: return new IntegerValue(this.getIntValue());
				case REAL: return new RealValue(this.getDoubleValue());
				case BOOLEAN: return new BooleanValue(this.getBooleanValue());
			}
			return null;
		}
		static Value getValue(DataType v1, double v2){
			switch (v1){
				case BOOLEAN: return new BooleanValue(v2>0);
				case INTEGER: return new IntegerValue((int) v2);
				case REAL: return new RealValue(v2);
				default: return NONE;
			}
		}
		static Value evalRelation(Value v1, String op, Value v2){
			return new BooleanValue(getRelationOperator(op).apply(v1.getDoubleValue(), v2.getDoubleValue()));
		}
		static BiFunction<Double,Double,Boolean> getRelationOperator(String op){
			switch (op){
				case "<": return (x,y) -> x<y;
				case "<=": return (x,y) -> x<=y;
				case "==": return Double::equals;
				case "!=": return (x,y) -> !x.equals(y);
				case ">=": return (x,y) -> x>=y;
				case ">": return (x,y) -> x>y;
				default: return (x,y) -> false;
			}
		}
		static Value applyOperation(Value v1, String op,Value v2){
			switch (op){
				case "+": return v1.add(v2);
				case "-": return v1.subtract(v2);
				case "/": return v1.divide(v2);
				case "*": return v1.multiply(v2);
				case "%": return v1.modulo(v2);
				default: return NONE;
			}
		}
		static Value applySign(String op, Value value){
			switch (op) {
				case "-": return value.minus();
				case "+": return value.plus();
				default: return NONE;
			}
		}

		Value NONE = new Value() {
			@Override
			public DataType getType() {
				return DataType.NONE;
			}

			@Override
			public Value add(Value value) {
				return this;
			}

			@Override
			public Value subtract(Value value) {
				return this;
			}

			@Override
			public Value multiply(Value value) {
				return this;
			}

			@Override
			public Value divide(Value value) {
				return this;
			}

			@Override
			public Value modulo(Value value) {
				return this;
			}

			@Override
			public Value pow(Value value) {
				return this;
			}

			@Override
			public Value minus() {
				return this;
			}

			@Override
			public Value plus() {
				return this;
			}

			@Override
			public Value and(Value value) {
				return this;
			}

			@Override
			public Value or(Value value) {
				return this;
			}

			@Override
			public Value not() {
				return this;
			}
		};

		Value TRUE = new BooleanValue(true);

		Value FALSE = new BooleanValue(false);

		class IntegerValue implements Value{
			private final int value;

			public IntegerValue(int value) { this.value = value; }


			@Override
			public DataType getType() {
				return DataType.INTEGER;
			}

			@Override
			public double getDoubleValue() {
				return this.value;
			}

			@Override
			public int getIntValue() {
				return this.value;
			}

			@Override
			public boolean getBooleanValue() {
				return this.value>0;
			}

			@Override
			public Value add(Value value) {
				switch (value.getType()){
					case INTEGER: return new IntegerValue(this.value+value.getIntValue());
					case REAL: return this.cast(value.getType()).add(value);
					default: return NONE;
				}
			}

			@Override
			public Value subtract(Value value) {
				switch (value.getType()){
					case INTEGER: return new IntegerValue(this.value-value.getIntValue());
					case REAL: return this.cast(value.getType()).subtract(value);
					default: return NONE;
				}
			}

			@Override
			public Value multiply(Value value) {
				switch (value.getType()){
					case INTEGER: return new IntegerValue(this.value*value.getIntValue());
					case REAL: return this.cast(value.getType()).multiply(value);
					default: return NONE;
				}
			}

			@Override
			public Value divide(Value value) {
				switch (value.getType()){
					case INTEGER: return new IntegerValue(this.value/value.getIntValue());
					case REAL: return this.cast(value.getType()).divide(value);
					default: return NONE;
				}
			}

			@Override
			public Value modulo(Value value) {
				switch (value.getType()){
					case INTEGER: return new IntegerValue(this.value%value.getIntValue());
					case REAL: return this.cast(value.getType()).modulo(value);
					default: return NONE;
				}
			}

			@Override
			public Value pow(Value value) {
				switch (value.getType()){
					case INTEGER: return new IntegerValue((int) Math.pow(this.value, value.getIntValue()));
					case REAL: return this.cast(value.getType()).pow(value);
					default: return NONE;
				}
			}

			@Override
			public Value minus() {
				return new IntegerValue(-this.value);
			}

			@Override
			public Value plus() {
				return new IntegerValue(+this.value);
			}

		}

		class RealValue implements Value{
			private final double value;

			public RealValue(double value) { this.value = value; }

			@Override
			public DataType getType() {
				return DataType.REAL;
			}

			@Override
			public double getDoubleValue() {
				return this.value;
			}

			@Override
			public int getIntValue() {
				return (int) this.value;
			}

			@Override
			public boolean getBooleanValue() {
				return this.value>0;
			}

			@Override
			public Value add(Value value) {
				switch (value.getType()) {
					case REAL:
					case INTEGER:
						return new RealValue(this.value+value.getDoubleValue());
					default:
						return NONE;
				}
			}

			@Override
			public Value subtract(Value value) {
				switch (value.getType()) {
					case REAL:
					case INTEGER:
						return new RealValue(this.value-getDoubleValue());
					default:
						return NONE;
				}
			}

			@Override
			public Value multiply(Value value) {
				switch (value.getType()) {
					case REAL:
					case INTEGER:
						return new RealValue(this.value*value.getDoubleValue());
					default:
						return NONE;
				}
			}

			@Override
			public Value divide(Value value) {
				switch (value.getType()) {
					case REAL:
					case INTEGER:
						return new RealValue(this.value/value.getDoubleValue());
					default:
						return NONE;
				}
			}

			@Override
			public Value modulo(Value value) {
				switch (value.getType()) {
					case REAL:
					case INTEGER:
						return new RealValue(this.value%value.getDoubleValue());
					default:
						return NONE;
				}
			}

			@Override
			public Value pow(Value value) {
				switch (value.getType()) {
					case REAL:
					case INTEGER:
						return new RealValue(Math.pow(this.value, value.getDoubleValue()));
					default:
						return NONE;
				}
			}

			@Override
			public Value minus() {
				return new RealValue(-this.value);
			}

			@Override
			public Value plus() {
				return new RealValue(+this.value);
			}
		}

		class BooleanValue implements Value {

			private final boolean value;

			public BooleanValue(boolean value) {
				this.value = value;
			}

			@Override
			public DataType getType() {
				return DataType.BOOLEAN;
			}

			@Override
			public boolean getBooleanValue() {
				return value;
			}

			@Override
			public Value and(Value value) {
				if (value.getType() == DataType.BOOLEAN) {
					return (!this.value?this:value);
				}
				return NONE;
			}

			@Override
			public Value or(Value value) {
				if (value.getType() == DataType.BOOLEAN) {
					return (this.value?this:value);
				}
				return NONE;
			}

			@Override
			public Value not() {
				return (this.value?FALSE:TRUE);
			}
		}

		//TODO
		class CharValue implements Value{

			@Override
			public DataType getType() {
				return null;
			}

			@Override
			public double getDoubleValue() {
				return Value.super.getDoubleValue();
			}

			@Override
			public int getIntValue() {
				return Value.super.getIntValue();
			}

			@Override
			public boolean getBooleanValue() {
				return Value.super.getBooleanValue();
			}

			@Override
			public Value add(Value value) {
				return Value.super.add(value);
			}

			@Override
			public Value subtract(Value value) {
				return Value.super.subtract(value);
			}

			@Override
			public Value multiply(Value value) {
				return Value.super.multiply(value);
			}

			@Override
			public Value divide(Value value) {
				return Value.super.divide(value);
			}

			@Override
			public Value modulo(Value value) {
				return Value.super.modulo(value);
			}

			@Override
			public Value pow(Value value) {
				return Value.super.pow(value);
			}

			@Override
			public Value minus() {
				return Value.super.minus();
			}

			@Override
			public Value plus() {
				return Value.super.plus();
			}

			@Override
			public Value and(Value value) {
				return Value.super.and(value);
			}

			@Override
			public Value or(Value value) {
				return Value.super.or(value);
			}

			@Override
			public Value not() {
				return Value.super.not();
			}

			@Override
			public Value cast(DataType type) {
				return Value.super.cast(type);
			}
		}

		//TODO
		class StringValue implements Value{

			@Override
			public DataType getType() {
				return null;
			}

			@Override
			public double getDoubleValue() {
				return Value.super.getDoubleValue();
			}

			@Override
			public int getIntValue() {
				return Value.super.getIntValue();
			}

			@Override
			public boolean getBooleanValue() {
				return Value.super.getBooleanValue();
			}

			@Override
			public Value add(Value value) {
				return Value.super.add(value);
			}

			@Override
			public Value subtract(Value value) {
				return Value.super.subtract(value);
			}

			@Override
			public Value multiply(Value value) {
				return Value.super.multiply(value);
			}

			@Override
			public Value divide(Value value) {
				return Value.super.divide(value);
			}

			@Override
			public Value modulo(Value value) {
				return Value.super.modulo(value);
			}

			@Override
			public Value pow(Value value) {
				return Value.super.pow(value);
			}

			@Override
			public Value minus() {
				return Value.super.minus();
			}

			@Override
			public Value plus() {
				return Value.super.plus();
			}

			@Override
			public Value and(Value value) {
				return Value.super.and(value);
			}

			@Override
			public Value or(Value value) {
				return Value.super.or(value);
			}

			@Override
			public Value not() {
				return Value.super.not();
			}

			@Override
			public Value cast(DataType type) {
				return Value.super.cast(type);
			}
		}
	}
