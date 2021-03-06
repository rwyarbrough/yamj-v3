/*
 *      Copyright (c) 2004-2013 YAMJ Members
 *      https://github.com/organizations/YAMJ/teams
 *
 *      This file is part of the Yet Another Media Jukebox (YAMJ).
 *
 *      YAMJ is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      YAMJ is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YAMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 *      Web: https://github.com/YAMJ/yamj-v3
 *
 */
package org.yamj.core.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.*;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.yamj.core.api.model.SqlScalars;
import org.yamj.core.api.options.IOptions;
import org.yamj.core.api.wrapper.IApiWrapper;

/**
 * Hibernate DAO implementation
 */
public abstract class HibernateDao {

    @Autowired
    private SessionFactory sessionFactory;
    private static final String DEFAULT_FIELD_NAME = "name";

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Store an entity.
     *
     * @param entity the entity to store
     */
    public void storeEntity(final Object entity) {
        getSession().saveOrUpdate(entity);
    }

    /**
     * Store all entities.
     *
     * @param entities the entities to store
     */
    @SuppressWarnings("rawtypes")
    public void storeAll(final Collection entities) {
        if (entities != null && entities.size() > 0) {
            Session session = getSession();
            for (Object entity : entities) {
                session.saveOrUpdate(entity);
            }
        }
    }

    /**
     * Save an entity.
     *
     * @param entity the entity to save
     */
    public void saveEntity(final Object entity) {
        getSession().save(entity);
    }

    /**
     * Update an entity.
     *
     * @param entity the entity to update
     */
    public void updateEntity(final Object entity) {
        getSession().update(entity);
    }

    /**
     * Delete an entity.
     *
     * @param entity the entity to delete
     */
    public void deleteEntity(final Object entity) {
        getSession().delete(entity);
    }

    /**
     * Get a single object by its id
     *
     * @param <T>
     * @param entityClass
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getById(Class<T> entityClass, Serializable id) {
        return (T) getSession().get(entityClass, id);
    }

    /**
     * Get a single object using the name field
     *
     * @param <T>
     * @param entityClass
     * @param name
     * @return
     */
    public <T> T getByName(Class<? extends T> entityClass, String name) {
        return getByField(entityClass, DEFAULT_FIELD_NAME, name);
    }

    /**
     * Get a single object by the passed field
     *
     * @param <T>
     * @param entityClass
     * @param field
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getByField(Class<? extends T> entityClass, String field, String name) {
        return (T) getSession().byNaturalId(entityClass).using(field, name).load();
    }

    /**
     * Convert row object to a string.
     *
     * @param rowElement
     * @return <code>String</code>
     */
    protected String convertRowElementToString(Object rowElement) {
        if (rowElement == null) {
            return null;
        } else if (rowElement instanceof String) {
            return (String) rowElement;
        } else {
            return rowElement.toString();
        }
    }

    /**
     * Convert row object to Integer.
     *
     * @param rowElement
     * @return <code>Integer</code>
     */
    protected Integer convertRowElementToInteger(Object rowElement) {
        if (rowElement == null) {
            return Integer.valueOf(0);
        } else if (StringUtils.isNumeric(rowElement.toString())) {
            return Integer.valueOf(rowElement.toString());
        } else {
            return Integer.valueOf(0);
        }
    }

    /**
     * Convert row object to Long.
     *
     * @param rowElement
     * @return <code>Long</code>
     */
    protected Long convertRowElementToLong(Object rowElement) {
        if (rowElement == null) {
            return Long.valueOf(0);
        } else if (StringUtils.isNumeric(rowElement.toString())) {
            return Long.valueOf(rowElement.toString());
        } else {
            return Long.valueOf(0);
        }
    }

    /**
     * Convert row object to date.
     *
     * @param rowElement
     * @return
     */
    protected Date convertRowElementToDate(Object rowElement) {
        if (rowElement == null) {
            return null;
        } else if (rowElement instanceof Date) {
            return (Date) rowElement;
        } else {
            // TODO invalid date
            return null;
        }
    }

