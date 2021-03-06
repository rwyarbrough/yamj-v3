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
package org.yamj.common.tools;

import java.util.ArrayList;
import java.util.List;

public class StringTools {
    /**
     * Split a list using a regex and return a list of trimmed strings
     *
     * @param stringToSplit
     * @param regexDelim
     * @return
     */
    public static List<String> splitList(String stringToSplit, String regexDelim) {
        List<String> finalValues = new ArrayList<String>();

        for (String output : stringToSplit.split(regexDelim)) {
            finalValues.add(output.trim());
        }

        return finalValues;
    }

}
