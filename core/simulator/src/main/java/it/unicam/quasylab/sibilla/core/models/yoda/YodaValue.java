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

package it.unicam.quasylab.sibilla.core.models.yoda;


import java.util.Arrays;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.IntStream;

/**
 * The interface <code>YodaValue</code> represents a value handled by YodaAgents
 */
public interface YodaValue {

    YodaValue NONE = new NoneValue();
    YodaValue FALSE = new BooleanValue(false);
    YodaValue TRUE = new BooleanValue(true);
    IntFunction<YodaValue> INTEGER_VALUE = IntegerValue::new;
    DoubleFunction<YodaValue> REAL_VALUE = RealValue::new;
    //List value

    /**
     * Taken an input integerevalue, this method returns the corresponding boolean value
     *
     * @param i the value to determine
     * @return the corresponding boolean value
     */
    static YodaValue getBooleanOf(int i){
        if (i>0){
            return TRUE;
        }else{
            return FALSE;
        }
    }

    /**
     * Taken an input double value, this method returns the corresponding boolean value
     *
     * @param d the value to determine
     * @return the corresponding boolean value
     */
    static YodaValue getBooleanOf(double d){
        if (d>0){
            return TRUE;
        }else{
            return FALSE;
        }
    }

    static YodaValue integerOf(int v){
        return new IntegerValue(v);
    }

    static YodaValue realOf(double v) {
        return new RealValue(v);
    }

    /**
     * This method returns the type of the value
     *
     * @return the type of the value
     */
    YodaType getType();

    /**
     * This method returns the cast of this value to the input type{@link YodaType}
     *
     * @param type type to cast this value
     * @return the cast of this value to the input type {@link YodaType}
     */
    default YodaValue cast(YodaType type){
        return NONE;
    }

    /**
     * This method returns true if the value is a true value, a non negative number, or a non empty sequence
     *
     * @return true if the value is a true value, a non negative number, or a non empty sequence
     */
    default YodaValue isTrue(){
        return NONE;
    }

    /**
     * This method return the integer representation of this value, if exists
     *
     * @return the integer representation of this value
     */
    default Optional<IntegerValue> integerValue(){
        return Optional.empty();
    }

    /**
     * This method the integer representation of this value
     *
     * @return
     */
    default Optional<RealValue> realValue(){
        return Optional.empty();
    }

    /**
     *This method returns the sum of this value with the input value
     *
     * @param value an input value
     * @return the sum of the two values
     */
    default YodaValue sum(YodaValue value){
        return NONE;
    }

    /**
     *This method returns the difference of this value with the input value
     *
     * @param value an input value
     * @return the difference of the two values
     */
    default YodaValue subtract(YodaValue value){
        return NONE;
    }

    /**
     *This method returns the multiplication of this value with the input value
     *
     * @param value an input value
     * @return the multiplication of the two values
     */
    default YodaValue multiply(YodaValue value){
        return NONE;
    }

    /**
     *This method returns the division of this value with the input value
     *
     * @param value an input value
     * @return the division of the two values
     */
    default YodaValue divide(YodaValue value){
        return NONE;
    }

    /**
     *This method returns the module of this value with the input value
     *
     * @param value an input value
     * @return the module of this value with the input value
     */
    default YodaValue module(YodaValue value){
        return NONE;
    }

    /**
     *This method returns the power of this value with the input value
     *
     * @param value an input value
     * @return the power of this value with the input value
     */
    default YodaValue pow(YodaValue value){
        return NONE;
    }

    /**
     * This method returns the exp of this value
     *
     * @return the exp of this value
     */
    default YodaValue exp(){
        return NONE;
    }

    /**
     * This method returns the natural logarithm of this value
     *
     * @return the natural logarithm of this value
     */
    default YodaValue logn(){
        return NONE;
    }

    /**
     * This method returns the logarithm with the input value base of this value
     *
     * @return the logarithm with the input value base of this value
     */
    default YodaValue log(YodaValue value){
        return NONE;
    }

    /**
     * This method returns the 10-based logarithm of this value
     *
     * @return the 10-based logarithm of this value
     */
    default YodaValue log10(){
        return NONE;
    }

    /**
     * This method returns the negation of this value
     *
     * @return the negation of this value
     */
    default YodaValue negation(){
        return NONE;
    }

    /**
     * This method returns the absolute of this value
     *
     * @return the absolute of this value
     */
    default YodaValue absolute(){
        return NONE;
    }

    /**
     * This method returns the max between this value and the input value
     *
     * @param value a value to compare
     * @return the max between this value and the input value
     */
    default YodaValue max(YodaValue value){
        return NONE;
    }