    /**
     * Convert row object to date.
     *
     * @param rowElement
     * @return
     */
    protected Timestamp convertRowElementToTimestamp(Object rowElement) {
        if (rowElement == null) {
            return null;
        } else if (rowElement instanceof Timestamp) {
            return (Timestamp) rowElement;
        } else {
            // TODO invalid timestamp
            return null;
        }
    }

    /**
     * Convert row object to big decimal.
     *
     * @param rowElement
     * @return <code>BigDecimal</code>
     */
    protected BigDecimal convertRowElementToBigDecimal(Object rowElement) {
        if (rowElement == null) {
            return BigDecimal.ZERO;
        } else if (rowElement instanceof BigDecimal) {
            return (BigDecimal) rowElement;
        } else {
            return new BigDecimal(rowElement.toString());
        }
    }

    @SuppressWarnings("rawtypes")
    protected void applyNamedParameterToQuery(Query queryObject, String paramName, Object value) throws HibernateException {
        if (value instanceof Collection) {
            queryObject.setParameterList(paramName, (Collection) value);
        } else if (value instanceof Object[]) {
            queryObject.setParameterList(paramName, (Object[]) value);
        } else {
            queryObject.setParameter(paramName, value);
        }
    }

    /**
     * Find entries by id.
     *
     * @param queryString the query string.
     * @param id the id
     * @return list of entities
     */
    @SuppressWarnings("rawtypes")
    public List findById(CharSequence queryString, Long id) {
        Query queryObject = getSession().createQuery(queryString.toString());
        queryObject.setCacheable(true);
        queryObject.setParameter("id", id);
        return queryObject.list();
    }

    /**
     * Find list of entities by named parameters.
     *
     * @param queryString the query string.
     * @param params the named parameters
     * @return list of entities
     */
    @SuppressWarnings("rawtypes")
    public List findByNamedParameters(CharSequence queryString, Map<String, Object> params) {
        Query query = getSession().createQuery(queryString.toString());
        query.setCacheable(true);
        for (Entry<String, Object> param : params.entrySet()) {
            applyNamedParameterToQuery(query, param.getKey(), param.getValue());
        }
        return query.list();
    }

    /**
     * Execute a query return the results.
     *
     * Puts the total count returned from the query into the wrapper
     *
     * @param sql
     * @param wrapper
     * @return
     */
    public List<Object[]> executeQuery(String sql, IApiWrapper wrapper) {
        SqlScalars ss = new SqlScalars(sql);
        return executeQueryWithTransform(Object[].class, ss, wrapper);
    }

    /**
     * Execute a query return the results
     *
     * Gets the options from the wrapper for start and max Puts the total count returned from the query into the wrapper
     *
     * @param <T>
     * @param T The class to return the transformed results of.
     * @param sqlScalars
     * @param wrapper
     * @return
     */
    public <T> List<T> executeQueryWithTransform(Class T, SqlScalars sqlScalars, IApiWrapper wrapper) {
        SQLQuery query = sqlScalars.createSqlQuery(getSession());
        query.setReadOnly(true);
        query.setCacheable(true);

        // TODO: Add a transformation if the class is not "Object"
        if (T != null && !T.equals(Object[].class)) {
            query.setResultTransformer(Transformers.aliasToBean(T));
        }

        // Add the scalars to the query
        sqlScalars.populateScalars(query);

        List<T> queryResults = query.list();

        // If the wrapper is populated, then run the query to get the maximum results
        if (wrapper != null) {
            wrapper.setTotalCount(queryResults.size());

            // If there is a start or max set, we will need to re-run the query after setting the options
            IOptions options = wrapper.getOptions();
            if (options != null) {
                if (options.getStart() > 0 || options.getMax() > 0) {
                    if (options.getStart() > 0) {
                        query.setFirstResult(options.getStart());
                    }

                    if (options.getMax() > 0) {
                        query.setMaxResults(options.getMax());
                    }
                    // This will get the trimmed list
                    queryResults = query.list();
                }
            }
        }

        return queryResults;
    }
}
