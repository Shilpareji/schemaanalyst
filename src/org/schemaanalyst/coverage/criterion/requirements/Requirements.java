package org.schemaanalyst.coverage.criterion.requirements;

import org.schemaanalyst.coverage.criterion.predicate.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 07/03/2014.
 */
public class Requirements {

    private ArrayList<Predicate> predicates;

    public Requirements() {
        predicates = new ArrayList<>();
    }

    public List<Predicate> getPredicates() {
        return new ArrayList<>(predicates);
    }

    public void addPredicate(Predicate predicateToAdd) {
        System.out.println("Entered");
        for (Predicate predicate : predicates) {
            if (predicate.equals(predicateToAdd)) {
                System.out.println("\t" + predicateToAdd + " is equal to a predicate in the set");
                predicate.addPurposes(predicateToAdd.getPurposes());
                return;
            }
        }
        System.out.println("\t adding " + predicateToAdd);
        predicates.add(predicateToAdd);
    }

    public void addPredicates(List<Predicate> predicates) {
        for (Predicate predicate : predicates) {
            addPredicate(predicate);
        }
    }

    public void addPredicates(Requirements requirements) {
        addPredicates(requirements.getPredicates());
    }

    public int size() {
        return predicates.size();
    }
}
