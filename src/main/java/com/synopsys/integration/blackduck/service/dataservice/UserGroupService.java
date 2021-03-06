/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.response.UserProjectsView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RoleAssignmentView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.component.UserGroupRequest;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class UserGroupService extends DataService {
    public static final BiPredicate<String, UserView> MATCHING_USERNAME = (username, userView) -> username.equalsIgnoreCase(userView.getUserName());

    public UserGroupService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public UserGroupView createUserGroup(UserGroupRequest userGroupRequest) throws IntegrationException {
        HttpUrl userGroupUrl = blackDuckApiClient.post(ApiDiscovery.USERGROUPS_LINK, userGroupRequest);
        UserGroupView userGroupView = blackDuckApiClient.getResponse(userGroupUrl, UserGroupView.class);
        return userGroupView;
    }

    public Optional<UserView> getUserByUsername(String username) throws IntegrationException {
        Predicate<UserView> predicate = userView -> MATCHING_USERNAME.test(username, userView);
        List<UserView> matchingUsers = blackDuckApiClient.getSomeMatchingResponses(ApiDiscovery.USERS_LINK_RESPONSE, predicate, 1);
        if (!matchingUsers.isEmpty()) {
            return Optional.ofNullable(matchingUsers.get(0));
        }

        logger.error(String.format("The user (%s) does not exist.", username));
        return Optional.empty();
    }

    public List<ProjectView> getProjectsForUser(String username) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getProjectsForUser(user.get());
    }

    public List<ProjectView> getProjectsForUser(UserView userView) throws IntegrationException {
        logger.debug("Attempting to get the assigned projects for User: " + userView.getUserName());
        List<UserProjectsView> assignedProjectViews = blackDuckApiClient.getAllResponses(userView, UserView.PROJECTS_LINK_RESPONSE);

        List<ProjectView> resolvedProjectViews = new ArrayList<>();
        for (UserProjectsView assigned : assignedProjectViews) {
            HttpUrl projectUrl = new HttpUrl(assigned.getProject());
            ProjectView project = blackDuckApiClient.getResponse(projectUrl, ProjectView.class);
            if (project != null) {
                resolvedProjectViews.add(project);
            }
        }

        return resolvedProjectViews;
    }

    public List<RoleAssignmentView> getRolesForUser(String username) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getRolesForUser(UserView userView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(userView, UserView.ROLES_LINK_RESPONSE);
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(String username) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getInheritedRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getInheritedRolesForUser(UserView userView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(userView, UserView.INHERITED_ROLES_LINK_RESPONSE);
    }

    public List<RoleAssignmentView> getAllRolesForUser(String username) throws IntegrationException {
        Optional<UserView> user = getUserByUsername(username);
        if (!user.isPresent()) {
            return Collections.emptyList();
        }
        return getAllRolesForUser(user.get());
    }

    public List<RoleAssignmentView> getAllRolesForUser(UserView userView) throws IntegrationException {
        Set<RoleAssignmentView> roleSet = new LinkedHashSet<>();
        roleSet.addAll(getRolesForUser(userView));
        roleSet.addAll(getInheritedRolesForUser(userView));
        return new ArrayList(roleSet);
    }

    public Optional<UserGroupView> getGroupByName(String groupName) throws IntegrationException {
        List<UserGroupView> allGroups = blackDuckApiClient.getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE);
        for (UserGroupView group : allGroups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                return Optional.of(group);
            }
        }
        logger.error(String.format("The group (%s) does not exist.", groupName));
        return Optional.empty();
    }

}
