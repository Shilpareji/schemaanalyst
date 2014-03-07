package org.schemaanalyst.coverage.testgeneration.datageneration;

import org.schemaanalyst.coverage.criterion.predicate.Predicate;
import org.schemaanalyst.coverage.testgeneration.TestCase;
import org.schemaanalyst.coverage.testgeneration.TestCaseGenerationAlgorithm;
import org.schemaanalyst.coverage.testgeneration.datageneration.checker.PredicateChecker;
import org.schemaanalyst.coverage.testgeneration.datageneration.fixer.PredicateFixer;
import org.schemaanalyst.coverage.testgeneration.datageneration.objectivefunction.PredicateObjectiveFunction;
import org.schemaanalyst.coverage.testgeneration.datageneration.valuegeneration.CellValueGenerator;
import org.schemaanalyst.data.Data;
import org.schemaanalyst.datageneration.search.Search;
import org.schemaanalyst.datageneration.search.objective.ObjectiveValue;
import org.schemaanalyst.util.random.Random;

/**
 * Created by phil on 26/02/2014.
 */
public class DirectedRandomTestCaseGenerationAlgorithm extends TestCaseGenerationAlgorithm {

    private Random random;
    private CellValueGenerator cellValueGenerator;
    private int maxEvaluations;

    public DirectedRandomTestCaseGenerationAlgorithm(Random random, CellValueGenerator cellValueGenerator, int maxEvaluations) {
        this.random = random;
        this.cellValueGenerator = cellValueGenerator;
        this.maxEvaluations = maxEvaluations;
    }

    @Override
    public TestCase generateTestCase(Data data, Data state, Predicate predicate) {
        PredicateChecker predicateChecker = new PredicateChecker(predicate, data, state);
        PredicateFixer predicateFixer = new PredicateFixer(predicateChecker, random, cellValueGenerator);

        // use a start initialiser?

        boolean success = predicateChecker.check();
        int evaluations = 0;
        while (!success && evaluations < maxEvaluations) {
            predicateFixer.attemptFix();
            evaluations ++;
            success = predicateChecker.check();
        }

        TestCase testCase = new TestCase(data, state, predicate, success);

        if (!success) {
            testCase.addInfo("dump", predicateChecker.getDump());
        }

        return testCase;
    }

    @Override
    public boolean testCaseSatisfiesPredicate(TestCase testCase, Predicate predicate) {
        PredicateObjectiveFunction objFun = new PredicateObjectiveFunction(predicate, testCase.getState());
        ObjectiveValue objVal = objFun.evaluate(testCase.getData());
        return objVal.isOptimal();
    }
}
