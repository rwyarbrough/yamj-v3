/*
 *      Copyright (c) 2004-2013 YAMJ Members
 *      https://github.com/organizations/YAMJ/teams
 *
 *      This file is part of the Yet Another Media Jukebox (YAMJ).
 *
 *      The YAMJ is free software: you can redistribute it and/or modify
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
 *      along with the YAMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 *      Web: https://github.com/YAMJ/yamj-v3
 *
 */
package org.yamj.core.database.dao;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.type.DateType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yamj.core.api.model.CountTimestamp;
import org.yamj.core.api.model.dto.IndexVideoDTO;
import org.yamj.common.type.MetaDataType;
import org.yamj.core.api.model.ApiWrapperList;
import org.yamj.core.api.model.SqlScalars;
import org.yamj.core.api.model.dto.IndexArtworkDTO;
import org.yamj.core.api.model.dto.IndexPersonDTO;
import org.yamj.core.api.options.OptionsIndexArtwork;
import org.yamj.core.api.options.OptionsIndexVideo;
import org.yamj.core.hibernate.HibernateDao;

@Service("apiDao")
public class ApiDao extends HibernateDao {

    private static final Logger LOG = LoggerFactory.getLogger(ApiDao.class);

    /**
     * Generate the query and load the results into the wrapper
     *
     * @param sqlString
     * @param wrapper
     */
    public void getVideoList(ApiWrapperList<IndexVideoDTO> wrapper) {
        SqlScalars sqlScalars = new SqlScalars(generateSqlForVideoList(wrapper));

        sqlScalars.addScalar("id", LongType.INSTANCE);
        sqlScalars.addScalar("videoTypeString", StringType.INSTANCE);
        sqlScalars.addScalar("title", StringType.INSTANCE);
        sqlScalars.addScalar("videoYear", IntegerType.INSTANCE);

        List<IndexVideoDTO> queryResults = executeQueryWithTransform(IndexVideoDTO.class, sqlScalars, wrapper);
        wrapper.setResults(queryResults);

        // Create and populate the ID list
        Map<MetaDataType, List<Long>> ids = new EnumMap<MetaDataType, List<Long>>(MetaDataType.class);
        for (MetaDataType mdt : MetaDataType.values()) {
            ids.put(mdt, new ArrayList());
        }

        Map<String, IndexVideoDTO> results = new HashMap<String, IndexVideoDTO>();

        for (IndexVideoDTO single : queryResults) {
            // Add the item to the map for further processing
            results.put(IndexArtworkDTO.makeKey(single), single);
            // Add the ID to the list
            ids.get(single.getVideoType()).add(single.getId());
        }

        boolean foundArtworkIds = Boolean.FALSE;    // Check to see that we have artwork to find
        // Remove any blank entries
        for (MetaDataType mdt : MetaDataType.values()) {
            if (CollectionUtils.isEmpty(ids.get(mdt))) {
                ids.remove(mdt);
            } else {
                // We've found an artwork, so we can continue
                foundArtworkIds = Boolean.TRUE;
            }
        }

        if (foundArtworkIds) {
            LOG.debug("Found artwork to process, IDs: {}", ids);
            addArtworks(ids, results, (OptionsIndexVideo) wrapper.getParameters());
        } else {
            LOG.debug("No artwork found to process, skipping.");
        }
    }