    /**
     * This method returns the min between this value and the input value
     *
     * @param value a value to compare
     * @return the min between this value and the input value
     */
    default YodaValue min(YodaValue value){
        return NONE;
    }

    /**
     * This method returns the ceiling of this value
     *
     * @return the ceiling of this value
     */
    default YodaValue ceil(){
        return NONE;
    }

    /**
     * This method returns the floor of this value
     *
     * @return the floor of this value
     */
    default YodaValue floor(){
        return NONE;
    }

    /**
     * This method returns the evaluation of the first parameter if this value is <code>true</code>
     * or the evaluation of the second parameter if this is <code>false</code>.
     * @param thenValue a value supplier
     * @param elseValue a value supplier
     * @return the evaluation of the first parameter if this value is <code>true</code>
     * or the evaluation of the second parameter if this is <code>false</code>.
     */
    default YodaValue ifThenElse(Supplier<YodaValue> thenValue, Supplier<YodaValue> elseValue){
        return NONE;
    }

    /**
     * This method returns the conjunction of this value with the input one
     *
     * @param value a input value
     * @return the conjunction of this value with the input one
     */
    default YodaValue conjunction(YodaValue value){
        return NONE;
    }

    /**
     * This method returns the disjunction of this value with the input one
     *
     * @param value a input value
     * @return the disjunction of this value with the input one
     */
    default YodaValue disjunction(YodaValue value){
        return NONE;
    }

    /**
     * This method returns the sin of this value
     *
     * @return the sin of this value
     */
    default YodaValue sin(){
        return NONE;
    }

    /**
     * This method returns the sinh of this value
     *
     * @return the sinh of this value
     */
    default YodaValue sinh(){
        return NONE;
    }

    /**
     * This method returns the asin of this value
     *
     * @return the asin of this value
     */
    default YodaValue asin(){
        return NONE;
    }

    /**
     * This method returns the cos of this value
     *
     * @return the cos of this value
     */
    default YodaValue cos(){
        return NONE;
    }

    /**
     * This method returns the cosh of this value
     *
     * @return the cosh of this value
     */
    default YodaValue cosh(){
        return NONE;
    }

    /**
     * This method returns the acos of this value
     *
     * @return the acos of this value
     */
    default YodaValue acos(){
        return NONE;
    }

    /**
     * This method returns the tan of this value
     *
     * @return the tan of this value
     */
    default YodaValue tan(){
        return NONE;
    }

    /**
     * This method returns the tanh of this value
     *
     * @return the tanh of this value
     */
    default YodaValue tanh(){
        return NONE;
    }

    /**
     * This method returns the atan of this value
     *
     * @return the atan of this value
     */
    default YodaValue atan(){
        return NONE;
    }

    /**
     * This method returns the length of a sequence identified by this value or
     * <code>NONE</code> if not a sequence
     *
     * @return the length of a sequence identified by this value or
     * <code>NONE</code> if not a sequence
     */
    default YodaValue length(){
        return NONE;
    }

    /**
     * This method returns the first element of this sequence. If this is not a sequence, NONE is returned
     *
     * @return the first element of this sequence. If this is not a sequence, NONE is returned
     */
    default YodaValue first(){
        return NONE;
    }

    /**
     * This method returns the last element of this sequence. If this is not a sequence, NONE is returned
     *
     * @return the last element of this sequence. If this is not a sequence, NONE is returned
     */
    default YodaValue last(){
        return NONE;
    }

    /**
     * This method returns the sequence obtained by adding the input value at the beginning of this sequence.
     * If this is not a sequence, NONE is returned.
     *
     * @param value a input value
     * @return the sequence obtained by adding the input value at the beginning of this sequence.
     * If this is not a sequence, NONE is returned.
     */
    default YodaValue addFirst(YodaValue value){
        return NONE;
    }

    /**
     * This method returns the sequence obtained by adding the input value at the end of this sequence.
     * If this is not a sequence, NONE is returned.
     *
     * @param value a input value
     * @return the sequence obtained by adding the input value at the end of this sequence.
     * If this is not a sequence, NONE is returned.
     */
    default YodaValue addLast(YodaValue value){
        return NONE;
    }

    /**
     * This method return the element indexed by the input index, if this is a sequence
     *
     * @param idx the input index
     * @return the element indexed by the input index, if this is a sequence
     */
    default YodaValue select(YodaValue idx){
        return NONE;
    }

    /**
     * This method returns the concatenation of this value sequence with the input one
     *
     * @param value a input value representing a sequence
     * @return the concatenation of this value sequence with the input one
     */
    default YodaValue concat(YodaValue value){
        return NONE;
    }



    class NoneValue implements YodaValue {

        @Override
        public YodaType getType(){return YodaType.NONE_TYPE;}
    }

