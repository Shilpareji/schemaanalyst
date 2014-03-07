package org.schemaanalyst.coverage.criterion.requirements;

import org.schemaanalyst.coverage.criterion.predicate.Predicate;
import org.schemaanalyst.coverage.criterion.requirements.expression.ExpressionCACPredicatesGenerator;
import org.schemaanalyst.sqlrepresentation.Schema;
import org.schemaanalyst.sqlrepresentation.constraint.CheckConstraint;
import org.schemaanalyst.sqlrepresentation.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 07/02/2014.
 */
public class CheckConstraintCACRequirementsGenerator extends ConstraintRequirementsGenerator {

    private Expression expression;

    public CheckConstraintCACRequirementsGenerator(Schema schema, CheckConstraint constraint) {
        super(schema, constraint);
        this.expression = constraint.getExpression();
    }

    @Override
    public List<Predicate> generateRequirements() {
        List<Predicate> requirements = new ArrayList<>();

        List<Predicate> expressionPredicates =
                ExpressionCACPredicatesGenerator.generatePredicates(table, expression);

        for (Predicate expressionPredicate : expressionPredicates) {
            Predicate predicate = generatePredicate(expressionPredicate.getPurposes());
            predicate.addClauses(expressionPredicate);
            requirements.add(predicate);
        }

        return requirements;
    }
}
