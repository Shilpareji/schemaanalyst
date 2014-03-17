package _deprecated.datageneration.search.objective.data;

import _deprecated.datageneration.search.objective.ObjectiveFunction;
import _deprecated.datageneration.search.objective.ObjectiveValue;
import _deprecated.datageneration.search.objective.row.ExpressionRowObjectiveFunctionFactory;
import org.schemaanalyst.data.Row;
import org.schemaanalyst.sqlrepresentation.Table;
import org.schemaanalyst.sqlrepresentation.expression.Expression;

public class ExpressionColumnObjectiveFunction extends ColumnObjectiveFunction {

    private ObjectiveFunction<Row> rowObjFun;
    private boolean allowNull; 
    
    /**
     * Objective function for an expression (typically embedded in a CHECK
     * constraint).
     * 
     * @param table
     *            The table involved.
     * @param expression
     *            The expression on which the objective function is to be based.
     * @param description
     *            Some descriptive text to describe the objective function.
     * @param goalIsToSatisfy
     *            If set to true, the goal of the objective function will be to
     *            produce rows that all satisfy the constraint, else the goal is
     *            to produce rows that all falsify the constraint.
     * @param allowNull
     *            If set to true, NULL values are permissible as part or all of
     *            a solution.
     */
    public ExpressionColumnObjectiveFunction(
            Table table, Expression expression,
            String description, boolean goalIsToSatisfy,
            boolean allowNull) {

        super(table, expression.getColumnsInvolved(), description, goalIsToSatisfy);

        this.allowNull = allowNull;
        
        ExpressionRowObjectiveFunctionFactory factory = new ExpressionRowObjectiveFunctionFactory(
                expression, goalIsToSatisfy, allowNull);
        rowObjFun = factory.create();
    }

    public boolean isNullAllowed() {
        return allowNull;
    }
    
    @Override
    protected ObjectiveValue evaluateRow(Row row) {
        return rowObjFun.evaluate(row);
    }
}
