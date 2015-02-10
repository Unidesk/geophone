/*
 * Copyright (C) 2015 Unidesk Corporation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package geophone;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class StateMap {
	Map<String, String> states = new HashMap<>();
	private static StateMap stateMap = null;

	private StateMap() {
		states.put("alabama", "AL");
		states.put("alaska", "AK");
		states.put("alberta", "AB");
		states.put("american samoa", "AS");
		states.put("arizona", "AZ");
		states.put("arkansas", "AR");
		states.put("british columbia", "BC");
		states.put("california", "CA");
		states.put("colorado", "CO");
		states.put("connecticut", "CT");
		states.put("delaware", "DE");
		states.put("district Of columbia", "DC");
		states.put("florida", "FL");
		states.put("georgia", "GA");
		states.put("guam", "GU");
		states.put("hawaii", "HI");
		states.put("idaho", "ID");
		states.put("illinois", "IL");
		states.put("indiana", "IN");
		states.put("iowa", "IA");
		states.put("kansas", "KS");
		states.put("kentucky", "KY");
		states.put("louisiana", "LA");
		states.put("maine", "ME");
		states.put("manitoba", "MB");
		states.put("maryland", "MD");
		states.put("massachusetts", "MA");
		states.put("michigan", "MI");
		states.put("minnesota", "MN");
		states.put("mississippi", "MS");
		states.put("missouri", "MO");
		states.put("montana", "MT");
		states.put("nebraska", "NE");
		states.put("nevada", "NV");
		states.put("new brunswick", "NB");
		states.put("new hampshire", "NH");
		states.put("new jersey", "NJ");
		states.put("new mexico", "NM");
		states.put("new york", "NY");
		states.put("newfoundland", "NF");
		states.put("north carolina", "NC");
		states.put("north dakota", "ND");
		states.put("northwest territories", "NT");
		states.put("nova scotia", "NS");
		states.put("nunavut", "NU");
		states.put("ohio", "OH");
		states.put("oklahoma", "OK");
		states.put("ontario", "ON");
		states.put("oregon", "OR");
		states.put("pennsylvania", "PA");
		states.put("prince edward island", "PE");
		states.put("puerto rico", "PR");
		states.put("quebec", "PQ");
		states.put("rhode island", "RI");
		states.put("saskatchewan", "SK");
		states.put("south carolina", "SC");
		states.put("south dakota", "SD");
		states.put("tennessee", "TN");
		states.put("texas", "TX");
		states.put("utah", "UT");
		states.put("vermont", "VT");
		states.put("virgin islands", "VI");
		states.put("virginia", "VA");
		states.put("washington", "WA");
		states.put("west virginia", "WV");
		states.put("wisconsin", "WI");
		states.put("wyoming", "WY");
		states.put("yukon territory", "YT");
	}
	
	public static StateMap getInstance() {
        if (stateMap == null)
            stateMap = new StateMap();
        return stateMap;
    }

	
	/**
	 * Locate a state abbreviation from a state name, case insensitive (exact match required)
	 * 
	 * @param state State to locate
	 * @return two letter abbreviation of the state, or null if not found
	 */
	String findAbbreviation(String state) {
		if (states.containsKey(state.toLowerCase()))
			return states.get(state.toLowerCase());
		return null;
	}
}