    class BooleanValue implements YodaValue {
        private final boolean value;

        public BooleanValue(boolean value) { this.value = value; }

        @Override
        public YodaType getType() {
            return YodaType.BOOLEAN_TYPE;
        }

        //TODO
        @Override
        public YodaValue cast(YodaType type) {
            return YodaValue.super.cast(type);
        }


        @Override
        public YodaValue negation() {
            return (value?FALSE:TRUE);
        }

        @Override
        public YodaValue ifThenElse(Supplier<YodaValue> thenValue, Supplier<YodaValue> elseValue) {
            return (value?thenValue.get():elseValue.get());
        }

        @Override
        public YodaValue conjunction(YodaValue other) {
            if (other.getType() == YodaType.BOOLEAN_TYPE){
                return (value?other:this);
            }
            return NONE;
        }

        @Override
        public YodaValue disjunction(YodaValue other) {
            if (other.getType()==YodaType.BOOLEAN_TYPE){
                return (value?this:other);
            }
            return NONE;
        }
    }

    class IntegerValue implements YodaValue {

        private final int value;

        public IntegerValue(int value) {
            this.value=value;
        }

        public int value() {
            return value;
        }

        private IntegerValue doApply(IntUnaryOperator operator){return new IntegerValue(operator.applyAsInt(this.value));}

        private IntegerValue doApply(IntBinaryOperator operator, IntegerValue other){
            return new IntegerValue(operator.applyAsInt(this.value,other.value));
        }

        @Override
        public YodaType getType() {
            return YodaType.INTEGER_TYPE;
        }

        //TODO
        @Override
        public YodaValue cast(YodaType type) {
            return NONE;
        }

        @Override
        public YodaValue isTrue() {
            return getBooleanOf(value);
        }

        @Override
        public Optional<IntegerValue> integerValue() {
            return Optional.of(this);
        }

