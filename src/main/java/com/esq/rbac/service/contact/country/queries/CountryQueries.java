package com.esq.rbac.service.contact.country.queries;

public interface CountryQueries {

    String LIST_ALL_COUNTRIES_DATA =
            "select c from Country c order by c.iso ASC";
}
