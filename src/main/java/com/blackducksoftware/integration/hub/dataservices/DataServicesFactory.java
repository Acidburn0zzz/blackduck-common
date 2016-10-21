/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.dataservices;

import com.blackducksoftware.integration.hub.api.bom.BomImportRestService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRestService;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionConfigRestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionRestService;
import com.blackducksoftware.integration.hub.api.extension.ExtensionUserOptionRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRestService;
import com.blackducksoftware.integration.hub.api.project.ReleaseItemRestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.api.user.UserRestService;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.vulnerabilities.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.api.vulnerableBomComponent.VulnerableBomComponentRestService;
import com.blackducksoftware.integration.hub.dataservices.extension.ExtensionConfigDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class DataServicesFactory {
    private final RestConnection restConnection;

    final Gson gson = new Gson();

    final JsonParser jsonParser = new JsonParser();

    private final BomImportRestService bomImportRestService;

    private final CodeLocationRestService codeLocationRestService;

    private final ComponentVersionRestService componentVersionRestService;

    private final NotificationRestService notificationRestService;

    private final PolicyRestService policyRestService;

    private final PolicyStatusRestService policyStatusRestService;

    private final ProjectRestService projectRestService;

    private final ProjectVersionRestService projectVersionRestService;

    private final ReleaseItemRestService releaseItemRestService;

    private final ScanSummaryRestService scanSummaryRestService;

    private final UserRestService userRestService;

    private final VersionBomPolicyRestService versionBomPolicyRestService;

    private final VulnerabilityRestService vulnerabilityRestService;

    private final ExtensionRestService extensionRestService;

    private final ExtensionConfigRestService extensionConfigRestService;

    private final ExtensionUserOptionRestService extensionUserOptionRestService;

    private final VulnerableBomComponentRestService vulnerableBomComponentRestService;

    public DataServicesFactory(final RestConnection restConnection) {
        this.restConnection = restConnection;

        bomImportRestService = new BomImportRestService(restConnection);
        codeLocationRestService = new CodeLocationRestService(restConnection, gson, jsonParser);
        componentVersionRestService = new ComponentVersionRestService(restConnection, gson, jsonParser);
        notificationRestService = new NotificationRestService(restConnection, gson, jsonParser);
        policyRestService = new PolicyRestService(restConnection, gson, jsonParser);
        policyStatusRestService = new PolicyStatusRestService(restConnection, gson, jsonParser);
        projectRestService = new ProjectRestService(restConnection, gson, jsonParser);
        projectVersionRestService = new ProjectVersionRestService(restConnection, gson, jsonParser);
        releaseItemRestService = new ReleaseItemRestService(restConnection, gson, jsonParser);
        scanSummaryRestService = new ScanSummaryRestService(restConnection, gson, jsonParser);
        userRestService = new UserRestService(restConnection, gson, jsonParser);
        versionBomPolicyRestService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
        vulnerabilityRestService = new VulnerabilityRestService(restConnection, gson, jsonParser);
        extensionRestService = new ExtensionRestService(restConnection, gson, jsonParser);
        extensionConfigRestService = new ExtensionConfigRestService(restConnection, gson, jsonParser);
        extensionUserOptionRestService = new ExtensionUserOptionRestService(restConnection, gson, jsonParser);
        vulnerableBomComponentRestService = new VulnerableBomComponentRestService(restConnection, gson, jsonParser);
    }

    public PolicyStatusDataService createPolicyStatusDataService() {
        return new PolicyStatusDataService(restConnection, gson, jsonParser, projectRestService,
                projectVersionRestService, policyStatusRestService);
    }

    public ScanStatusDataService createScanStatusDataService() {
        return new ScanStatusDataService(restConnection, gson, jsonParser, projectRestService, projectVersionRestService,
                codeLocationRestService, scanSummaryRestService);
    }

    public NotificationDataService createNotificationDataService(final IntLogger logger) {
        return new NotificationDataService(logger, restConnection, gson, jsonParser);
    }

    public NotificationDataService createNotificationDataService(final IntLogger logger,
            final PolicyNotificationFilter policyNotificationFilter) {
        return new NotificationDataService(logger, restConnection, gson, jsonParser, policyNotificationFilter);
    }

    public ExtensionConfigDataService createExtensionConfigDataService(final IntLogger logger) {
        return new ExtensionConfigDataService(logger, restConnection, gson, jsonParser, userRestService,
                extensionRestService, extensionConfigRestService, extensionUserOptionRestService);
    }

    public RestConnection getRestConnection() {
        return restConnection;
    }

    public Gson getGson() {
        return gson;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public BomImportRestService getBomImportRestService() {
        return bomImportRestService;
    }

    public CodeLocationRestService getCodeLocationRestService() {
        return codeLocationRestService;
    }

    public ComponentVersionRestService getComponentVersionRestService() {
        return componentVersionRestService;
    }

    public NotificationRestService getNotificationRestService() {
        return notificationRestService;
    }

    public PolicyRestService getPolicyRestService() {
        return policyRestService;
    }

    public PolicyStatusRestService getPolicyStatusRestService() {
        return policyStatusRestService;
    }

    public ProjectRestService getProjectRestService() {
        return projectRestService;
    }

    public ProjectVersionRestService getProjectVersionRestService() {
        return projectVersionRestService;
    }

    public ReleaseItemRestService getReleaseItemRestService() {
        return releaseItemRestService;
    }

    public ScanSummaryRestService getScanSummaryRestService() {
        return scanSummaryRestService;
    }

    public UserRestService getUserRestService() {
        return userRestService;
    }

    public VersionBomPolicyRestService getVersionBomPolicyRestService() {
        return versionBomPolicyRestService;
    }

    public VulnerabilityRestService getVulnerabilityRestService() {
        return vulnerabilityRestService;
    }

    public ExtensionConfigRestService getExtensionConfigRestService() {
        return extensionConfigRestService;
    }

    public ExtensionRestService getExtensionRestService() {
        return extensionRestService;
    }

    public ExtensionUserOptionRestService getExtensionUserOptionRestService() {
        return extensionUserOptionRestService;
    }

    public VulnerableBomComponentRestService getVulnerableBomComponentRestService() {
        return vulnerableBomComponentRestService;
    }
}
