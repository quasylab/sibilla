package quasylab.sibilla.lang.pm.validation

import com.google.inject.Inject
import java.util.List
import java.util.Set
import org.eclipse.emf.common.util.EList
import org.eclipse.xtext.util.IResourceScopeCache
import org.eclipse.xtext.xbase.lib.Functions.Function1
import quasylab.sibilla.lang.pm.model.AndExpression
import quasylab.sibilla.lang.pm.model.CallExpression
import quasylab.sibilla.lang.pm.model.Constant
import quasylab.sibilla.lang.pm.model.Expression
import quasylab.sibilla.lang.pm.model.FalseLiteral
import quasylab.sibilla.lang.pm.model.FractionOf
import quasylab.sibilla.lang.pm.model.IfThenElseExpression
import quasylab.sibilla.lang.pm.model.Macro
import quasylab.sibilla.lang.pm.model.MaxExpression
import quasylab.sibilla.lang.pm.model.MinExpression
import quasylab.sibilla.lang.pm.model.Model
import quasylab.sibilla.lang.pm.model.ModuloExpression
import quasylab.sibilla.lang.pm.model.MulDivExpression
import quasylab.sibilla.lang.pm.model.NegationExpression
import quasylab.sibilla.lang.pm.model.NotExpression
import quasylab.sibilla.lang.pm.model.NumExpression
import quasylab.sibilla.lang.pm.model.NumberOf
import quasylab.sibilla.lang.pm.model.OrExpression
import quasylab.sibilla.lang.pm.model.ReferenceableElement
import quasylab.sibilla.lang.pm.model.RelationExpression
import quasylab.sibilla.lang.pm.model.SumDiffExpression
import quasylab.sibilla.lang.pm.model.TrueLiteral

class ExpressionTypeInference {
	
	//TODO: This injection seams not working.
	@Inject IResourceScopeCache cache = IResourceScopeCache.NullImpl.INSTANCE
	
	Set<ReferenceableElement> pending = newHashSet()
	
	def dispatch inferType( NumExpression e ) {
		if (e.isIsReal) {
			return ExpressionType::DOUBLE
		} else {
			return ExpressionType::INTEGER
		}
	} 
	
	def dispatch inferType( Expression e ) {
		return ExpressionType::ERROR
	}
	
	def dispatch inferType( TrueLiteral e ) {
		return ExpressionType::BOOLEAN;
	}

	def dispatch inferType( FalseLiteral e ) {
		return ExpressionType::BOOLEAN;
	}
	
	def dispatch inferType( NotExpression e ) {
		return ExpressionType::BOOLEAN;
	}

	def dispatch inferType( FractionOf e ) {
		return ExpressionType::DOUBLE;
	}

	def dispatch inferType( NumberOf e ) {
		return ExpressionType::INTEGER;
	}

	def dispatch ExpressionType inferType( IfThenElseExpression e ) {
		return combine( 
			e ?. ifBranch ?. inferType ?: ExpressionType::ERROR , 
			e ?. elseBranch ?. inferType ?: ExpressionType::ERROR 
		)		
	}
	
	def dispatch ExpressionType inferType( NegationExpression e ) {
		val t = e . argument ?. inferType
		if ((t == ExpressionType::INTEGER)||(t == ExpressionType::DOUBLE)) {
			return t
		} else {
			ExpressionType::ERROR			
		}
	}
	
	def dispatch ExpressionType inferType( CallExpression e ) {
		e.symbol.typeOf
	}
	
	def dispatch ExpressionType inferType( MinExpression e ) {
		e.args.inferTypeOfList		
	}

	def dispatch ExpressionType inferType( MaxExpression e ) {
		e.args.inferTypeOfList		
	}
	
	def dispatch ExpressionType inferType( ModuloExpression e ) {
		val t1 = e.left ?. inferType ?: ExpressionType::ERROR	
		val t2 = e.right ?. inferType ?: ExpressionType::ERROR
		if (areIntegers(t1,t2)) {
			ExpressionType::INTEGER			
		} else {
			ExpressionType::ERROR
		}
	}
	
