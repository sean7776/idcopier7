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
package com.blackducksoftware.tools.idcopier.controller;

import java.util.List;

import com.blackducksoftware.tools.idcopier.constants.IDCViewModelConstants;
import com.blackducksoftware.tools.idcopier.model.UserServiceModel;
import com.blackducksoftware.tools.idcopier.service.LoginService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.ProjectInfo;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.BomProgressStatus;
import com.blackducksoftware.tools.idcopier.constants.IDCPathConstants;
import com.blackducksoftware.tools.idcopier.constants.IDCViewConstants;
import com.blackducksoftware.tools.idcopier.model.IDCServer;
import com.blackducksoftware.tools.idcopier.model.IDCSession;
import com.blackducksoftware.tools.idcopier.service.ProjectService;
import com.google.gson.Gson;


@RestController
@SessionAttributes(IDCViewModelConstants.IDC_SESSION)
public class IDCProjectController {
	static Logger log = Logger.getLogger(IDCProjectController.class);

	@Autowired
	private UserServiceModel userServiceModel;

	@RequestMapping(IDCPathConstants.GET_PROJECTS)
	public String getProjectJSONList(@RequestParam(value = IDCViewModelConstants.IDC_SERVER_NAME) String serverName) {
		log.info("Getting project list for server: " + serverName);
		List<ProjectInfo> projects = null;
		try {
			LoginService loginService = userServiceModel.getLoginService();
			ProjectService projectService = userServiceModel.getProjectService();
			IDCServer server = loginService.getServerByName(serverName);
			ProtexServerProxy proxy = loginService.getProxy(serverName);
			projects = projectService.getProjectsByServer(proxy, server);
		} catch (Exception e) {
			log.error("Error getting projects", e);
		}

		log.info("Returning JSON projects: " + new Gson().toJson(projects));

		return new Gson().toJson(projects);
	}

	@RequestMapping(value = IDCPathConstants.PROJECT_DISPLAY_TREE)
	public ModelAndView displayProjectTree(@RequestParam(value = IDCViewModelConstants.PROJECT_SOURCE_ID) String projectId, @RequestParam(value = IDCViewModelConstants.IDC_SERVER_NAME) String serverName,
			@ModelAttribute(IDCViewModelConstants.IDC_SESSION) IDCSession session, Model model) {
		ModelAndView modelAndView = new ModelAndView();

		log.debug("Processing project: " + projectId);

		try {
			LoginService loginService = userServiceModel.getLoginService();
			ProjectService projectService = userServiceModel.getProjectService();
			ProtexServerProxy proxy = loginService.getProxy(serverName);
			String jsonTree = projectService.getProjectJSON(proxy, projectId);

			modelAndView.addObject(IDCViewModelConstants.PROJECT_JSON_TREE, jsonTree);

			modelAndView.setViewName(IDCViewConstants.PROJECT_PAGE);
		} catch (Exception e) {
			log.error("Unable to display project tree", e);
		}

		return modelAndView;
	}

	/**
	 * Gets the children for a specific path
	 * 
	 * @param serverName
	 * @param projectId
	 * @param path
	 *            - The node in the tree that needs expansion
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(IDCPathConstants.TREE_EXPAND_NODE + "/{serverName}/{projectId}")
	public String expandPathNode(@PathVariable String serverName, @PathVariable String projectId, @RequestParam(value = IDCViewModelConstants.TREE_NODE_PATH, required = true) String path, Model model) {
		log.debug("Generating for path: '" + path + "'");

		String jsonTree = "";
		try {
			LoginService loginService = userServiceModel.getLoginService();
			ProjectService projectService = userServiceModel.getProjectService();
			ProtexServerProxy proxy = loginService.getProxy(serverName);
			jsonTree = projectService.getFolderJSON(proxy, projectId, path);
		} catch (Exception e) {
			log.error("Connection not established", e);
		}
		log.info("Returning JSON: " + jsonTree);
		return jsonTree;
	}

	/**
	 * Refreshes the BOM of a particular project
	 * 
	 * @param serverName
	 * @param projectId
	 * @param path
	 * @param model
	 * @return
	 */
	@RequestMapping(IDCPathConstants.REFRESH_BOM + "/{serverName}/{projectId}")
	public void refreshBOM(@PathVariable String serverName, @PathVariable String projectId, @RequestParam(value = IDCViewModelConstants.PARTIAL_BOM_OPTION) Boolean partialBomRefresh) {
		log.debug("Preparing BOM Refresh for project: '" + projectId + "'");

		try {
			log.debug("Partial BOM Refresh Option: " + partialBomRefresh);
			LoginService loginService = userServiceModel.getLoginService();
			ProtexServerProxy proxy = loginService.getProxy(serverName);
			BomApi bomApi = proxy.getBomApi();
			bomApi.refreshBom(projectId, partialBomRefresh, true);

			log.debug("BOM Refresh completed for project ID: " + projectId);
		} catch (Exception e) {
			log.error("Error during BOM refresh", e);
		}
	}

	@RequestMapping(IDCPathConstants.REFRESH_BOM_STATUS + "/{serverName}/{projectId}")
	public String refreshBOMStatus(@PathVariable String serverName, @PathVariable String projectId) {
		log.debug("Getting BOM Refresh status for: '" + projectId + "'");
		Gson gson = new Gson();
		String jsonRefreshStatus = "";
		try {
			LoginService loginService = userServiceModel.getLoginService();
			ProjectService projectService = userServiceModel.getProjectService();
			ProtexServerProxy proxy = loginService.getProxy(serverName);
			BomProgressStatus status = projectService.getBOMRefreshStatus(proxy, projectId);
			jsonRefreshStatus = gson.toJson(status);
			log.debug("BOM Refresh status (JSON): " + jsonRefreshStatus);
		} catch (Exception e) {
			log.error("Error getting refresh status: " + e.getMessage());
		}

		return jsonRefreshStatus;

	}
}
