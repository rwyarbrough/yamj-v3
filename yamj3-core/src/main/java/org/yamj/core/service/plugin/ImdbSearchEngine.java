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
package org.yamj.core.service.plugin;

import org.yamj.common.tools.PropertyTools;
import org.yamj.core.tools.web.HTMLTools;
import org.yamj.core.tools.web.PoolingHttpClient;
import org.yamj.core.tools.web.SearchEngineTools;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("imdbSearchEngine")
public class ImdbSearchEngine implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(ImdbSearchEngine.class);
    private static final String DEFAULT_SITE = "us";
    private static final String OBJECT_MOVIE = "movie";
    private static final String OBJECT_PERSON = "person";
    private static final String CATEGORY_MOVIE = "movie";
    private static final String CATEGORY_TV = "tv";
    private static final String CATEGORY_ALL = "all";
    private static final String SEARCH_FIRST = "first";
    private static final String SEARCH_EXACT = "exact";
    private static final Map<String, ImdbSiteDataDefinition> MATCHES_DATA_PER_SITE = new HashMap<String, ImdbSiteDataDefinition>();
    @Autowired
    private PoolingHttpClient httpClient;
    private String searchMatch;
    private boolean searchVariable;
    private ImdbSiteDataDefinition imdbSiteDef;
    private SearchEngineTools searchEngineTools;
    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final String QUOTES = "Quotes";
    private static final String TAGLINES = "Taglines";
    private static final String HTML_SLASH_QUOTE = "/\"";
    private static final String ASPECT_RATIO = "Aspect Ratio";
    private static final String ORIGINAL_TITLE = "original title";
    private static final String RATED = "Rated";

    static {
        MATCHES_DATA_PER_SITE.put("us", new ImdbSiteDataDefinition("http://www.imdb.com/", ISO_8859_1, "Director|Directed by", "Cast", "Release Date", "Runtime", ASPECT_RATIO, "Country",
                "Company", "Genre", QUOTES, "Plot", RATED, "Certification", "Original Air Date", "Writer|Writing credits", "Tagline", ORIGINAL_TITLE));

        MATCHES_DATA_PER_SITE.put("fr", new ImdbSiteDataDefinition("http://www.imdb.fr/", ISO_8859_1, "R&#xE9;alisateur|R&#xE9;alis&#xE9; par", "Ensemble", "Date de sortie", "Dur&#xE9;e", ASPECT_RATIO, "Pays",
                "Soci&#xE9;t&#xE9;", "Genre", "Citation", "Intrigue", RATED, "Classification", "Date de sortie", "Sc&#xE9;naristes|Sc&#xE9;naristes", TAGLINES, ORIGINAL_TITLE));

        MATCHES_DATA_PER_SITE.put("es", new ImdbSiteDataDefinition("http://www.imdb.es/", ISO_8859_1, "Director|Dirigida por", "Reparto", "Fecha de Estreno", "Duraci&#xF3;n", "Relaci&#xF3;n de Aspecto", "Pa&#xED;s",
                "Compa&#xF1;&#xED;a", "G&#xE9;nero", QUOTES, "Trama", RATED, "Clasificaci&#xF3;n", "Fecha de Estreno", "Escritores|Cr&#xE9;ditos del gui&#xF3;n", TAGLINES, ORIGINAL_TITLE));

        MATCHES_DATA_PER_SITE.put("de", new ImdbSiteDataDefinition("http://www.imdb.de/", ISO_8859_1, "Regisseur|Regie", "Besetzung", "Premierendatum", "L&#xE4;nge", "Seitenverh&#xE4;ltnis", "Land",
                "Firma", "Genre", "Nutzerkommentare", "Handlung", RATED, "Altersfreigabe", "Premierendatum", "Guionista|Buch", TAGLINES, "Originaltitel"));

        MATCHES_DATA_PER_SITE.put("it", new ImdbSiteDataDefinition("http://www.imdb.it/", ISO_8859_1, "Regista|Registi|Regia di", "Cast", "Data di uscita", "Durata", ASPECT_RATIO,
                "Nazionalit&#xE0;", "Compagnia", "Genere", QUOTES, "Trama", RATED, "Divieti", "Data di uscita", "Sceneggiatore|Scritto da", TAGLINES, ORIGINAL_TITLE));

        MATCHES_DATA_PER_SITE.put("pt", new ImdbSiteDataDefinition("http://www.imdb.pt/", "UTF-8", "Diretor|Dirigido por", "Elenco", "Data de Lan&#xE7;amento", "Dura&#xE7;&#xE3;o", ASPECT_RATIO,
                "Pa&#xED;s", "Companhia", "G&#xEA;nero", QUOTES, "Argumento", RATED, "Certifica&#xE7;&#xE3;o", "Data de Lan&#xE7;amento",
                "Roteirista|Cr&#xE9;ditos como roteirista", TAGLINES, ORIGINAL_TITLE));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        searchMatch = PropertyTools.getProperty("imdb.id.search.match", "regular");
        searchVariable = PropertyTools.getBooleanProperty("imdb.id.search.variable", Boolean.TRUE);

        String site = PropertyTools.getProperty("imdb.site", DEFAULT_SITE);
        imdbSiteDef = MATCHES_DATA_PER_SITE.get(site);
        if (imdbSiteDef == null) {
            LOG.warn("No site definition for '{}' using the default instead '{}'", site, DEFAULT_SITE);
            site = DEFAULT_SITE;
            imdbSiteDef = MATCHES_DATA_PER_SITE.get(site);
        }

        searchEngineTools = new SearchEngineTools(httpClient, site);
    }

    /**
     * Retrieve the IMDb matching the specified movie name and year. This routine is based on a IMDb request.
     *
     * @param title
     * @param year
     * @param isTVShow flag to indicate if the searched movie is a TV show
     * @return the IMDb id
     */
    public String getImdbId(String title, int year, boolean isTVShow) {
        return getImdbId(title, year, (isTVShow ? CATEGORY_TV : CATEGORY_MOVIE));
    }

    /**
     * Retrieve the IMDb matching the specified movie name and year. This routine is based on a IMDb request.
     *
     * @param title
     * @param year
     * @param categoryType the type of the category to search within
     * @return the IMDb id
     */
    private String getImdbId(String title, int year, String categoryType) {
        String imdbId = getImdbIdFromImdb(title, year, OBJECT_MOVIE, categoryType);
        if (StringUtils.isBlank(imdbId)) {
            // try with search engines
            String imdbUrl;
            if (CATEGORY_TV.equals(categoryType)) {
                // leave out the year
                imdbUrl = searchEngineTools.searchURL(title, -1, "www.imdb.com/title");
            } else {
                imdbUrl = searchEngineTools.searchURL(title, year, "www.imdb.com/title");
            }
            imdbId = getImdbIdFromURL(imdbUrl, categoryType);
        }
        return imdbId;
    }

    /**
     * @param personName
     * @param movieId
     * @return
     */
    public String getImdbPersonId(String personName, String movieId) {
        try {
            if (StringUtils.isNotBlank(movieId)) {
                StringBuilder sb = new StringBuilder(imdbSiteDef.getSite());
                sb.append("search/name?name=");
                sb.append(URLEncoder.encode(personName, imdbSiteDef.getCharset().displayName())).append("&role=").append(movieId);

                LOG.debug("Querying IMDB for '{}'", sb.toString());
                String xml = httpClient.requestContent(sb.toString(), imdbSiteDef.getCharset());

                // Check if this is an exact match (we got a person page instead of a results list)
                Matcher titlematch = imdbSiteDef.getPersonRegex().matcher(xml);
                if (titlematch.find()) {
                    LOG.debug("IMDb returned one match '{}'", titlematch.group(1));
                    return titlematch.group(1);
                }

                String firstPersonId = HTMLTools.extractTag(HTMLTools.extractTag(xml, "<tr class=\"even detailed\">", "</tr>"), "<a href=\"/name/", HTML_SLASH_QUOTE);
                if (StringUtils.isNotBlank(firstPersonId)) {
                    return firstPersonId;
                }
            }

            return getImdbPersonId(personName);
        } catch (IOException error) {
            LOG.error("Failed retreiving IMDb Id for person '{}'", personName, error);
            return null;
        }
    }

    /**
     * Get the IMDb ID for a person
     *
     * @param personName
     * @return
     */
    public String getImdbPersonId(String personName) {
        String imdbId = getImdbIdFromImdb(personName.toLowerCase(), -1, OBJECT_PERSON, CATEGORY_ALL);
        if (StringUtils.isBlank(imdbId)) {
            String imdbUrl = searchEngineTools.searchURL(personName, -1, "www.imdb.com/name");
            imdbId = getImdbIdFromURL(imdbUrl, OBJECT_PERSON);
        }
        return imdbId;
    }

    private String getImdbIdFromURL(String url, String objectType) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        String imdbId = StringUtils.EMPTY;
        int beginIndex = url.indexOf(objectType.equals(OBJECT_MOVIE) ? "/title/tt" : "/name/nm");
        if (beginIndex > -1) {
            int index;
            if (objectType.equals(OBJECT_MOVIE)) {
                index = beginIndex + 7;
            } else {
                index = beginIndex + 6;
            }
            StringTokenizer st = new StringTokenizer(url.substring(index), HTML_SLASH_QUOTE);
            imdbId = st.nextToken();
        }

        if (imdbId.startsWith(objectType.equals(OBJECT_MOVIE) ? "tt" : "nm")) {
            LOG.debug("Found IMDb ID '{}'", imdbId);
            return imdbId;
        }
        return null;
    }

    /**
     * Retrieve the IMDb matching the specified movie name and year. This routine is base on a IMDb request.
     */
    private String getImdbIdFromImdb(String title, int year, String objectType, String categoryType) {
        StringBuilder sb = new StringBuilder(imdbSiteDef.getSite());
        sb.append("find?q=");
        try {
            sb.append(URLEncoder.encode(title, imdbSiteDef.getCharset().displayName()));
        } catch (UnsupportedEncodingException ex) {
            LOG.debug("Failed to encode title '{}'", title);
            sb.append(title);
        }

        if (year > 0) {
            sb.append("+%28").append(year).append("%29");
        }
        sb.append("&s=");
        if (objectType.equals(OBJECT_MOVIE)) {
            sb.append("tt");
            if (searchVariable) {
                if (categoryType.equals(CATEGORY_MOVIE)) {
                    sb.append("&ttype=ft");
                } else if (categoryType.equals(CATEGORY_TV)) {
                    sb.append("&ttype=tv");
                }
            }
        } else {
            sb.append("nm");
        }
        sb.append("&site=aka");

        LOG.debug("Querying IMDb for '{}'", sb.toString());
        String xml;
        try {
            xml = httpClient.requestContent(sb.toString(), imdbSiteDef.getCharset());
        } catch (IOException ex) {
            LOG.error("Failed retreiving IMDb Id for '{}'", title, ex);
            return null;
        }

        // Check if this is an exact match (we got a movie page instead of a results list)
        Pattern titleregex = imdbSiteDef.getPersonRegex();
        if (objectType.equals(OBJECT_MOVIE)) {
            titleregex = imdbSiteDef.getTitleRegex();
        }

        Matcher titlematch = titleregex.matcher(xml);
        if (titlematch.find()) {
            LOG.debug("IMDb returned one match '{}'", titlematch.group(1));
            return titlematch.group(1);
        }

        String searchName = HTMLTools.extractTag(HTMLTools.extractTag(xml, ";ttype=ep\">", "\"</a>.</li>"), "<b>", "</b>").toLowerCase();
        final String formattedName;
        final String formattedYear;
        final String formattedExact;

        if (SEARCH_FIRST.equalsIgnoreCase(searchMatch)) {
            // first match so nothing more to check
            formattedName = null;
            formattedYear = null;
            formattedExact = null;
        } else if (StringUtils.isNotBlank(searchName)) {
            if (year > 0 && searchName.endsWith(")") && searchName.contains("(")) {
                searchName = searchName.substring(0, searchName.lastIndexOf('(') - 1);
                formattedName = searchName.toLowerCase();
                formattedYear = "(" + year + ")";
                formattedExact = formattedName + "</a> " + formattedYear;
            } else {
                formattedName = searchName.toLowerCase();
                formattedYear = "</a>";
                formattedExact = formattedName + formattedYear;
            }
        } else {
            sb = new StringBuilder();
            try {
                sb.append(URLEncoder.encode(title, imdbSiteDef.getCharset().displayName()).replace("+", " "));
            } catch (UnsupportedEncodingException ex) {
                LOG.debug("Failed to encode title '{}'", title);
                sb.append(title);
            }
            formattedName = sb.toString().toLowerCase();
            if (year > 0) {
                formattedYear = "(" + year + ")";
                formattedExact = formattedName + "</a> " + formattedYear;
            } else {
                formattedYear = "</a>";
                formattedExact = formattedName + formattedYear;

            }
            searchName = formattedExact;
        }

        for (String searchResult : HTMLTools.extractTags(xml, "<table class=\"findList\">", "</table>", "<td class=\"result_text\">", "</td>", false)) {
            // LOGGER.debug(LOG_MESSAGE + "Check  : '" + searchResult + "'");
            boolean foundMatch = false;
            if (SEARCH_FIRST.equalsIgnoreCase(searchMatch)) {
                // first result matches
                foundMatch = true;
            } else if (SEARCH_EXACT.equalsIgnoreCase(searchMatch)) {
                // exact match
                foundMatch = (searchResult.toLowerCase().indexOf(formattedExact) != -1);
            } else {
                // regular match: name and year match independent from each other
                int nameIndex = searchResult.toLowerCase().indexOf(formattedName);
                if (nameIndex != -1) {
                    foundMatch = (searchResult.indexOf(formattedYear) > nameIndex);
                }
            }

            if (foundMatch) {
                // LOGGER.debug(LOG_MESSAGE + "Title match  : '" + searchResult + "'");
                return HTMLTools.extractTag(searchResult, "<a href=\"" + (objectType.equals(OBJECT_MOVIE) ? "/title/" : "/name/"), "/");
            } else {
                for (String otherResult : HTMLTools.extractTags(searchResult, "</';\">", "</p>", "<p class=\"find-aka\">", "</em>", false)) {
                    if (otherResult.toLowerCase().indexOf("\"" + searchName + "\"") != -1) {
                        // LOGGER.debug(LOG_MESSAGE + "Other title match: '" + otherResult + "'");
                        return HTMLTools.extractTag(searchResult, "/images/b.gif?link=" + (objectType.equals(OBJECT_MOVIE) ? "/title/" : "/name/"), "/';\">");
                    }
                }
            }
        }

        // alternate search for person ID
        if (objectType.equals(OBJECT_PERSON)) {
            String firstPersonId = HTMLTools.extractTag(HTMLTools.extractTag(xml, "<table><tr> <td valign=\"top\">", "</td></tr></table>"), "<a href=\"/name/", HTML_SLASH_QUOTE);
            if (StringUtils.isBlank(firstPersonId)) {
                // alternate approach
                int beginIndex = xml.indexOf("<a href=\"/name/nm");
                if (beginIndex > -1) {
                    StringTokenizer st = new StringTokenizer(xml.substring(beginIndex + 15), HTML_SLASH_QUOTE);
                    firstPersonId = st.nextToken();
                }
            }

            if (firstPersonId.startsWith("nm")) {
                LOG.debug("Found IMDb ID '{}'", firstPersonId);
                return firstPersonId;
            }
        }

        LOG.debug("Failed to find a match on IMDb");
        return null;
    }
}
