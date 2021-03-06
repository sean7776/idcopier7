/**
 * IDCopier
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.tools.idcopier.model;

import java.io.Serializable;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.idcopier.constants.IDCConfigurationConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * XSTream mapping of the server bean node in the server config file
 * 
 * @author Ari Kamen
 * @date Sep 22, 2014
 * 
 */
@XStreamAlias(IDCConfigurationConstants.SERVER_CONFIG_SERVER_NODE)
public class IDCServer implements Serializable {
	static Logger log = Logger.getLogger(IDCServer.class);

	private static final long serialVersionUID = 1L;

	@XStreamAlias(IDCConfigurationConstants.SERVER_CONFIG_SERVER_URI)
	private String serverURI;
	@XStreamAlias(IDCConfigurationConstants.SERVER_CONFIG_SERVER_USERNAME)
	private String userName;
	@XStreamAlias(IDCConfigurationConstants.SERVER_CONFIG_SERVER_PASSWORD)
	private String password;

	/**
	 * This is the name that will displayed on the UI
	 */
	private String serverName = null;
	// Once established, this is flipped to true
	private Boolean loggedIn = false;
	// In case an error happens, we capture it here.
	private String loginError = null;

	public IDCServer() {

	}

	/**
	 * Creates a plain server bean
	 * 
	 * @param server
	 * @param user
	 * @param password
	 * @throws Exception
	 */
	public IDCServer(String server, String user, String password) throws Exception {
		setServerURI(server);
		setUserName(user);
		setPassword(password);
	}

	public IDCServer(String server) {
		serverName = server;
	}

	public String getServerURI() {
		return serverURI;
	}

	public void setServerURI(String serverURI) throws Exception {
		this.serverURI = serverURI;
		// Validate host name
		getHostFromURI(serverURI);
	}

	/**
	 * Helper method to retrieve host name
	 * 
	 * @param serverURI
	 * @return
	 * @throws Exception
	 */
	public static String getHostFromURI(String serverURI) throws Exception {
		String hostName = null;
		try {
			URIBuilder builder = new URIBuilder(serverURI);
			hostName = builder.getHost();
			if (hostName == null)
				throw new IllegalArgumentException("Unable to determine host name from URI: " + serverURI);
		} catch (Exception e) {
			throw new IllegalArgumentException("Trouble parsing server URI: " + e.getMessage());
		}

		return hostName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Returns the hostName by parsing the URI
	 * 
	 * @return
	 */
	public String getServerName() {
		// Because we want to just use the host name, parse this URI
		try {
			if (serverName != null)
				return serverName;

			URIBuilder builder = new URIBuilder(serverURI);
			serverName = builder.getHost();
		} catch (Exception e) {
			log.warn("Trouble parsing server URI: " + e.getMessage());
		}
		return serverName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("Server URL: " + getServerURI());
		sb.append("\n");
		sb.append("User Name: " + getUserName());
		sb.append("\n");
		sb.append("Server Name: " + getServerName());
		sb.append("\n");

		return sb.toString();
	}

	public Boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(Boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getLoginError() {
		return loginError;
	}

	public void setLoginError(String loginError) {
		this.loginError = loginError;
	}
}
