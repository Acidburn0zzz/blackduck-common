/**
 * Hub Common
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
package com.blackducksoftware.integration.hub.api.project;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.model.view.AssignedGroupView;
import com.blackducksoftware.integration.hub.model.view.AssignedUserView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class ProjectAssignmentRequestService extends HubResponseService {

    public ProjectAssignmentRequestService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<AssignedUserView> getProjectUsers(final ProjectView projectView) throws IntegrationException {
        return getAllItemsFromLink(projectView, MetaService.USERS_LINK, AssignedUserView.class);
    }

    public List<AssignedGroupView> getProjectGroups(final ProjectView projectView) throws IntegrationException {
        return getAllItemsFromLink(projectView, MetaService.GROUPS_LINK, AssignedGroupView.class);
    }

}