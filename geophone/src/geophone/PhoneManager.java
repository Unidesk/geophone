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
import java.util.Locale;
import main.Main;
import org.json.simple.JSONObject;

/**
 *
 * @author Chris
 */
public class PhoneManager {

	/**
	 * lookup phone information and return JSON object with results
	 * 
	 * The resulting json object will contain:
	 * originalPhone: Original phone number
	 * valid: boolean true/false if valid number
	 * errorReason: if valid is false, this contains the reason
	 * reformattedNumber: Phone number in new format (if valid true)
	 * country: Country code for the phone number (if valid true)
	 * stateProvince: state/province for the number (if valid true)
	 * 
	 * @param phoneNumber Phone number to look up
	 * @return JSONObject with formatted json results
	 */
	public static JSONObject json(String phoneNumber) {
		JSONObject json = new JSONObject();
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

		try {
			Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, "US");

			json.put("originalPhone", phoneNumber);

			if (phoneUtil.isValidNumber(numberProto)) {
				json.put("valid", true);

				String reformattedNumber;
				if (phoneUtil.isValidNumberForRegion(numberProto, "US"))
					reformattedNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
				else
					reformattedNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
				json.put("reformattedNumber", reformattedNumber);
				PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
				String stateProvince = geocoder.getDescriptionForNumber(numberProto, Locale.ENGLISH);
				String country = phoneUtil.getRegionCodeForNumber(numberProto);
				json.put("stateProvince", stateProvince);
				json.put("country", country);
				System.out.println("   RESULT: " + reformattedNumber + " [" + stateProvince + "/" + country + "]");
			} else {
				System.err.println("   ERROR: Invalid phone number");
				json.put("valid", false);
				json.put("errorReason", "INVALID_PHONE_NUMBER");
			}
		} catch (NumberParseException e) {
			json.put("valid", false);
			json.put("errorReason", "   ERROR: " + e.toString());	
			System.err.println("NumberParseException was thrown: " + e.toString());
		}
		
		return json;
	}
}
