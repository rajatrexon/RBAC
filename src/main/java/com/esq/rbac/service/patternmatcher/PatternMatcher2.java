package com.esq.rbac.service.patternmatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PatternMatcher2 {
    private static final Logger log = LoggerFactory.getLogger(PatternMatcher2.class);
    private final static String DELIMITER = ";";
    private final static String ESCAPED_DELIMITER = "\\;";
    private final Set<String> patterns = new HashSet<String>();
    private final ArrayList<PatternMatcher> matchers = new ArrayList<PatternMatcher>();

    public void add(String... patternVector) {
        add(Arrays.asList(patternVector));
    }

    public synchronized void add(List<String> patternVector) {
        String pattern = vectorAsString(patternVector);
        if (!patterns.contains(pattern)) {
            patterns.add(pattern);

            int dimension = 0;
            for (String p : patternVector) {
                if (matchers.size() <= dimension) {
                    matchers.add(new PatternMatcher());
                }
                matchers.get(dimension).add(p);
                dimension++;
            }
        }
    }

    public List<String> find(String... inputVector) {
        return find(Arrays.asList(inputVector));
    }

    public List<String> find(List<String> inputVector) {
        log.debug("find; inputVector={}", inputVector);
        ArrayList<List<String>> candidates = new ArrayList<List<String>>();
        int dimension = 0;
        for (String input : inputVector) {
            if (matchers.size() <= dimension) {
                // no result, when inputVector longer than any patternVector
                return null;
            }
            List<String> list = matchers.get(dimension).find(input);
            if (list.isEmpty()) {
                // no result, when one dimension has no matches
                return null;
            }
            candidates.add(list);
            dimension++;
        }

        // initialize iterators
        ArrayList<Iterator<String>> iterators = new ArrayList<Iterator<String>>(dimension);
        ArrayList<String> testVector = new ArrayList<String>(dimension);
        for (int i=0; i<dimension; i++) {
            Iterator<String> iterator = candidates.get(i).iterator();
            testVector.add(iterator.next());
            iterators.add(iterator);
        }

        // iterate
        while (true) {
            if (patterns.contains(vectorAsString(testVector))) {
                return testVector;
            }
            for (int i=dimension-1; i>=0; i--) {
                if (iterators.get(i).hasNext()) {
                    testVector.set(i, iterators.get(i).next());
                    break;
                } else if (i == 0) {
                    // finished iterations
                    return null;
                } else {
                    // reset current iterator and carry over
                    Iterator<String> iterator = candidates.get(i).iterator();
                    testVector.set(i, iterator.next());
                    iterators.set(i, iterator);
                }
            }
        }
    }

    private String vectorAsString(List<String> patternVector) {
        StringBuilder sb = new StringBuilder();
        for (String p : patternVector) {
            sb.append(p.replace(DELIMITER, ESCAPED_DELIMITER));
        }
        return sb.toString();
    }
}
