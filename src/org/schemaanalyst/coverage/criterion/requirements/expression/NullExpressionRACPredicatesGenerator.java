package org.schemaanalyst.coverage.criterion.requirements.expression;

import com.thoughtworks.xstream.mapper.Mapper;
import org.schemaanalyst.coverage.criterion.Predicate;
import org.schemaanalyst.coverage.criterion.clause.NullClause;
import org.schemaanalyst.sqlrepresentation.Table;
import org.schemaanalyst.sqlrepresentation.expression.NullExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 07/02/2014.
 */
public class NullExpressionRACPredicatesGenerator extends ExpressionRACPredicatesGenerator {

    private NullExpression nullExpression;

    public NullExpressionRACPredicatesGenerator(Table table, NullExpression nullExpression) {
        super(table);
        this.nullExpression = nullExpression;
    }

    @Override
    public List<Predicate> generateTruePredicates() {
        return generatePredicate(true);
    }

    @Override
    public List<Predicate> generateFalsePredicates() {
        return generatePredicate(false);
    }

    private List<Predicate> generatePredicate(boolean outcome) {
        List<Predicate> predicates = new ArrayList<>();
        Predicate predicate = new Predicate("Testing " + nullExpression + " is " + outcome);
        predicate.addClause(new NullClause(table, nullExpression.getColumnsInvolved().get(0), outcome));
        predicates.add(predicate);
        return predicates;
    }
}
