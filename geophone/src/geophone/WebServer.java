/*
 This file is part of geophone.

 theunibot is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 theunibot is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with theunibot.  If not, see <http://www.gnu.org/licenses/>.

 Copyright (c) 2014 Unidesk Corporation
 */
package geophone;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.nanohttpd.NanoHTTPD;
import static server.nanohttpd.NanoHTTPD.MIME_HTML;
import static server.nanohttpd.NanoHTTPD.MIME_PLAINTEXT;

/**
 *
 */
public class WebServer extends NanoHTTPD {

	private final String ROOT_DIR = "./webRootFolder";

	private final String API_SUBDIR = "/geophone";

	private Map<String, String> mimeTypes = new LinkedHashMap<String, String>();

	private final String[] MAIN
			= {
				"/", ""
			};

	private final String XSS_KEY = "Access-Control-Allow-Origin";
	private final String XSS_VALUE = "*";

	//serve pages
	private final String PAGE_TO_SERVE = ROOT_DIR + "/geophone.html";

	private String mimeType = MIME_PLAINTEXT;
	
	public WebServer() {
		super(Settings.port());
		mimeTypes.put("js", "application/javascript");
		mimeTypes.put("html", MIME_HTML);
		mimeTypes.put("htm", MIME_HTML);
		mimeTypes.put("json", "application/json");
	}

	@Override
	public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
		Response response = new Response("");
		response.addHeader(XSS_KEY, XSS_VALUE);

