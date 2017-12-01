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
package com.blackducksoftware.integration.hub.service;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.HubResponse;
import com.blackducksoftware.integration.hub.model.HubView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class HubResponseService {
    private final HubRequestFactory hubRequestFactory;
    private final URL hubBaseUrl;
    private final JsonParser jsonParser;
    private final Gson gson;
    protected final MetaService metaService;

    public HubResponseService(final RestConnection restConnection) {
        this.hubRequestFactory = new HubRequestFactory(restConnection);
        this.hubBaseUrl = restConnection.hubBaseUrl;
        this.jsonParser = restConnection.jsonParser;
        this.gson = restConnection.gson;
        this.metaService = new MetaService(restConnection.logger);
    }

    public URL getHubBaseUrl() {
        return hubBaseUrl;
    }

    public HubRequestFactory getHubRequestFactory() {
        return hubRequestFactory;
    }

    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public Gson getGson() {
        return gson;
    }

    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz) throws IntegrationException {
        return getAllItemsFromApi(apiSegment, clazz, 100);
    }

    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz, final int itemsPerPage) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(itemsPerPage, Arrays.asList(SEGMENT_API, apiSegment));
        final List<T> allItems = getAllItems(hubPagedRequest, clazz);
        return allItems;
    }

    public <T extends HubResponse> List<T> getAllItemsFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        final String link = metaService.getFirstLink(hubView, metaLinkRef);
        final List<T> allItems = getAllItems(link, clazz);
        return allItems;
    }

    public <T extends HubResponse> List<T> getAllItemsFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        if (metaService.hasLink(hubView, metaLinkRef)) {
            return getAllItemsFromLink(hubView, metaLinkRef, clazz);
        } else {
            return new ArrayList<>();
        }
    }

    public <T extends HubResponse> T getItemFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        final String link = metaService.getFirstLink(hubView, metaLinkRef);
        final T item = getItem(link, clazz);
        return item;
    }

    public <T extends HubResponse> T getItemFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        if (metaService.hasLink(hubView, metaLinkRef)) {
            return getItemFromLink(hubView, metaLinkRef, clazz);
        } else {
            return null;
        }
    }

    public <T extends HubResponse> T getItemAs(final JsonElement item, final Class<T> clazz) {
        final T hubItem = gson.fromJson(item, clazz);
        hubItem.json = gson.toJson(item);
        return hubItem;
    }

    public <T extends HubResponse> T getItemAs(final String item, final Class<T> clazz) {
        final T hubItem = gson.fromJson(item, clazz);
        hubItem.json = item;
        return hubItem;
    }

    public <T extends HubResponse> T getItem(final HubRequest request, final Class<T> clazz) throws IntegrationException {
        return getItem(request, clazz, null);
    }

    public <T extends HubResponse> T getItem(final HubRequest request, final Class<T> clazz, final String mediaType) throws IntegrationException {
        Response response = null;
        try {
            if (StringUtils.isNotBlank(mediaType)) {
                response = request.executeGet(mediaType);
            } else {
                response = request.executeGet();
            }
            // the string method closes the body
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            return getItemAs(jsonObject, clazz);
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public <T extends HubResponse> T getItem(final String url, final Class<T> clazz) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(url);
        return getItem(request, clazz);
    }

    public <T extends HubResponse> List<T> getItems(final JsonArray itemsArray, final Class<T> clazz) {
        final LinkedList<T> itemList = new LinkedList<>();
        for (final JsonElement element : itemsArray) {
            final T item = getItemAs(element, clazz);
            itemList.add(item);
        }
        return itemList;
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getItems(final JsonObject jsonObject, final Class<T> clazz) throws IntegrationException {
        final LinkedList<T> itemList = new LinkedList<>();
        final JsonElement itemsElement = jsonObject.get("items");
        final JsonArray itemsArray = itemsElement.getAsJsonArray();
        for (final JsonElement element : itemsArray) {
            final T item = getItemAs(element, clazz);
            itemList.add(item);
        }
        return itemList;
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return getItems(hubPagedRequest, clazz, null);
    }

    /**
     * Will NOT make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        Response response = null;
        try {
            if (StringUtils.isNotBlank(mediaType)) {
                response = hubPagedRequest.executeGet(mediaType);
            } else {
                response = hubPagedRequest.executeGet();
            }
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            return getItems(jsonObject, clazz);
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * Will make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return getAllItems(hubPagedRequest, clazz, null);
    }

    /**
     * Will make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final List<T> allItems = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = hubPagedRequest.offset;
        Response response = null;
        try {
            if (StringUtils.isNotBlank(mediaType)) {
                response = hubPagedRequest.executeGet(mediaType);
            } else {
                response = hubPagedRequest.executeGet();
            }
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            totalCount = jsonObject.get("totalCount").getAsInt();
            allItems.addAll(getItems(jsonObject, clazz));
            while (allItems.size() < totalCount && currentOffset < totalCount) {
                currentOffset += hubPagedRequest.limit;
                hubPagedRequest.offset = currentOffset;
                allItems.addAll(getItems(hubPagedRequest, clazz, mediaType));
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return allItems;
    }

    /**
     * Will make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final String url, final Class<T> clazz) throws IntegrationException {
        final HubPagedRequest pagedRequest = hubRequestFactory.createPagedRequest(url);
        return getAllItems(pagedRequest, clazz, null);
    }

    /**
     * Will make further paged requests to get the full list of items
     */
    public <T extends HubResponse> List<T> getAllItems(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final HubPagedRequest pagedRequest = hubRequestFactory.createPagedRequest(url);
        return getAllItems(pagedRequest, clazz, mediaType);
    }

}