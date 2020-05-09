/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package quasylab.sibilla.lang.pm.validation;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.util.IResourceScopeCache;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import quasylab.sibilla.lang.pm.model.AndExpression;
import quasylab.sibilla.lang.pm.model.CallExpression;
import quasylab.sibilla.lang.pm.model.Constant;
import quasylab.sibilla.lang.pm.model.Expression;
import quasylab.sibilla.lang.pm.model.FalseLiteral;
import quasylab.sibilla.lang.pm.model.FractionOf;
import quasylab.sibilla.lang.pm.model.IfThenElseExpression;
import quasylab.sibilla.lang.pm.model.Macro;
import quasylab.sibilla.lang.pm.model.MaxExpression;
import quasylab.sibilla.lang.pm.model.MinExpression;
import quasylab.sibilla.lang.pm.model.Model;
import quasylab.sibilla.lang.pm.model.ModuloExpression;
import quasylab.sibilla.lang.pm.model.MulDivExpression;
import quasylab.sibilla.lang.pm.model.NegationExpression;
import quasylab.sibilla.lang.pm.model.NotExpression;
import quasylab.sibilla.lang.pm.model.NumExpression;
import quasylab.sibilla.lang.pm.model.NumberOf;
import quasylab.sibilla.lang.pm.model.OrExpression;
import quasylab.sibilla.lang.pm.model.ReferenceableElement;
import quasylab.sibilla.lang.pm.model.RelationExpression;
import quasylab.sibilla.lang.pm.model.SumDiffExpression;
import quasylab.sibilla.lang.pm.model.TrueLiteral;
import quasylab.sibilla.lang.pm.validation.ExpressionType;

@SuppressWarnings("all")
public class ExpressionTypeInference {
  @Inject
  private IResourceScopeCache cache = IResourceScopeCache.NullImpl.INSTANCE;
  
  private Set<ReferenceableElement> pending = CollectionLiterals.<ReferenceableElement>newHashSet();
  
  protected ExpressionType _inferType(final NumExpression e) {
    boolean _isIsReal = e.isIsReal();
    if (_isIsReal) {
      return ExpressionType.DOUBLE;
    } else {
      return ExpressionType.INTEGER;
    }
  }
  
  protected ExpressionType _inferType(final Expression e) {
    return ExpressionType.ERROR;
  }
  
  protected ExpressionType _inferType(final TrueLiteral e) {
    return ExpressionType.BOOLEAN;
  }
  
  protected ExpressionType _inferType(final FalseLiteral e) {
    return ExpressionType.BOOLEAN;
  }
  
  protected ExpressionType _inferType(final NotExpression e) {
    return ExpressionType.BOOLEAN;
  }
  
  protected ExpressionType _inferType(final FractionOf e) {
    return ExpressionType.DOUBLE;
  }
  
  protected ExpressionType _inferType(final NumberOf e) {
    return ExpressionType.INTEGER;
  }
  
  protected ExpressionType _inferType(final IfThenElseExpression e) {
    ExpressionType _elvis = null;
    Expression _ifBranch = null;
    if (e!=null) {
      _ifBranch=e.getIfBranch();
    }
    ExpressionType _inferType = null;
    if (_ifBranch!=null) {
      _inferType=this.inferType(_ifBranch);
    }
    if (_inferType != null) {
      _elvis = _inferType;
    } else {
      _elvis = ExpressionType.ERROR;
    }
    ExpressionType _elvis_1 = null;
    Expression _elseBranch = null;
    if (e!=null) {
      _elseBranch=e.getElseBranch();
    }
    ExpressionType _inferType_1 = null;
    if (_elseBranch!=null) {
      _inferType_1=this.inferType(_elseBranch);
    }
    if (_inferType_1 != null) {
      _elvis_1 = _inferType_1;
    } else {
      _elvis_1 = ExpressionType.ERROR;
    }
    return ExpressionTypeInference.combine(_elvis, _elvis_1);
  }
  