    private String generateSqlForVideoList(ApiWrapperList<IndexVideoDTO> wrapper) {
        OptionsIndexVideo options = (OptionsIndexVideo) wrapper.getParameters();
        Map<String, String> includes = options.splitIncludes();
        Map<String, String> excludes = options.splitExcludes();

        StringBuilder sbSQL = new StringBuilder();

        // Add the movie entries
        if (options.getType().equals("ALL") || options.getType().equals("MOVIE")) {
            sbSQL.append("SELECT vd.id");
            sbSQL.append(", '").append(MetaDataType.MOVIE).append("' AS videoTypeString");
            sbSQL.append(", vd.title");
            sbSQL.append(", vd.publication_year as videoYear");
            sbSQL.append(" FROM videodata vd");
            // Add genre tables for include and exclude
            if (includes.containsKey("genre") || excludes.containsKey("genre")) {
                sbSQL.append(", videodata_genres vg, genre g");
            }

            sbSQL.append(" WHERE vd.episode < 0");
            // Add joins for genres
            if (includes.containsKey("genre") || excludes.containsKey("genre")) {
                sbSQL.append(" AND vd.id=vg.data_id");
                sbSQL.append(" AND vg.genre_id=g.id");
                sbSQL.append(" AND g.name='");
                if (includes.containsKey("genre")) {
                    sbSQL.append(includes.get("genre"));
                } else {
                    sbSQL.append(excludes.get("genre"));
                }
                sbSQL.append("'");
            }

            if (includes.containsKey("year")) {
                sbSQL.append(" AND vd.publication_year=").append(includes.get("year"));
            }

            if (excludes.containsKey("year")) {
                sbSQL.append(" AND vd.publication_year!=").append(includes.get("year"));
            }
        }

        if (options.getType().equals("ALL")) {
            sbSQL.append(" UNION ");
        }

        // Add the TV entires
        if (options.getType().equals("ALL") || options.getType().equals("TV")) {
            sbSQL.append("SELECT ser.id");
            sbSQL.append(", '").append(MetaDataType.SERIES).append("' AS videoTypeString");
            sbSQL.append(", ser.title");
            sbSQL.append(", ser.start_year as videoYear");
            sbSQL.append(" FROM series ser ");
            sbSQL.append(" WHERE 1=1"); // To make it easier to add the optional include and excludes

            if (includes.containsKey("year")) {
                sbSQL.append(" AND ser.start_year=").append(includes.get("year"));
            }

            if (excludes.containsKey("year")) {
                sbSQL.append(" AND ser.start_year!=").append(includes.get("year"));
            }
        }

        if (StringUtils.isNotBlank(options.getSortby())) {
            sbSQL.append(" ORDER BY ");
            sbSQL.append(options.getSortby()).append(" ");
            sbSQL.append(options.getSortdir().toUpperCase());
        }

        return sbSQL.toString();
    }

    /**
     * Search the list of IDs for artwork and add to the artworkList.
     *
     * @param ids
     * @param artworkList
     * @param options
     */
    private void addArtworks(Map<MetaDataType, List<Long>> ids, Map<String, IndexVideoDTO> artworkList, OptionsIndexVideo options) {
        List<String> artworkRequired = options.splitArtwork();
        LOG.debug("Artwork required: {}", artworkRequired.toString());

        if (CollectionUtils.isNotEmpty(artworkRequired)) {
            SqlScalars sqlScalars = new SqlScalars();
            boolean hasMovie = CollectionUtils.isNotEmpty(ids.get(MetaDataType.MOVIE));
            boolean hasSeries = CollectionUtils.isNotEmpty(ids.get(MetaDataType.SERIES));

            if (hasMovie) {
                sqlScalars.addToSql("SELECT 'MOVIE' as sourceString, v.id as videoId, a.id as artworkId, al.id as locatedId, ag.id as generatedId, a.artwork_type as artworkTypeString, ag.cache_dir as cacheDir, ag.cache_filename as cacheFilename");
                sqlScalars.addToSql(" FROM videodata v, artwork a");
                sqlScalars.addToSql(" LEFT JOIN artwork_located al ON a.id=al.artwork_id");
                sqlScalars.addToSql(" LEFT JOIN artwork_generated ag ON al.id=ag.located_id");
                sqlScalars.addToSql(" WHERE v.id=a.videodata_id");
                sqlScalars.addToSql(" AND v.episode<0");
                sqlScalars.addToSql(" AND v.id IN (:movielist)");
                sqlScalars.addToSql(" AND a.artwork_type IN (:artworklist)");
            }

            if (hasMovie && hasSeries) {
                sqlScalars.addToSql(" UNION");
            }

            if (hasSeries) {
                sqlScalars.addToSql(" SELECT 'SERIES' as sourceString, s.id as videoId, a.id as artworkId, al.id as locatedId, ag.id as generatedId, a.artwork_type as artworkTypeString, ag.cache_dir as cacheDir, ag.cache_filename as cacheFilename");
                sqlScalars.addToSql(" FROM series s, artwork a");
                sqlScalars.addToSql(" LEFT JOIN artwork_located al ON a.id=al.artwork_id");
                sqlScalars.addToSql(" LEFT JOIN artwork_generated ag ON al.id=ag.located_id");
                sqlScalars.addToSql(" WHERE s.id=a.series_id");
                sqlScalars.addToSql(" AND s.id IN (:serieslist)");
                sqlScalars.addToSql(" AND a.artwork_type IN (:artworklist)");
            }

            sqlScalars.addScalar("sourceString", StringType.INSTANCE);
            sqlScalars.addScalar("videoId", LongType.INSTANCE);
            sqlScalars.addScalar("artworkId", LongType.INSTANCE);
            sqlScalars.addScalar("locatedId", LongType.INSTANCE);
            sqlScalars.addScalar("generatedId", LongType.INSTANCE);
            sqlScalars.addScalar("artworkTypeString", StringType.INSTANCE);
            sqlScalars.addScalar("cacheDir", StringType.INSTANCE);
            sqlScalars.addScalar("cacheFilename", StringType.INSTANCE);

            if (hasMovie) {
                sqlScalars.addParameterList("movielist", ids.get(MetaDataType.MOVIE));
            }

            if (hasSeries) {
                sqlScalars.addParameterList("serieslist", ids.get(MetaDataType.SERIES));
            }
            sqlScalars.addParameterList("artworklist", artworkRequired);

            List<IndexArtworkDTO> results = executeQueryWithTransform(IndexArtworkDTO.class, sqlScalars, null);

            LOG.trace("Found {} artworks", results.size());
            for (IndexArtworkDTO ia : results) {
                LOG.trace("  {} = {}", ia.Key(), ia.toString());
                artworkList.get(ia.Key()).addArtwork(ia);
            }
        }
    }