        @Override
        public YodaValue sum(YodaValue other) {
            if (other.getType() == YodaType.INTEGER_TYPE){
                return doApply(Integer::sum, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public YodaValue subtract(YodaValue other) {
            if (other.getType() == YodaType.INTEGER_TYPE){
                return doApply((x,y) -> x-y, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public YodaValue multiply(YodaValue other) {
            if (other.getType() == YodaType.INTEGER_TYPE){
                return doApply((x,y)->x*y, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public YodaValue divide(YodaValue other) {
            if (other.getType() == YodaType.INTEGER_TYPE){
                IntegerValue otherInt = (IntegerValue) other;
                if (otherInt.value == 0){
                    return NONE;
                }
                return doApply((x,y)->x/y, otherInt);
            }

            return NONE;
        }

        @Override
        public YodaValue module(YodaValue other) {
            if (other.getType() == YodaType.INTEGER_TYPE){
                return doApply((x,y) -> x%y, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public YodaValue absolute() {
            return doApply(Math::abs);
        }

        //TODO
        @Override
        public YodaValue pow(YodaValue other) {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue exp() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue logn() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue log(YodaValue value) {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue log10() {
            return NONE;
        }

        @Override
        public YodaValue max(YodaValue other) {
            if (other.getType() == YodaType.INTEGER_TYPE){
                return doApply(Math::max, (IntegerValue) other);
            }
            return NONE;
        }

        @Override
        public YodaValue min(YodaValue other) {
            if (other.getType() == YodaType.INTEGER_TYPE){
                return doApply(Math::min, (IntegerValue) other);
            }
            return NONE;
        }

        //TODO
        @Override
        public YodaValue ifThenElse(Supplier<YodaValue> thenValue, Supplier<YodaValue> elseValue) {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue sin() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue sinh() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue asin() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue cos() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue cosh() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue acos() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue tan() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue tanh() {
            return NONE;
        }

        //TODO
        @Override
        public YodaValue atan() {
            return NONE;
        }
    }

    class RealValue implements YodaValue{

        private final double value;

        public RealValue(double value) {this.value=value;}

        private RealValue doApply(DoubleUnaryOperator operator){return new RealValue(operator.applyAsDouble(this.value));}

        private RealValue doApply(DoubleBinaryOperator operator, RealValue other){
            return new RealValue(operator.applyAsDouble(this.value, other.value));
        }

        @Override
        public YodaType getType() {
            return YodaType.REAL_TYPE;
        }

        //TODO
        @Override
        public YodaValue cast(YodaType type) {
            return YodaValue.super.cast(type);
        }

        @Override
        public YodaValue isTrue() {
            return getBooleanOf(value);
        }


        @Override
        public Optional<RealValue> realValue() {
            return Optional.of(this);
        }

        @Override
        public YodaValue sum(YodaValue other) {
            if (YodaType.REAL_TYPE==other.getType()){
                return doApply(Double::sum, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public YodaValue subtract(YodaValue other) {
            if (YodaType.REAL_TYPE==other.getType()){
                return doApply((x,y) -> x-y,((RealValue) other));
            }
            return NONE;
        }

        @Override
        public YodaValue multiply(YodaValue other) {
            if (YodaType.REAL_TYPE==other.getType()){
                return doApply((x,y)-> x*y, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public YodaValue divide(YodaValue other) {
            if (YodaType.REAL_TYPE==other.getType()){
                return doApply((x,y) -> x/y, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public YodaValue module(YodaValue other) {
            if (YodaType.REAL_TYPE==other.getType()){
                return doApply((x,y) -> x%y, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public YodaValue pow(YodaValue other) {
            if (YodaType.REAL_TYPE==other.getType()){
                return doApply(Math::pow, ((RealValue) other));
            }
            return NONE;
        }

        //TODO
        @Override
        public YodaValue exp() {
            return YodaValue.super.exp();
        }

        //TODO
        @Override
        public YodaValue logn() {
            return YodaValue.super.logn();
        }

        //TODO
        @Override
        public YodaValue log(YodaValue value) {
            return YodaValue.super.log(value);
        }

        @Override
        public YodaValue log10() {
            return doApply(Math::log10);
        }

        @Override
        public YodaValue max(YodaValue other) {
            if (YodaType.REAL_TYPE==other.getType()){
                return doApply(Math::max, ((RealValue) other));
            }
            return NONE;
        }

        @Override
        public YodaValue min(YodaValue other) {
            if (YodaType.REAL_TYPE==other.getType()){
                return doApply(Math::max, ((RealValue) other));
            }
            return NONE;
        }

        //TODO
        @Override
        public YodaValue ifThenElse(Supplier<YodaValue> thenValue, Supplier<YodaValue> elseValue) {
            return YodaValue.super.ifThenElse(thenValue, elseValue);
        }

        @Override
        public YodaValue sin() {
            return doApply(Math::sin);
        }

        @Override
        public YodaValue sinh() {
            return doApply(Math::sinh);
        }

        @Override
        public YodaValue asin() {
            return doApply(Math::asin);
        }

        @Override
        public YodaValue cos() {
            return doApply(Math::cos);
        }

        @Override
        public YodaValue cosh() {
            return doApply(Math::cosh);
        }

        @Override
        public YodaValue acos() {
            return doApply(Math::acos);
        }

        @Override
        public YodaValue tan() {
            return doApply(Math::tan);
        }

        @Override
        public YodaValue tanh() {
            return doApply(Math::tanh);
        }

        @Override
        public YodaValue atan() {
            return doApply(Math::atan);
        }
    }

    class ListValue implements YodaValue{

        private final YodaType elementType;
        private final YodaValue[] values;

        public ListValue(YodaType elementType, YodaValue ... values){
            this.elementType=elementType;
            this.values= values;
        }

        @Override
        public YodaType getType() {
            return null;
        }

        @Override
        public YodaValue isTrue() {
            return getBooleanOf(values.length);
        }

        @Override
        public YodaValue length() {
            return INTEGER_VALUE.apply(values.length);
        }

        @Override
        public YodaValue first() {
            if (values.length>0) {
                return values[0];
            }
            return NONE;
        }

        @Override
        public YodaValue last() {
            return new ListValue(this.elementType, Arrays.copyOfRange(this.values,1,this.values.length));
        }

        @Override
        public YodaValue addFirst(YodaValue value) {
            if (value.getType().equals(this.elementType)){
                YodaValue[] newValues = IntStream.range(0, this.values.length + 1)
                        .mapToObj(i -> (i>0? this.values[i-1]:value))
                        .toArray(YodaValue[]::new);
                return new ListValue(this.elementType, newValues);
            }
            return NONE;
        }

        @Override
        public YodaValue addLast(YodaValue value) {
            if (value.getType().equals(this.elementType)){
                YodaValue[] newValues = IntStream.range(0, this.values.length + 1)
                        .mapToObj(i -> (i<this.values.length?this.values[i]:value))
                        .toArray(YodaValue[]::new);
                return new ListValue(this.elementType, newValues);
            }
            return NONE;
        }

        @Override
        public YodaValue select(YodaValue idx) {
            if (idx.getType() != YodaType.INTEGER_TYPE){
                return NONE;
            }
            IntegerValue idxValue = (IntegerValue) idx;
            if ((idxValue.value<0)||(idxValue.value>=values.length)){
                return NONE;
            }
            return this.values[idxValue.value];
        }

        @Override
        public YodaValue concat(YodaValue value) {
            return YodaValue.super.concat(value);
        }
    }
}
