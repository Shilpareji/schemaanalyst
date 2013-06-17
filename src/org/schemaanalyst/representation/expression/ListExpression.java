package org.schemaanalyst.representation.expression;

public class ListExpression extends ComposedExpression {

	public ListExpression(Expression... subexpressions) {
		super(subexpressions);
	}
	
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