    /**
     * Get a single Count and Timestamp
     *
     * @param type
     * @param tablename
     * @param clause
     * @return
     */
    public CountTimestamp getCountTimestamp(MetaDataType type, String tablename, String clause) {
        if (StringUtils.isBlank(tablename)) {
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT '").append(type).append("' as typeString, ");
        sql.append("count(*) as count, ");
        sql.append("MAX(create_timestamp) as createTimestamp, ");
        sql.append("MAX(update_timestamp) as updateTimestamp, ");
        sql.append("MAX(id) as lastId ");
        sql.append("FROM ").append(tablename);
        if (StringUtils.isNotBlank(clause)) {
            sql.append(" WHERE ").append(clause);
        }

        SqlScalars sqlScalars = new SqlScalars(sql);

        sqlScalars.addScalar("typeString", StringType.INSTANCE);
        sqlScalars.addScalar("count", LongType.INSTANCE);
        sqlScalars.addScalar("createTimestamp", TimestampType.INSTANCE);
        sqlScalars.addScalar("updateTimestamp", TimestampType.INSTANCE);
        sqlScalars.addScalar("lastId", LongType.INSTANCE);

        List<CountTimestamp> results = executeQueryWithTransform(CountTimestamp.class, sqlScalars, null);
        if (CollectionUtils.isEmpty(results)) {
            return new CountTimestamp(type);
        }

        return results.get(0);
    }

    public void getPersonList(ApiWrapperList<IndexPersonDTO> wrapper) {
        SqlScalars sqlScalars = new SqlScalars();
        // Make sure to set the alias for the files for the Transformation into the class
        sqlScalars.addToSql("SELECT p.id,");
        sqlScalars.addToSql(" p.name,");
        sqlScalars.addToSql(" p.biography, ");
        sqlScalars.addToSql(" p.birth_day as birthDay, ");
        sqlScalars.addToSql(" p.birth_place as birthPlace, ");
        sqlScalars.addToSql(" p.birth_name as birthName, ");
        sqlScalars.addToSql(" p.death_day as deathDay ");
        sqlScalars.addToSql(" FROM person p");
        sqlScalars.addToSql(" WHERE 1=1");

        sqlScalars.addScalar("id", LongType.INSTANCE);
        sqlScalars.addScalar("birthDay", DateType.INSTANCE);
        sqlScalars.addScalar("birthPlace", DateType.INSTANCE);
        sqlScalars.addScalar("birthName", StringType.INSTANCE);
        sqlScalars.addScalar("deathDay", DateType.INSTANCE);

        List<IndexPersonDTO> results = executeQueryWithTransform(IndexPersonDTO.class, sqlScalars, wrapper);
        wrapper.setResults(results);
    }

    //<editor-fold defaultstate="collapsed" desc="Artwork Methods">
    public IndexArtworkDTO getArtworkById(Long id) {
        SqlScalars sqlScalars = getSqlArtwork(new OptionsIndexArtwork(id));

        List<IndexArtworkDTO> results = executeQueryWithTransform(IndexArtworkDTO.class, sqlScalars, null);
        if (CollectionUtils.isEmpty(results)) {
            return new IndexArtworkDTO();
        }

        return results.get(0);
    }

    public List<IndexArtworkDTO> getArtworkList(ApiWrapperList<IndexArtworkDTO> wrapper) {
        SqlScalars sqlScalars = getSqlArtwork((OptionsIndexArtwork) wrapper.getParameters());
        return executeQueryWithTransform(IndexArtworkDTO.class, sqlScalars, wrapper);
    }

    private SqlScalars getSqlArtwork(OptionsIndexArtwork options) {
        SqlScalars sqlScalars = new SqlScalars();

        sqlScalars.addToSql("SELECT a.id AS artworkId,");
        sqlScalars.addToSql(" al.id AS locatedId,");
        sqlScalars.addToSql(" ag.id AS generatedId,");
        sqlScalars.addToSql(" a.season_id AS seasonId,");
        sqlScalars.addToSql(" a.series_id AS seriesId,");
        sqlScalars.addToSql(" a.videodata_id AS videodataId,");
        sqlScalars.addToSql(" a.artwork_type AS artworkTypeString,");
        sqlScalars.addToSql(" ag.cache_filename AS cacheFilename,");
        sqlScalars.addToSql(" ag.cache_dir AS cacheDir");
        sqlScalars.addToSql(" FROM artwork a");
        sqlScalars.addToSql(" LEFT JOIN artwork_located al on a.id=al.artwork_id");
        sqlScalars.addToSql(" LEFT JOIN artwork_generated ag on al.id=ag.located_id");
        sqlScalars.addToSql(" WHERE 1=1"); // Make appending restrictions easier
        if (options != null) {
            if (options.getId() > 0L) {
                sqlScalars.addToSql(" AND a.id=:id");
                sqlScalars.addParameter("id", options.getId());
            }

            if (CollectionUtils.isNotEmpty(options.getArtwork())) {
                sqlScalars.addToSql(" AND a.artwork_type IN (:artworklist)");
                sqlScalars.addParameterList("artworklist", options.getArtwork());
            }

            if (CollectionUtils.isNotEmpty(options.getVideo())) {
                StringBuilder sb = new StringBuilder("AND (");
                boolean first = Boolean.TRUE;
                for (String type : options.getVideo()) {
                    MetaDataType mdt = MetaDataType.fromString(type);
                    LOG.info("Type: {}, MDT: {}, first: {}", type, mdt, first);
                    if (first) {
                        first = Boolean.FALSE;
                    } else {
                        sb.append(" OR");
                    }
                    if (mdt == MetaDataType.MOVIE) {
                        sb.append(" videodata_id IS NOT NULL");
                    } else if (mdt == MetaDataType.SERIES) {
                        sb.append(" series_id IS NOT NULL");
                    } else if (mdt == MetaDataType.SEASON) {
                        sb.append(" season_id IS NOT NULL");
                    }
                }
                sb.append(")");
                sqlScalars.addToSql(sb.toString());
            }
        }

        // Add the scalars
        sqlScalars.addScalar("artworkId", LongType.INSTANCE);
        sqlScalars.addScalar("locatedId", LongType.INSTANCE);
        sqlScalars.addScalar("generatedId", LongType.INSTANCE);
        sqlScalars.addScalar("seasonId", LongType.INSTANCE);
        sqlScalars.addScalar("seriesId", LongType.INSTANCE);
        sqlScalars.addScalar("videodataId", LongType.INSTANCE);
        sqlScalars.addScalar("artworkTypeString", StringType.INSTANCE);
        sqlScalars.addScalar("cacheDir", StringType.INSTANCE);
        sqlScalars.addScalar("cacheFilename", StringType.INSTANCE);

        return sqlScalars;
    }
    //</editor-fold>
}
