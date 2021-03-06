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
package org.yamj.core.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.MapKeyType;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.yamj.core.database.model.type.OverrideFlag;

@Entity
@Table(name = "series",
        uniqueConstraints
        = @UniqueConstraint(name = "UIX_SERIES_NATURALID", columnNames = {"identifier"}))
@org.hibernate.annotations.Table(appliesTo = "series",
        indexes = {
            @Index(name = "IX_SERIES_TITLE", columnNames = {"title"}),
            @Index(name = "IX_SERIES_STATUS", columnNames = {"status"})
        })
public class Series extends AbstractMetadata implements IDataGenres {

    private static final long serialVersionUID = 1L;
    @Column(name = "start_year")
    private int startYear = -1;
    @Column(name = "end_year")
    private int endYear = -1;
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "series_ids", joinColumns
            = @JoinColumn(name = "series_id"))
    @ForeignKey(name = "FK_SERIES_SOURCEIDS")
    @Fetch(value = FetchMode.SELECT)
    @MapKeyColumn(name = "sourcedb", length = 40)
    @Column(name = "sourcedb_id", length = 200, nullable = false)
    private Map<String, String> sourceDbIdMap = new HashMap<String, String>(0);
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "series_ratings", joinColumns
            = @JoinColumn(name = "series_id"))
    @ForeignKey(name = "FK_SERIES_RATINGS")
    @Fetch(value = FetchMode.SELECT)
    @MapKeyColumn(name = "sourcedb", length = 40)
    @Column(name = "rating", length = 30, nullable = false)
    private Map<String, Integer> ratings = new HashMap<String, Integer>(0);
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "series_override", joinColumns
            = @JoinColumn(name = "series_id"))
    @ForeignKey(name = "FK_SERIES_OVERRIDE")
    @Fetch(value = FetchMode.SELECT)
    @MapKeyColumn(name = "flag", length = 30)
    @MapKeyType(value
            = @Type(type = "overrideFlag"))
    @Column(name = "source", length = 30, nullable = false)
    private Map<OverrideFlag, String> overrideFlags = new EnumMap<OverrideFlag, String>(OverrideFlag.class);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "series")
    private Set<Season> seasons = new HashSet<Season>(0);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "series")
    private List<Artwork> artworks = new ArrayList<Artwork>(0);
    @ManyToMany
    @ForeignKey(name = "FK_SERIESGENRES_SERIES", inverseName = "FK_SERIESGENRES_GENRE")
    @JoinTable(name = "series_genres",
            joinColumns = {
                @JoinColumn(name = "series_id")},
            inverseJoinColumns = {
                @JoinColumn(name = "genre_id")})
    private Set<Genre> genres = new HashSet<Genre>(0);
    @Transient
    private Set<String> genreNames = new LinkedHashSet<String>(0);

    // GETTER and SETTER
    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public Map<String, String> getSourceDbIdMap() {
        return sourceDbIdMap;
    }

    @Override
    public String getSourceDbId(String sourceDb) {
        return sourceDbIdMap.get(sourceDb);
    }

    public void setSourceDbIdMap(Map<String, String> sourceDbIdMap) {
        this.sourceDbIdMap = sourceDbIdMap;
    }

    @Override
    public void setSourceDbId(String sourceDb, String id) {
        if (StringUtils.isNotBlank(id)) {
            sourceDbIdMap.put(sourceDb, id);
        }
    }

    public Map<String, Integer> getRatings() {
        return ratings;
    }

    public void setRatings(Map<String, Integer> ratings) {
        this.ratings = ratings;
    }

    public void addRating(String source, Integer rating) {
        this.ratings.put(source, rating);
    }

    @JsonIgnore // This is not needed for the API
    public Map<OverrideFlag, String> getOverrideFlags() {
        return overrideFlags;
    }

    public void setOverrideFlags(Map<OverrideFlag, String> overrideFlags) {
        this.overrideFlags = overrideFlags;
    }

    @Override
    public void setOverrideFlag(OverrideFlag overrideFlag, String source) {
        this.overrideFlags.put(overrideFlag, source);
    }

    @JsonIgnore // This is not needed for the API
    @Override
    public String getOverrideSource(OverrideFlag overrideFlag) {
        return overrideFlags.get(overrideFlag);
    }

    public Set<Season> getSeasons() {
        return seasons;
    }

    public void setSeasons(Set<Season> seasons) {
        this.seasons = seasons;
    }

    public List<Artwork> getArtworks() {
        return artworks;
    }

    public void setArtworks(List<Artwork> artworks) {
        this.artworks = artworks;
    }

    /**
     * Get the genres
     *
     * @return
     */
    @Override
    public Set<Genre> getGenres() {
        return genres;
    }

    /**
     * Set the genres
     *
     * @param genres
     */
    @Override
    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    /**
     * Get the string representation of the genres
     *
     * Usually populated from the source site
     *
     * @return
     */
    @Override
    public Set<String> getGenreNames() {
        return genreNames;
    }

    /**
     * Set the string representation of the genres
     *
     * Usually populated from the source site
     *
     * @param genreNames
     * @param source
     */
    @Override
    public void setGenreNames(Set<String> genreNames, String source) {
        if (CollectionUtils.isNotEmpty(genreNames)) {
            this.genreNames = genreNames;
            setOverrideFlag(OverrideFlag.GENRES, source);
        }
    }

    // EQUALITY CHECKS
    @Override
    public int hashCode() {
        final int prime = 7;
        int result = 1;
        result = prime * result + (getIdentifier() == null ? 0 : getIdentifier().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof Series)) {
            return false;
        }
        Series castOther = (Series) other;
        return StringUtils.equals(getIdentifier(), castOther.getIdentifier());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Series [ID=");
        sb.append(getId());
        sb.append(", identifier=");
        sb.append(getIdentifier());
        sb.append(", title=");
        sb.append(getTitle());
        sb.append(", title=");
        sb.append(getYear());
        sb.append("]");
        return sb.toString();
    }
}
