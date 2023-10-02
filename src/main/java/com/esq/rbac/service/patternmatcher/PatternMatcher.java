package com.esq.rbac.service.patternmatcher;

import java.util.*;

public class PatternMatcher {
    private static final char ESCAPE = '\\';
    private static final char WILDCARD = '*';
    private final SortedSet<String> patternSet = new TreeSet<String>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            int o1wildcards = countWildcards(o1);
            int o2wildcards = countWildcards(o2);
            if (o1wildcards > 0 && o2wildcards == 0) {
                return 1;
            } else if (o1wildcards == 0 && o2wildcards > 0) {
                return -1;
            } else if ((o1.length() - o1wildcards) < (o2.length() - o2wildcards)) {
                return 1;
            } else if ((o1.length() - o1wildcards) > (o2.length() - o2wildcards)) {
                return -1;
            } else {
                return o1.compareTo(o2);
            }
        }
    });

    public PatternMatcher() {
    }

    private static boolean isCaseInSensitive;

    public static boolean isCaseInSensitive() {
        return isCaseInSensitive;
    }
    public static void setCaseInSensitive(boolean isCaseInSensitive) {
        PatternMatcher.isCaseInSensitive = isCaseInSensitive;
    }

    public PatternMatcher(Collection<String> patterns) {
        add(patterns);
    }

    public void add(String... patterns) {
        add(Arrays.asList(patterns));
    }

    public final void add(Collection<String> patterns) {
        for (String p : patterns) {
            if (patternSet.contains(p)) {
                continue;
            }
            patternSet.add(p);
        }
    }

    /**
     * Return matching patterns, ordered by pattern length
     *
     * @param input
     * @return
     */
    public List<String> find(String input) {
        List<String> result = new LinkedList<String>();
        if (input != null && !input.isEmpty()) {
            for (String pattern : patternSet) {
                if (match(input, pattern, 0, 0)) {
                    result.add(pattern);
                }
            }
        }
        return result;
    }

    private static int countWildcards(String pattern) {
        int result = 0;
        for (int i = 0; i < pattern.length(); i++) {
            switch (pattern.charAt(i)) {
                case WILDCARD:
                    result++;
                    break;
                case ESCAPE:
                    i++;
            }
        }
        return result;
    }

    /**
     * Internal matching recursive function.
     */
    private static boolean match(String input, String pattern, int stringStartNdx, int patternStartNdx) {
        int pNdx = patternStartNdx;
        int sNdx = stringStartNdx;
        int pLen = pattern.length();
        if (pLen == 1) {
            if (pattern.charAt(0) == WILDCARD) {     // speed-up
                return true;
            }
        }
        int sLen = input.length();
        boolean nextIsNotWildcard = false;

        while (true) {

            // check if end of string and/or pattern occurred
            if ((sNdx >= sLen)) {   // end of string still may have pending '*' in pattern
                while ((pNdx < pLen) && (pattern.charAt(pNdx) == WILDCARD)) {
                    pNdx++;
                }
                return pNdx >= pLen;
            }
            if (pNdx >= pLen) {         // end of pattern, but not end of the string
                return false;
            }
            char p = pattern.charAt(pNdx);    // pattern char

            // perform logic
            if (!nextIsNotWildcard) {

                if (p == ESCAPE) {
                    pNdx++;
                    nextIsNotWildcard = true;
                    continue;
                }
                /*
                 if (p == '?') {
                 sNdx++;
                 pNdx++;
                 continue;
                 }
                 */
                if (p == WILDCARD) {
                    char pnext = 0;           // next pattern char
                    if (pNdx + 1 < pLen) {
                        pnext = pattern.charAt(pNdx + 1);
                    }
                    if (pnext == WILDCARD) {         // double '*' have the same effect as one '*'
                        pNdx++;
                        continue;
                    }
                    int i;
                    pNdx++;

                    // find recursively if there is any substring from the end of the
                    // line that matches the rest of the pattern !!!
                    for (i = input.length(); i >= sNdx; i--) {
                        if (match(input, pattern, i, pNdx)) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                nextIsNotWildcard = false;
            }

            if(!isCaseInSensitive)
            {
                // check if pattern char and string char are equals with case
                if (p != input.charAt(sNdx)) {
                    return false;
                }
            }else
            {
                // check if pattern char and string char are equals with caseInSensitive
                if (Character.toLowerCase(p) != Character.toLowerCase(input.charAt(sNdx))) {
                    return false;
                }
            }
            // everything matches for now, continue
            sNdx++;
            pNdx++;
        }
    }
}
