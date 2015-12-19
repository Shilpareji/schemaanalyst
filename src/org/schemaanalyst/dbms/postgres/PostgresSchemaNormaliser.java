package org.schemaanalyst.dbms.postgres;

import java.util.ArrayList;
import java.util.List;
import org.schemaanalyst.mutation.normalisation.SchemaNormaliser;
import org.schemaanalyst.sqlrepresentation.Column;
import org.schemaanalyst.sqlrepresentation.Schema;
import org.schemaanalyst.sqlrepresentation.Table;
import org.schemaanalyst.sqlrepresentation.constraint.CheckConstraint;
import org.schemaanalyst.sqlrepresentation.constraint.NotNullConstraint;
import org.schemaanalyst.sqlrepresentation.constraint.PrimaryKeyConstraint;
import org.schemaanalyst.sqlrepresentation.constraint.UniqueConstraint;
import org.schemaanalyst.sqlrepresentation.expression.AndExpression;
import org.schemaanalyst.sqlrepresentation.expression.ColumnExpression;
import org.schemaanalyst.sqlrepresentation.expression.Expression;
import org.schemaanalyst.sqlrepresentation.expression.ExpressionAdapter;
import org.schemaanalyst.sqlrepresentation.expression.NullExpression;
import org.schemaanalyst.sqlrepresentation.expression.ParenthesisedExpression;

/**
 *
 * @author Chris J. Wright
 */
public class PostgresSchemaNormaliser extends SchemaNormaliser {

    @Override
    public Schema normalise(Schema schema) {
        Schema duplicate = schema.duplicate();
        normalisePrimaryKeys(duplicate);
        normaliseCheckNotNull(duplicate);
        return duplicate;
    }

    /**
     * Replace each Primary Key with a Unique and not null
     *
     * @param schema The schema to normalise
     */
    private void normalisePrimaryKeys(Schema schema) {
        for (PrimaryKeyConstraint pk : schema.getPrimaryKeyConstraints()) {
            // Add UNIQUE if does not exist
            UniqueConstraint uc = new UniqueConstraint(pk.getTable(), pk.getColumns());
            if (!schema.getUniqueConstraints(pk.getTable()).contains(uc)) {
                schema.addUniqueConstraint(uc);
            }
            // Add NOT NULL if does not exist
            for (Column col : pk.getColumns()) {
                NotNullConstraint nn = new NotNullConstraint(pk.getTable(), col);
                if (!schema.getNotNullConstraints(pk.getTable()).contains(nn)) {
                    schema.addNotNullConstraint(nn);
                }
            }
            schema.removePrimaryKeyConstraint(pk.getTable());
        }
    }
    
    private void normaliseUnique(Schema schema) {
        for (Table table : schema.getTables()) {
            List<UniqueConstraint> ucs = schema.getUniqueConstraints(table);
            for (UniqueConstraint uc : ucs) {
                if (isSimplerUnique(uc, ucs)) {
                    schema.removeUniqueConstraint(uc);
                }
            }
        }
    }
    
    private boolean isSimplerUnique(UniqueConstraint uc, List<UniqueConstraint> ucs) {
        List<Column> ucCols = uc.getColumns();
        for (UniqueConstraint other : ucs) {
            List<Column> otherCols = other.getColumns();
            if (uc != other && otherCols.containsAll(ucCols)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replace each Check is not null with a not null, including those in ANDs
     *
     * @param schema The schema to normalise
     */
    private void normaliseCheckNotNull(final Schema schema) {
        for (final CheckConstraint check : schema.getCheckConstraints()) {
            ExpressionAdapter visitor = new ExpressionAdapter() {
                @Override
                public void visit(AndExpression expression) {
                    // For any subexpression that is a check is not null expression, replace with not null
                    List<Expression> newSubexpressions = new ArrayList<>();
                    for (Expression subexpression : expression.getSubexpressions()) {
                        subexpression = stripParenthesised(subexpression);
                        if (subexpression instanceof NullExpression) {
                            boolean success = replaceNullExpression((NullExpression) subexpression);
                            if (!success) {
                                newSubexpressions.add(subexpression);
                            }
                        } else {
                            newSubexpressions.add(subexpression);
                        }
                    }
                    if (newSubexpressions.isEmpty()) {
                        // Remove if no subexpressions left
                        schema.removeCheckConstraint(check);
                    } else {
                        // Else set the new subexpressions
                        expression.setSubexpressions(newSubexpressions);
                    }
                }

                @Override
                public void visit(ParenthesisedExpression expression) {
                    expression.getSubexpression().accept(this);
                }

                @Override
                public void visit(NullExpression expression) {
                    // Replace Check is not null with a not null
                    boolean success = replaceNullExpression(expression);
                    if (success) {
                        // Remove the check constraint
                        schema.removeCheckConstraint(check);
                    }
                }

                /**
                 * Attempt to replace a NullExpression with a Not Null constraint
                 * @param expression The NullExpression
                 * @return Whether it was able to be removed
                 */
                private boolean replaceNullExpression(NullExpression expression) {
                    // Only apply if it is "NOT NULL" variant
                    boolean success = false;
                    if (expression.isNotNull()) {
                        Expression subexpression = expression.getSubexpression();
                        // Only apply if constraint refers to a column
                        if (subexpression instanceof ColumnExpression) {
                            success = true;
                            ColumnExpression colExpr = (ColumnExpression) subexpression;
                            Column col = colExpr.getColumn();
                            // Create an equivalent not null, if one doesn't already exist
                            NotNullConstraint constraint = new NotNullConstraint(check.getTable(), col);
                            if (!schema.getNotNullConstraints(check.getTable()).contains(constraint)) {
                                schema.addNotNullConstraint(constraint);
                            }
                        }
                    }
                    return success;
                }
                
                private Expression stripParenthesised(Expression expression) {
                    if (expression instanceof ParenthesisedExpression) {
                        return ((ParenthesisedExpression)expression).getSubexpression();
                    } else {
                        return expression;
                    }
                }
            };
            check.getExpression().accept(visitor);
        }
    }

}
