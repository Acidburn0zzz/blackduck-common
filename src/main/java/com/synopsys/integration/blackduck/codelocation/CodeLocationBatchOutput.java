/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.blackduck.codelocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public abstract class CodeLocationBatchOutput<T extends CodeLocationOutput> implements Iterable<T> {
    private final Map<String, Integer> successfulCodeLocationNamesToExpectedNotificationCounts = new HashMap<>();
    private final List<T> outputs = new ArrayList<>();

    public CodeLocationBatchOutput(List<T> outputs) {
        successfulCodeLocationNamesToExpectedNotificationCounts
                .putAll(
                        outputs
                                .stream()
                                .peek(this.outputs::add)
                                .filter(output -> Result.SUCCESS == output.getResult())
                                .filter(output -> StringUtils.isNotBlank(output.getCodeLocationName()))
                                .collect(Collectors.toMap(CodeLocationOutput::getCodeLocationName, CodeLocationOutput::getExpectedNotificationCount))
                );
    }

    public List<T> getOutputs() {
        return outputs;
    }

    public Set<String> getSuccessfulCodeLocationNames() {
        return successfulCodeLocationNamesToExpectedNotificationCounts.keySet();
    }

    public int getExpectedNotificationCount() {
        return successfulCodeLocationNamesToExpectedNotificationCounts
                       .values()
                       .stream()
                       .mapToInt(Integer::intValue)
                       .sum();
    }

    @Override
    public Iterator<T> iterator() {
        return outputs.iterator();
    }

}
