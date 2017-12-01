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
package com.blackducksoftware.integration.hub.api.scan;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_SCAN_SUMMARIES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class ScanSummaryRequestService extends HubResponseService {
    private static final List<String> SCAN_SUMMARIES_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_SCAN_SUMMARIES);

    public ScanSummaryRequestService(final RestConnection restConnection) {
        super(restConnection);
    }

    public List<ScanSummaryView> getAllScanSummaryItems(final String scanSummaryUrl) throws IntegrationException {
        final List<ScanSummaryView> allScanSummaryItems = getAllItems(scanSummaryUrl, ScanSummaryView.class);
        return allScanSummaryItems;
    }

    public ScanSummaryView getScanSummaryViewById(final String scanSummaryId) throws IntegrationException {
        final List<String> segments = new ArrayList<>(SCAN_SUMMARIES_SEGMENTS);
        segments.add(scanSummaryId);
        final HubRequest request = getHubRequestFactory().createRequest(segments);
        return getItem(request, ScanSummaryView.class);
    }

}