	def dispatch ExpressionType inferType( SumDiffExpression e ) {
		inferTypeOfBinaryArythmeticOperator(e.left,e.right);
	}
	
	def dispatch ExpressionType inferType( MulDivExpression e ) {
		inferTypeOfBinaryArythmeticOperator(e.left,e.right);
	}
	
	def dispatch ExpressionType inferType( RelationExpression e ) {
		ExpressionType::BOOLEAN
	}

	def dispatch ExpressionType inferType( AndExpression e ) {
		ExpressionType::BOOLEAN
	}
	
	def dispatch ExpressionType inferType( OrExpression e ) {
		ExpressionType::BOOLEAN
	}
	
	def ExpressionType inferTypeOfBinaryArythmeticOperator( Expression e1 , Expression e2 ) {
		val t1 = e1 ?. inferType ?: ExpressionType::ERROR
		val t2 = e2 ?. inferType ?: ExpressionType::ERROR
		if (areNumbers(t1,t2)) {
			combine(t1,t2)
		} else {
			ExpressionType::ERROR	
		}
	}

	def inferTypeOfList( EList<Expression> elist ) {
		elist.map[ it.inferType ].combine		
	}	
	
//	def computeSymbolType( String name , Expression e ) {
//		if (pending.contains(name)) {
//			ExpressionType::ERROR
//		} else {
//			if (!symbolTable.containsKey(name)) {
//				symbolTable.get(name)
//			} else {
//				registerSymbolType( name , e )
//			}
//		}
//	}

	def dispatch ExpressionType getTypeOf( Macro m ) {
		cache.get("type" -> m,m.eResource)[ 
			if (pending.contains(m)) {
				ExpressionType::ERROR			
			} else {
				m.computeType(m.value)
			}
		]
	}

	def dispatch ExpressionType getTypeOf( Constant c ) {
		cache.get("type" -> c,c.eResource)[ 
			if (pending.contains(c)) {
				ExpressionType::ERROR			
			} else {
				c.computeType(c.value)
			}
		]
	}
	
	def computeType( ReferenceableElement element, Expression e) {
		pending.add(element)
		val t = e.inferType
		pending.remove(element)
		t
	}
	
	def static combine( ExpressionType t1 , ExpressionType t2 ) {
		if (t1 == t2) {
			return t1;
		}
		if ((t1 == ExpressionType::INTEGER)&&(t2 == ExpressionType::DOUBLE)) {
			return ExpressionType::DOUBLE;
		}
		if ((t2 == ExpressionType::INTEGER)&&(t1 == ExpressionType::DOUBLE)) {
			return ExpressionType::DOUBLE;
		}		
		return ExpressionType::ERROR
	}
	
	
	def static ExpressionType combine(List<ExpressionType> elist) {
		if (elist.empty) {
			ExpressionType::ERROR
		} else {
			elist.reduce[t1,t2| combine(t1,t2)]
		}
	}
	
	def inferTypes( Model m ) {
		m.elements.filter(Macro).forEach[it.typeOf]
		m.elements.filter(Constant).forEach[it.typeOf]
	}
	
	def areIntegers( ExpressionType ... types ) {
		areOfType( ExpressionType::INTEGER , types )
	}

	def areBooleans( ExpressionType ... types ) {
		areOfType( ExpressionType::INTEGER , types )
	}

	def areDoubles( ExpressionType ... types ) {
		areOfType( ExpressionType::INTEGER , types )
	}
	
	def areNumbers( ExpressionType ... types ) {
		satisfyTypePredicate([(it==ExpressionType::INTEGER)||(it==ExpressionType::DOUBLE)],types)
	}

	def areOfType( ExpressionType type, ExpressionType ... types ) {
		satisfyTypePredicate( [it==type] , types )
	}
	
	def satisfyTypePredicate( Function1<ExpressionType,Boolean> p , ExpressionType ... types ) {
		types.forall( p )		
	}
	
	def isANumberType( ExpressionType t ) {
		(t==ExpressionType::INTEGER)||(t==ExpressionType::DOUBLE)
	}
}