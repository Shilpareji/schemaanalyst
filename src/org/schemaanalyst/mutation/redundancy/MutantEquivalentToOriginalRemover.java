package org.schemaanalyst.mutation.redundancy;

import org.schemaanalyst.mutation.equivalence.EquivalenceChecker;
import java.util.Iterator;
import java.util.List;
import org.schemaanalyst.mutation.Mutant;
import org.schemaanalyst.mutation.pipeline.MutantRemover;

/**
 * A {@link MutantRemover} that removes mutants equivalent to the original 
 * artefact, according to a provided {@link EquivalenceChecker}.
 * 
 * @author Chris J. Wright
 * @param <T> The type of the artefact being mutated.
 */
public class MutantEquivalentToOriginalRemover<T> extends EquivalenceTesterMutantRemover<T> {
    
    private T originalArtefact;

    /**
     * Constructor.
     * 
     * @param checker The equivalence checker
     * @param originalArtefact The original artefact that was mutated
     */
    public MutantEquivalentToOriginalRemover(EquivalenceChecker<T> checker, T originalArtefact) {
        super(checker);
        this.originalArtefact = originalArtefact;
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public List<Mutant<T>> removeMutants(List<Mutant<T>> mutants) {
        for (Iterator<Mutant<T>> it = mutants.iterator(); it.hasNext();) {
            Mutant<T> mutant = it.next();
            if (checker.areEquivalent(originalArtefact, mutant.getMutatedArtefact())) {
                it.remove();
            }
        }
        return mutants;
    }
    
}
