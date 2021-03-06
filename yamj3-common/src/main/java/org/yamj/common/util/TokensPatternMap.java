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
package org.yamj.common.util;

import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public abstract class TokensPatternMap extends HashMap<String, Pattern> {

    private static final long serialVersionUID = 2239121205124537392L;

    /**
     * Generate pattern using tokens from given string.
     *
     * @param key Language id.
     * @param tokensStr Tokens list divided by comma or space.
     */
    public void put(String key, String tokensStr) {
        List<String> tokens = new ArrayList<String>();
        for (String token : tokensStr.split("[ ,]+")) {
            token = StringUtils.trimToNull(token);
            if (token != null) {
                tokens.add(token);
            }
        }
        put(key, tokens);
    }

    public void putAll(List<String> keywords, Map<String, String> keywordMap) {
        for (String keyword : keywords) {
            // Just pass the keyword if the map is null
            if (keywordMap.get(keyword) == null) {
                put(keyword, keyword);
            } else {
                put(keyword, keywordMap.get(keyword));
            }
        }
    }

    /**
     * Generate pattern using tokens from given string.
     *
     * @param key Language id.
     * @param tokens Tokens list.
     */
    protected abstract void put(String key, Collection<String> tokens);
}
