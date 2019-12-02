/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation.bdio2upload;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class UploadBdio2Callable implements Callable<UploadOutput> {
    private final BlackDuckService blackDuckService;
    private final UploadTarget uploadTarget;

    public UploadBdio2Callable(BlackDuckService blackDuckService, UploadTarget uploadTarget) {
        this.blackDuckService = blackDuckService;
        this.uploadTarget = uploadTarget;
    }

    @Override
    public UploadOutput call() {
        try {
            String uri = blackDuckService.getUri(BlackDuckService.SCAN_DATA_PATH);
            Request request = RequestFactory.createCommonPostRequestBuilder(uploadTarget.getUploadFile()).uri(uri).mimeType(uploadTarget.getMediaType()).build();
            try (Response response = blackDuckService.execute(request)) {
                String responseString = response.getContentString();
                return UploadOutput.SUCCESS(uploadTarget.getCodeLocationName(), responseString);
            } catch (IOException e) {
                return UploadOutput.FAILURE(uploadTarget.getCodeLocationName(), e.getMessage(), e);
            }
        } catch (Exception e) {
            return UploadOutput.FAILURE(uploadTarget.getCodeLocationName(), "Failed to upload file: " + uploadTarget.getUploadFile().getAbsolutePath() + " because " + e.getMessage(), e);
        }
    }

}
