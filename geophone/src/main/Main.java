/*
 This file is part of theunibot.

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
package main;

import geophone.Settings;
import geophone.WebServer;
import server.nanohttpd.ServerRunner;

/**
 *
 */
public class Main {

    public static final boolean DEBUG = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // merge stdout and stderr so we have a clean output
        System.setErr(System.out);
		
		// initialize
		Settings.loadSettings();

        // start up the web server
        ServerRunner.run(WebServer.class);
    }
}
