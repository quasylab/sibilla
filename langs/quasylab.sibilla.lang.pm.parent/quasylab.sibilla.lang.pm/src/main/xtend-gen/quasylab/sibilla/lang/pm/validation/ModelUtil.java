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