		NanoHTTPD.Method method = session.getMethod();
		String responseStr = "";
		switch (method) {
			case GET:
			case POST:
				Map<String, String> params = session.getParms();
				String uri = session.getUri();

				//=== PARSE URI===
				if (main.Main.DEBUG) {
					System.out.println("Unparsed: " + method + " '" + uri + "' ");
				}
				if (uri.endsWith("/"))//remove the last slash on a uri
				{
					uri = uri.substring(0, uri.length() - 1);
				}
				if (uri.startsWith(API_SUBDIR + "/")) {
					uri = uri.replaceFirst(API_SUBDIR, "");
				}
				if (main.Main.DEBUG) {
					System.out.println("Parsed:   " + method + " '" + uri + "' ");
				}
				//===END PARSE URI===

				if (uri.equals(MAIN[0]) || uri.equals(MAIN[1])) {
					try {
//                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.ACCEPTED, MIME_HTML, new FileInputStream(PAGE_TO_SERVE));//
						response.setData(new FileInputStream(PAGE_TO_SERVE));
						response.setMimeType(MIME_HTML);
						response.setStatus(Response.Status.OK);
						return response;
					} catch (FileNotFoundException ex) {
						Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
						String fail = "{main file not found}";
						response.setData(new ByteArrayInputStream(fail.getBytes()));
						response.setMimeType(MIME_HTML);
						response.setStatus(Response.Status.OK);
						return response;
//                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, MIME_HTML, "Failed to load page.");
					}
				} else if (uri.startsWith("/lookup")) {
					if (Settings.validateKey(params.get("key"))) {
						String owner = Settings.keyOwner(params.get("key"));
						String number = params.get("number");
						System.out.println("Looking up " + number + " for key '" + owner + "'");
						JSONObject json = PhoneManager.json(number);
						responseStr = json.toJSONString();
					} else {
						JSONObject json = new JSONObject();
						json.put("valid", false);
						json.put("errorReason", "INVALID_KEY");
						System.err.println("REJECT: Invalid key " + params.get("key") + " on number " + params.get("number"));
						responseStr = json.toJSONString();
					}
				} else if (uri.startsWith("/bulk")) {
					if (!Settings.validateKey(params.get("key"))) {
						JSONObject json = new JSONObject();
						json.put("valid", false);
						json.put("errorReason", "INVALID_KEY");
						System.err.println("REJECT: Invalid key " + params.get("key") + " on number " + params.get("number"));
						responseStr = json.toJSONString();
					} else {
						String owner = Settings.keyOwner(params.get("key"));
						responseStr = null;
						
						// batch request.  We support both GET and POST on this one.  GET takes
						// parameters over query string and POST takes them from POST data in JSON format
						ArrayList<String> numbers = new ArrayList<>();
						String jsonNumbers = null;
						if (method == NanoHTTPD.Method.POST) {
							// post
							Map<String, String> postKeys = new HashMap<String, String>();
							try {
								session.parseBody(postKeys);
								/*
								System.out.println("Got " + postKeys.size() + " keys");
								for (String x : postKeys.keySet())
									System.out.println("Key is " + x);
								*/
								jsonNumbers = postKeys.get("postData");
							} catch (IOException | ResponseException ex) {
								JSONObject json = new JSONObject();
								json.put("valid", false);
								json.put("errorReason", "INVALID_POST: " + ex.getMessage());
								System.err.println("Invalid post: " + ex.getMessage());
								responseStr = json.toJSONString();
							}
						} else {
							// get - can be passsed via json (like POST) or via n1, n2, n3...							
							if (params.get("json") != null) {
								jsonNumbers = params.get("json");
							} else {
								for (int index = 1; true; ++index) {
									String number = params.get("n" + index);
									if ((number == null) || (number.isEmpty()))
										break;
									numbers.add(number);
								}
							}
						}
						
						// if no response (ie, no error) continue with the processing
						if (responseStr == null) {
							// do we have jsonNumbers to decode?
							if (jsonNumbers != null) {
								System.out.println("JSON query: " + jsonNumbers);
								try {
									JSONArray jsonParse = (JSONArray) JSONValue.parse(jsonNumbers);
									for (Object number : jsonParse)
										numbers.add((String) number);
								} catch (Exception e) {
									JSONObject json = new JSONObject();
									json.put("valid", false);
									json.put("errorReason", "INVALID_JSON: " + e.getMessage());
									System.err.println("Invalid JSON: " + e.getMessage());
									responseStr = json.toJSONString();									
								}
							}
						}
						
						// and again, continue only if no errors
						if (responseStr == null) {
							// now process all numbers in the numbers list
							JSONArray container = new JSONArray();
							for (String number : numbers) {
								System.out.println("Looking up " + number + " for key '" + owner + "'");
								JSONObject json = PhoneManager.json(number);
								container.add(json);
							}
							responseStr = container.toJSONString();
						}
					}
				} else //standard file
				{
					System.out.println("URI: " + uri);
					String[] pieces = uri.split("\\.");
					String extension = null;

					if (pieces.length != 0) {
						extension = pieces[pieces.length - 1];
					}
					if (main.Main.DEBUG) {
						System.out.print("Extension: " + extension + " ");
					}

					if (extension != null && mimeTypes.get(extension) != null) {
						mimeType = mimeTypes.get(extension);
					} else {
						mimeType = MIME_PLAINTEXT;
					}
					System.out.println("MimeType: " + mimeType);

					try {
						response.setData(new FileInputStream(ROOT_DIR + uri));
						response.setMimeType(mimeType);
						response.setStatus(Response.Status.OK);
						return response;
					} catch (FileNotFoundException ex) {
						System.err.println("File not found: " + ROOT_DIR + uri);
						String fail = "{ \"valid\": false, \"errorReason\": \"File '" + ROOT_DIR + uri + "' not found\" }";
						response.setData(new ByteArrayInputStream(fail.getBytes()));
						response.setMimeType(mimeType);
						response.setStatus(Response.Status.OK);
						return response;
					}

				}
				//Real response
				mimeType = mimeTypes.get("json");
				response.setData(new ByteArrayInputStream(responseStr.getBytes()));
				response.setMimeType(mimeType);
				response.setStatus(Response.Status.OK);
				return response;
			default:
				System.err.println("Invalid request (not GET or POST)");
				mimeType = mimeTypes.get("json");
				String fail = "{ \"valid\": false, \"errorReason\": \"Invalid request (not GET or POST)\" }";
				response.setData(new ByteArrayInputStream(fail.getBytes()));
				response.setMimeType(mimeType);
				response.setStatus(Response.Status.OK);
				return response;
		}
	}

}
