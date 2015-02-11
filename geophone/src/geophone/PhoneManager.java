/*
 * Copyright (C) 2015 Chris
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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import java.util.ArrayList;
import java.util.Locale;
import main.Main;
import org.json.simple.JSONObject;

/**
 *
 * @author Chris
 */
public class PhoneManager {

	/**
	 * lookup phone information for a primary or alternate number and return JSON object with results
	 * 
	 * The resulting json object will contain:
	 * valid: boolean true/false if valid number
	 * errorReason: if valid is false, this contains the reason.
	 * reformattedNumber: Phone number in new format (if valid true)
	 * country: Country code for the phone number (if valid true)
	 * stateProvince: state/province for the number (if valid true)
	 * city: city name (if valid true, and if available, may be empty string)
	 * 
	 * @param phoneNumbers Phone number(s) to look up, in order (first successful one is returned)
	 * @return JSONObject with formatted json results
	 */
	public static JSONObject json(ArrayList<String> phoneNumbers) {
		// make sure we have at least one number
		if (phoneNumbers.size() == 0) {
			JSONObject json = new JSONObject();
			json.put("valid", false);
			json.put("errorReason", "   ERROR: No numbers provided");	
			return json;
		}
		// process all numbers
		for (String phoneNumber : phoneNumbers) {
			// start with primary number
			JSONObject json = json(phoneNumber);
			if ((boolean) json.get("valid")) {
				json.put("originalNumber", phoneNumber);
				return json;
			}

			// again with "+"
			json = json("+" + phoneNumber);
			if ((boolean) json.get("valid")) {
				json.put("originalNumber", phoneNumber);
				return json;
			}
		}
		
		// give up - return original error reason
		return json(phoneNumbers.get(0));
	}
	
	/**
	 * internal lookup phone information and return JSON object with results
	 * 
	 * @param phoneNumber Phone number to look up
	 * @return JSONObject with formatted json results
	 */
	private static JSONObject json(String phoneNumber) {
		JSONObject json = new JSONObject();
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

		try {
			Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, "US");

			if (phoneUtil.isValidNumber(numberProto)) {
				json.put("valid", true);
				
				String reformattedNumber;
				if (phoneUtil.isValidNumberForRegion(numberProto, "US"))
					reformattedNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
				else
					reformattedNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
				json.put("reformattedNumber", reformattedNumber);
				PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
				String stateProvinceCity = geocoder.getDescriptionForNumber(numberProto, Locale.ENGLISH);
				String country = phoneUtil.getRegionCodeForNumber(numberProto);
				
				// stateProvinceCity may be "city, state".  If so, parse it out
				String[] parts = stateProvinceCity.split(",", 2);
				String stateProvince;
				String city;
				if (parts.length == 1) {
					stateProvince = parts[0].trim();
					city = "";
				} else {
					stateProvince = parts[1].trim();
					city = parts[0].trim();
				}
				
				// now see if we can clean up the state name
				String stateAbbreviation = StateMap.getInstance().findAbbreviation(stateProvince);
				if (stateAbbreviation != null)
					stateProvince = stateAbbreviation;
				
				// json encode them
				json.put("city", city);
				json.put("stateProvince", stateProvince);
				json.put("country", country);
				System.out.println("   RESULT: " + reformattedNumber + " [" + city + "/" + stateProvince + "/" + country + "]");
			} else {
				System.err.println("   ERROR: Invalid phone number");
				json.put("valid", false);
				json.put("errorReason", "INVALID_PHONE_NUMBER");
			}
		} catch (NumberParseException e) {
			json.put("valid", false);
			json.put("errorReason", "ERROR: " + e.toString());	
			System.err.println("   NumberParseException was thrown: " + e.toString());
		}
		
		return json;
	}
}
