package org.schemaanalyst.coverage;

import org.schemaanalyst.configuration.DatabaseConfiguration;
import org.schemaanalyst.configuration.LocationsConfiguration;
import org.schemaanalyst.coverage.criterion.Criterion;
import org.schemaanalyst.coverage.criterion.predicate.Predicate;
import org.schemaanalyst.coverage.criterion.types.CriterionFactory;
import org.schemaanalyst.coverage.testgeneration.*;
import org.schemaanalyst.coverage.testgeneration.datageneration.DirectedRandomTestCaseGenerationAlgorithm;
import org.schemaanalyst.coverage.testgeneration.datageneration.valuegeneration.CellValueGenerator;
import org.schemaanalyst.coverage.testgeneration.datageneration.valuegeneration.ExpressionConstantMiner;
import org.schemaanalyst.coverage.testgeneration.datageneration.valuegeneration.ValueInitializationProfile;
import org.schemaanalyst.coverage.testgeneration.datageneration.valuegeneration.ValueLibrary;
import org.schemaanalyst.dbms.DBMS;
import org.schemaanalyst.dbms.sqlite.SQLiteDBMS;
import org.schemaanalyst.sqlrepresentation.Schema;
import org.schemaanalyst.util.random.Random;
import org.schemaanalyst.util.random.SimpleRandom;
import org.schemaanalyst.util.runner.Runner;
import parsedcasestudy.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import static org.schemaanalyst.util.java.JavaUtils.JAVA_FILE_SUFFIX;

/**
 * Created by phil on 21/01/2014.
 */
public class GenerateSchemaCoverage extends Runner {

    boolean printUncoveredPredicates = true;

    @Override
    protected void task() {

        // these are parameters of the task (TODO: formalize these as per Runner ...)
        // Schema schema = new BankAccount();
        Schema schema = new BookTown(); // -- insert error (seen this before?)
        // Schema schema = new Cloc();
        // Schema schema = new CoffeeOrders();
        // Schema schema = new CustomerOrder();
        // Schema schema = new DellStore();
        // Schema schema = new Employee();  // -- checks now work
        // Schema schema = new Examination();   // -- checks now work
        // Schema schema = new Flights(); // -- checks now work (with value library)
        // Schema schema = new FrenchTowns();
        // Schema schema = new Inventory();
        // Schema schema = new Iso3166();
        // Schema schema = new JWhoisServer();
        // Schema schema = new NistDML181();  // now works
        // Schema schema = new NistDML182(); // now works
        // Schema schema = new NistDML183();
        // Schema schema = new NistWeather(); // -- crashes
        // Schema schema = new NistXTS748(); // -- checks now work
        // Schema schema = new NistXTS749();
        // Schema schema = new Person(); // -- crashes
        // Schema schema = new Products(); // one infeasible (expected) check constraint)
        // Schema schema = new RiskIt();
        // Schema schema = new StudentResidence(); // checks now work
        // Schema schema = new UnixUsage();
        // Schema schema = new Usda();

        DBMS dbms = new SQLiteDBMS();
        Criterion criterion = CriterionFactory.instantiate("amplifiedConstraintCACWithNullAndUniqueColumnCACCoverage");
        boolean reuseTestCases = false;

        //Search<Data> search = SearchFactory.avsDefaults(0L, 100000);
        // instantiate the test case generation algorithm
        //TestCaseGenerationAlgorithm testCaseGenerator =
        //        new SearchBasedTestCaseGenerationAlgorithm(search);

        Random random = new SimpleRandom(10L);
        TestCaseGenerationAlgorithm testCaseGenerator =
                new DirectedRandomTestCaseGenerationAlgorithm(
                        random,
                        new CellValueGenerator(
                                new ExpressionConstantMiner().mine(schema),
                                ValueInitializationProfile.SMALL,
                                random,
                                0.1,
                                0.25,
                                false),
                        500);

        // instantiate the test suite generator and generate the test suite
        TestSuiteGenerator dg = new TestSuiteGenerator(
                schema,
                criterion,
                dbms.getValueFactory(),
                testCaseGenerator,
                reuseTestCases);

        TestSuite testSuite = dg.generate();

        // execute each test case to see what the DBMS result is for each row generated (accept / row)
        TestCaseExecutor executor = new TestCaseExecutor(
                schema,
                dbms,
                new DatabaseConfiguration(),
                new LocationsConfiguration());

        executor.execute(testSuite);

        // write report to console
        printReport(schema, criterion, testSuite, dg.getFailedTestCases(), testCaseGenerator);

        // write JUnit test suite to file
        writeTestSuite(schema, dbms, testSuite, "generatedtest");
    }

    private void printReport(Schema schema,
                             Criterion criterionUsed,
                             TestSuite testSuite,
                             List<TestCase> failedTestCases,
                             TestCaseGenerationAlgorithm testCaseGenerator) {

        // print out each test suite test case
        System.out.println("SUCCESSFUL TEST CASES:");
        for (TestCase testCase : testSuite.getTestCases()) {
            printTestCase(testCase, true);
        }

        // print out each failed test case
        System.out.println("FAILED TEST CASES:");
        for (TestCase testCase : failedTestCases) {
            printTestCase(testCase, false);
        }

        printTestSuiteStats(schema, criterionUsed, testSuite, testCaseGenerator);
    }

    private void printTestCase(TestCase testCase, boolean success) {
        System.out.println("\n" + testCase);

        //if (!success) {
            // print details of the objective value computed by the datageneration
            //System.out.println("FAIL – INFO DUMP:");
            System.out.println(testCase.getInfo("info"));
        //}
    }

    private void printTestSuiteStats(Schema schema, Criterion criterionUsed, TestSuite testSuite, TestCaseGenerationAlgorithm testCaseGenerator) {
        System.out.println("\nTEST SUITE STATS:");
        System.out.println("Number of test cases: " + testSuite.getNumTestCases());
        System.out.println("Number of inserts: " + testSuite.getNumInserts());

        for (Criterion criterion : CriterionFactory.allCriteria()) {
            String name = criterion.getName();
            String starred = "";
            if (name.equals(criterionUsed.getName())) {
                starred = " (*)";
            }

            CoverageReport coverageReport = testCaseGenerator.computeCoverage(testSuite, criterion.generateRequirements(schema));
            System.out.println(name + starred + ": " + coverageReport.getCoverage());

            if (printUncoveredPredicates) {
                List<Predicate> uncovered = coverageReport.getUncovered();
                if (uncovered.size() > 0) {
                    System.out.println("Uncovered predicates: ");
                    for (Predicate predicate : uncovered) {
                        System.out.println(predicate.getPurposes() + ": " + predicate);
                    }
                }
            }
        }
    }

    private void writeTestSuite(Schema schema, DBMS dbms, TestSuite testSuite, String packageName) {
        String className = "Test" + schema.getName();

        String javaCode = new TestSuiteJavaWriter(schema, dbms, testSuite)
                .writeTestSuite(packageName, className);

        File javaFile = new File(locationsConfiguration.getSrcDir()
                + "/" + packageName + "/" + className + JAVA_FILE_SUFFIX);
        try (PrintWriter fileOut = new PrintWriter(javaFile)) {
            fileOut.println(javaCode);
            System.out.println("\n[JUnit test suite written to " + javaFile + "]");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void validateParameters() {
        // no params to validate
    }

    public static void main(String... args) {
        new GenerateSchemaCoverage().run(args);
    }
}
