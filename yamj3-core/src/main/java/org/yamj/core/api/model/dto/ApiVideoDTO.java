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
package org.yamj.core.api.model.dto;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.yamj.common.type.MetaDataType;
import org.yamj.core.database.model.type.ArtworkType;

/**
 *
 * @author stuart.boston
 */
public class ApiVideoDTO extends AbstractApiIdentifiableDTO {

    private MetaDataType videoType;
    private String title;
    private String originalTitle;
    private Integer videoYear;
    private List<ApiGenreDTO> genres = new ArrayList<ApiGenreDTO>();
    private final Map<ArtworkType, List<ApiArtworkDTO>> artwork = new EnumMap<ArtworkType, List<ApiArtworkDTO>>(ArtworkType.class);
    private String outline;
    private String plot;
    private String country;
    private String quote;
    private String tagline;
    private Long topRank;
    private Long seriesId = -1L;
    private Long seasonId = -1L;
    private Long season = -1L;
    private Long episode = -1L;

    //<editor-fold defaultstate="collapsed" desc="Getter Methods">
    public MetaDataType getVideoType() {
        return videoType;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public Integer getVideoYear() {
        return videoYear;
    }

    public Map<ArtworkType, List<ApiArtworkDTO>> getArtwork() {
        return artwork;
    }

    public int getArtworkCount() {
        int count = 0;
        for (Map.Entry<ArtworkType, List<ApiArtworkDTO>> entry : artwork.entrySet()) {
            count += entry.getValue().size();
        }
        return count;
    }

    public List<ApiGenreDTO> getGenres() {
        return genres;
    }

    public int getGenreCount() {
        return genres.size();
    }

    public String getOutline() {
        return outline;
    }

    public String getPlot() {
        return plot;
    }

    public String getCountry() {
        return country;
    }

    public String getQuote() {
        return quote;
    }

    public String getTagline() {
        return tagline;
    }

    public Long getTopRank() {
        return topRank;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public Long getSeasonId() {
        return seasonId;
    }

    public Long getSeason() {
        return season;
    }

    public Long getEpisode() {
        return episode;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Setter Methods">
    public void setVideoType(MetaDataType videoType) {
        this.videoType = videoType;
    }

    public void setVideoTypeString(String videoType) {
        this.videoType = MetaDataType.fromString(videoType);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public void setVideoYear(Integer videoYear) {
        this.videoYear = videoYear;
    }

    public void setFirstAired(String firstAired) {
        if (StringUtils.isNotBlank(firstAired) && firstAired.length() >= 4) {
            String year = firstAired.substring(0, 4);
            if (StringUtils.isNumeric(year)) {
                setVideoYear(Integer.parseInt(year));
            }
        }
    }

    public void setArtwork(List<ApiArtworkDTO> artworkList) {
        for (ApiArtworkDTO aadto : artworkList) {
            addArtwork(aadto);
        }
    }

    public void setGenres(List<ApiGenreDTO> genres) {
        this.genres = genres;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public void setTopRank(Long topRank) {
        this.topRank = topRank;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public void setSeasonId(Long seasonId) {
        this.seasonId = seasonId;
    }

    public void setSeason(Long season) {
        this.season = season;
    }

    public void setEpisode(Long episode) {
        this.episode = episode;
    }
    //</editor-fold>

    public void addArtwork(ApiArtworkDTO newArtwork) {
        // Add a blank list if it doesn't already exist
        if (!artwork.containsKey(newArtwork.getArtworkType())) {
            artwork.put(newArtwork.getArtworkType(), new ArrayList<ApiArtworkDTO>(1));
        }
        this.artwork.get(newArtwork.getArtworkType()).add(newArtwork);
    }
}