  protected ExpressionType _inferType(final NegationExpression e) {
    ExpressionType _xblockexpression = null;
    {
      Expression _argument = e.getArgument();
      ExpressionType _inferType = null;
      if (_argument!=null) {
        _inferType=this.inferType(_argument);
      }
      final ExpressionType t = _inferType;
      ExpressionType _xifexpression = null;
      if ((Objects.equal(t, ExpressionType.INTEGER) || Objects.equal(t, ExpressionType.DOUBLE))) {
        return t;
      } else {
        _xifexpression = ExpressionType.ERROR;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  protected ExpressionType _inferType(final CallExpression e) {
    return this.getTypeOf(e.getSymbol());
  }
  
  protected ExpressionType _inferType(final MinExpression e) {
    return this.inferTypeOfList(e.getArgs());
  }
  
  protected ExpressionType _inferType(final MaxExpression e) {
    return this.inferTypeOfList(e.getArgs());
  }
  
  protected ExpressionType _inferType(final ModuloExpression e) {
    ExpressionType _xblockexpression = null;
    {
      ExpressionType _elvis = null;
      Expression _left = e.getLeft();
      ExpressionType _inferType = null;
      if (_left!=null) {
        _inferType=this.inferType(_left);
      }
      if (_inferType != null) {
        _elvis = _inferType;
      } else {
        _elvis = ExpressionType.ERROR;
      }
      final ExpressionType t1 = _elvis;
      ExpressionType _elvis_1 = null;
      Expression _right = e.getRight();
      ExpressionType _inferType_1 = null;
      if (_right!=null) {
        _inferType_1=this.inferType(_right);
      }
      if (_inferType_1 != null) {
        _elvis_1 = _inferType_1;
      } else {
        _elvis_1 = ExpressionType.ERROR;
      }
      final ExpressionType t2 = _elvis_1;
      ExpressionType _xifexpression = null;
      boolean _areIntegers = this.areIntegers(t1, t2);
      if (_areIntegers) {
        _xifexpression = ExpressionType.INTEGER;
      } else {
        _xifexpression = ExpressionType.ERROR;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  protected ExpressionType _inferType(final SumDiffExpression e) {
    return this.inferTypeOfBinaryArythmeticOperator(e.getLeft(), e.getRight());
  }
  
  protected ExpressionType _inferType(final MulDivExpression e) {
    return this.inferTypeOfBinaryArythmeticOperator(e.getLeft(), e.getRight());
  }
  
  protected ExpressionType _inferType(final RelationExpression e) {
    return ExpressionType.BOOLEAN;
  }
  
  protected ExpressionType _inferType(final AndExpression e) {
    return ExpressionType.BOOLEAN;
  }
  
  protected ExpressionType _inferType(final OrExpression e) {
    return ExpressionType.BOOLEAN;
  }
  
  public ExpressionType inferTypeOfBinaryArythmeticOperator(final Expression e1, final Expression e2) {
    ExpressionType _xblockexpression = null;
    {
      ExpressionType _elvis = null;
      ExpressionType _inferType = null;
      if (e1!=null) {
        _inferType=this.inferType(e1);
      }
      if (_inferType != null) {
        _elvis = _inferType;
      } else {
        _elvis = ExpressionType.ERROR;
      }
      final ExpressionType t1 = _elvis;
      ExpressionType _elvis_1 = null;
      ExpressionType _inferType_1 = null;
      if (e2!=null) {
        _inferType_1=this.inferType(e2);
      }
      if (_inferType_1 != null) {
        _elvis_1 = _inferType_1;
      } else {
        _elvis_1 = ExpressionType.ERROR;
      }
      final ExpressionType t2 = _elvis_1;
      ExpressionType _xifexpression = null;
      boolean _areNumbers = this.areNumbers(t1, t2);
      if (_areNumbers) {
        _xifexpression = ExpressionTypeInference.combine(t1, t2);
      } else {
        _xifexpression = ExpressionType.ERROR;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  public ExpressionType inferTypeOfList(final EList<Expression> elist) {
    final Function1<Expression, ExpressionType> _function = (Expression it) -> {
      return this.inferType(it);
    };
    return ExpressionTypeInference.combine(ListExtensions.<Expression, ExpressionType>map(elist, _function));
  }
  
  protected ExpressionType _getTypeOf(final Macro m) {
    Pair<String, Macro> _mappedTo = Pair.<String, Macro>of("type", m);
    final Provider<ExpressionType> _function = () -> {
      ExpressionType _xifexpression = null;
      boolean _contains = this.pending.contains(m);
      if (_contains) {
        _xifexpression = ExpressionType.ERROR;
      } else {
        _xifexpression = this.computeType(m, m.getValue());
      }
      return _xifexpression;
    };
    return this.cache.<ExpressionType>get(_mappedTo, m.eResource(), _function);
  }
  
  protected ExpressionType _getTypeOf(final Constant c) {
    Pair<String, Constant> _mappedTo = Pair.<String, Constant>of("type", c);
    final Provider<ExpressionType> _function = () -> {
      ExpressionType _xifexpression = null;
      boolean _contains = this.pending.contains(c);
      if (_contains) {
        _xifexpression = ExpressionType.ERROR;
      } else {
        _xifexpression = this.computeType(c, c.getValue());
      }
      return _xifexpression;
    };
    return this.cache.<ExpressionType>get(_mappedTo, c.eResource(), _function);
  }
  
  public ExpressionType computeType(final ReferenceableElement element, final Expression e) {
    ExpressionType _xblockexpression = null;
    {
      this.pending.add(element);
      final ExpressionType t = this.inferType(e);
      this.pending.remove(element);
      _xblockexpression = t;
    }
    return _xblockexpression;
  }
  
  public static ExpressionType combine(final ExpressionType t1, final ExpressionType t2) {
    boolean _equals = Objects.equal(t1, t2);
    if (_equals) {
      return t1;
    }
    if ((Objects.equal(t1, ExpressionType.INTEGER) && Objects.equal(t2, ExpressionType.DOUBLE))) {
      return ExpressionType.DOUBLE;
    }
    if ((Objects.equal(t2, ExpressionType.INTEGER) && Objects.equal(t1, ExpressionType.DOUBLE))) {
      return ExpressionType.DOUBLE;
    }
    return ExpressionType.ERROR;
  }
  
  public static ExpressionType combine(final List<ExpressionType> elist) {
    ExpressionType _xifexpression = null;
    boolean _isEmpty = elist.isEmpty();
    if (_isEmpty) {
      _xifexpression = ExpressionType.ERROR;
    } else {
      final Function2<ExpressionType, ExpressionType, ExpressionType> _function = (ExpressionType t1, ExpressionType t2) -> {
        return ExpressionTypeInference.combine(t1, t2);
      };
      _xifexpression = IterableExtensions.<ExpressionType>reduce(elist, _function);
    }
    return _xifexpression;
  }
  
  public void inferTypes(final Model m) {
    final Consumer<Macro> _function = (Macro it) -> {
      this.getTypeOf(it);
    };
    Iterables.<Macro>filter(m.getElements(), Macro.class).forEach(_function);
    final Consumer<Constant> _function_1 = (Constant it) -> {
      this.getTypeOf(it);
    };
    Iterables.<Constant>filter(m.getElements(), Constant.class).forEach(_function_1);
  }
  
  public boolean areIntegers(final ExpressionType... types) {
    return this.areOfType(ExpressionType.INTEGER, types);
  }
  
  public boolean areBooleans(final ExpressionType... types) {
    return this.areOfType(ExpressionType.INTEGER, types);
  }
  
  public boolean areDoubles(final ExpressionType... types) {
    return this.areOfType(ExpressionType.INTEGER, types);
  }
  
  public boolean areNumbers(final ExpressionType... types) {
    final Function1<ExpressionType, Boolean> _function = (ExpressionType it) -> {
      return Boolean.valueOf((Objects.equal(it, ExpressionType.INTEGER) || Objects.equal(it, ExpressionType.DOUBLE)));
    };
    return this.satisfyTypePredicate(_function, types);
  }
  
  public boolean areOfType(final ExpressionType type, final ExpressionType... types) {
    final Function1<ExpressionType, Boolean> _function = (ExpressionType it) -> {
      return Boolean.valueOf(Objects.equal(it, type));
    };
    return this.satisfyTypePredicate(_function, types);
  }
  
  public boolean satisfyTypePredicate(final Function1<ExpressionType, Boolean> p, final ExpressionType... types) {
    return IterableExtensions.<ExpressionType>forall(((Iterable<ExpressionType>)Conversions.doWrapArray(types)), p);
  }
  
  public boolean isANumberType(final ExpressionType t) {
    return (Objects.equal(t, ExpressionType.INTEGER) || Objects.equal(t, ExpressionType.DOUBLE));
  }
  
  public ExpressionType inferType(final Expression e) {
    if (e instanceof AndExpression) {
      return _inferType((AndExpression)e);
    } else if (e instanceof CallExpression) {
      return _inferType((CallExpression)e);
    } else if (e instanceof FalseLiteral) {
      return _inferType((FalseLiteral)e);
    } else if (e instanceof FractionOf) {
      return _inferType((FractionOf)e);
    } else if (e instanceof IfThenElseExpression) {
      return _inferType((IfThenElseExpression)e);
    } else if (e instanceof MaxExpression) {
      return _inferType((MaxExpression)e);
    } else if (e instanceof MinExpression) {
      return _inferType((MinExpression)e);
    } else if (e instanceof ModuloExpression) {
      return _inferType((ModuloExpression)e);
    } else if (e instanceof MulDivExpression) {
      return _inferType((MulDivExpression)e);
    } else if (e instanceof NegationExpression) {
      return _inferType((NegationExpression)e);
    } else if (e instanceof NotExpression) {
      return _inferType((NotExpression)e);
    } else if (e instanceof NumExpression) {
      return _inferType((NumExpression)e);
    } else if (e instanceof NumberOf) {
      return _inferType((NumberOf)e);
    } else if (e instanceof OrExpression) {
      return _inferType((OrExpression)e);
    } else if (e instanceof RelationExpression) {
      return _inferType((RelationExpression)e);
    } else if (e instanceof SumDiffExpression) {
      return _inferType((SumDiffExpression)e);
    } else if (e instanceof TrueLiteral) {
      return _inferType((TrueLiteral)e);
    } else if (e != null) {
      return _inferType(e);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(e).toString());
    }
  }
  
  public ExpressionType getTypeOf(final EObject c) {
    if (c instanceof Constant) {
      return _getTypeOf((Constant)c);
    } else if (c instanceof Macro) {
      return _getTypeOf((Macro)c);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(c).toString());
    }
  }
}
