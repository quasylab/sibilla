package quasylab.sibilla.lang.pm.validation

import quasylab.sibilla.lang.pm.model.CallExpression
import quasylab.sibilla.lang.pm.model.Constant
import quasylab.sibilla.lang.pm.model.Expression

class ModelUtil {
	
	def buildRecursiveTable() {
		
	}
	
	def dispatch listOfUsedReferences( Constant c ) {
		
	}
	
	def dispatch listOfUsedReferences( Expression e ) {
		e.eAllContents.filter(CallExpression)
	}
	
}