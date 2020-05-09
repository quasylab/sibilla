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

import com.google.common.collect.Iterators;
import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.emf.ecore.EObject;
import quasylab.sibilla.lang.pm.model.CallExpression;
import quasylab.sibilla.lang.pm.model.Constant;
import quasylab.sibilla.lang.pm.model.Expression;

@SuppressWarnings("all")
public class ModelUtil {
  public Object buildRecursiveTable() {
    return null;
  }
  
  protected Iterator<CallExpression> _listOfUsedReferences(final Constant c) {
    return null;
  }
  
  protected Iterator<CallExpression> _listOfUsedReferences(final Expression e) {
    return Iterators.<CallExpression>filter(e.eAllContents(), CallExpression.class);
  }
  
  public Iterator<CallExpression> listOfUsedReferences(final EObject c) {
    if (c instanceof Constant) {
      return _listOfUsedReferences((Constant)c);
    } else if (c instanceof Expression) {
      return _listOfUsedReferences((Expression)c);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(c).toString());
    }
  }
}
