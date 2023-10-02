package com.esq.rbac.service.util;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionSort;
import com.esq.rbac.service.util.dal.Options;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SearchUtils {

    public static final String SEARCH_PARAM = "q";
    private static final String SQL_WILDCARD = "%";

    public SearchUtils() {
    }

    public static String getSearchParam(Options options, String param) {
        String q = null;
        OptionFilter optionFilter = options == null ? null : (OptionFilter)options.getOption(OptionFilter.class);
        Map<String, String> filters = optionFilter == null ? null : optionFilter.getFilters();
        if (filters != null) {
            q = (String)filters.get(param);
        }

        return q;
    }

    public static String wildcarded(String q) {
        StringBuilder sb = new StringBuilder();
        if (!q.startsWith("%")) {
            sb.append("%");
        }

        if (q != null && q.contains("[")) {
            q = q.replaceAll("\\[", "[[]");
        }

        sb.append(q.toLowerCase());
        if (!q.endsWith("%")) {
            sb.append("%");
        }

        return sb.toString();
    }

    public static String getOrderByParam(Options options, Map<String, String> sortColumnsMap) {
        StringBuilder sb = new StringBuilder("");
        OptionSort optionSort = options != null ? (OptionSort)options.getOption(OptionSort.class) : null;
        if (optionSort != null) {
            List<String> sortColumns = new LinkedList();
            Iterator var5 = optionSort.getSortProperties().iterator();

            String column;
            while(var5.hasNext()) {
                String property = (String)var5.next();
                column = " asc";
                if (property.startsWith("-")) {
                    column = " desc";
                    property = property.substring(1);
                }

                if (sortColumnsMap.get(property) != null) {
                    sortColumns.add((String)sortColumnsMap.get(property) + column);
                }
            }

            if (!sortColumns.isEmpty()) {
                sb.append(" order by ");
                int i = 1;

                for(Iterator var9 = sortColumns.iterator(); var9.hasNext(); ++i) {
                    column = (String)var9.next();
                    if (i > 1) {
                        sb.append(", ");
                    }

                    sb.append(column);
                }
            }
        }

        return sb.toString();
    }
}
