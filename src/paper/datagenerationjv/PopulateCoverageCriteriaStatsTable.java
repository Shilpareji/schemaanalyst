package paper.datagenerationjv;

import org.schemaanalyst.dbms.DBMS;
import org.schemaanalyst.dbms.postgres.PostgresDBMS;
import org.schemaanalyst.sqlrepresentation.Schema;
import org.schemaanalyst.testgeneration.coveragecriterion.CoverageCriterion;
import org.schemaanalyst.testgeneration.coveragecriterion.TestRequirements;

import java.util.List;

import static paper.datagenerationjv.Instantiator.instantiateCoverageCriterion;
import static paper.datagenerationjv.Instantiator.instantiateDBMS;
import static paper.datagenerationjv.Instantiator.instantiateSchema;

/**
 *
 * Expects a clean coverage_criteria_stats table.
 *
 * Created by phil on 13/08/2014.
 */
public class PopulateCoverageCriteriaStatsTable {

    protected ResultsDatabase resultsDatabase;

    public void populate(String resultsDatabaseFileName) {
        resultsDatabase = new ResultsDatabase(resultsDatabaseFileName);

        List<String> schemaNames = resultsDatabase.getNames("schemas");
        for (String schemaName : schemaNames) {
            List<String> coverageCriteriaNames = resultsDatabase.getNames("coverage_criteria");
            for (String coverageCriterionName : coverageCriteriaNames) {
                List<String> dbmsNames = resultsDatabase.getNames("dbmses");
                for (String dbmsName : dbmsNames) {
                    populate(schemaName, coverageCriterionName, dbmsName);
                }
            }
        }
    }

    protected void populate(String schemaName, String coverageCriterionName, String dbmsName) {
        Schema schema = instantiateSchema(schemaName);
        DBMS dbms = instantiateDBMS(dbmsName);
        CoverageCriterion coverageCriterion = instantiateCoverageCriterion(coverageCriterionName, schema, dbms);

        TestRequirements tr = coverageCriterion.generateRequirements();
        int numReqs = tr.size();

        tr.reduce();
        int numReqsMinusDuplicates = tr.size();

        tr.filterInfeasible();
        int numReqsMinusInfeasible = tr.size();

        String data = "\"" + schemaName + "\", \"" + coverageCriterionName + "\", \"" + dbmsName + "\", "
                    +  numReqs + ", " +  numReqsMinusDuplicates + ", " +  numReqsMinusInfeasible;

        String sql = "INSERT INTO coverage_criteria_stats VALUES(" + data + ")";

        System.out.println(sql);

        resultsDatabase.executeInsert(sql);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("No results database file name provided");
            System.exit(1);
        }

        new PopulateCoverageCriteriaStatsTable().populate(args[0]);
    }
}
