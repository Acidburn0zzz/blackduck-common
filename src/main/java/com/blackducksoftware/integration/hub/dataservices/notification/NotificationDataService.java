package com.blackducksoftware.integration.hub.dataservices.notification;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.blackducksoftware.integration.hub.api.ComponentVersionRestService;
import com.blackducksoftware.integration.hub.api.NotificationRestService;
import com.blackducksoftware.integration.hub.api.PolicyRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.VersionBomPolicyRestService;
import com.blackducksoftware.integration.hub.api.VulnerabilityRestService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationClearedNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.dataservices.AbstractDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ProjectAggregateBuilder;
import com.blackducksoftware.integration.hub.dataservices.notification.items.ProjectAggregateData;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.AbstractNotificationTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.NotificationCounter;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyViolationClearedTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyViolationOverrideTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.PolicyViolationTransform;
import com.blackducksoftware.integration.hub.dataservices.notification.transforms.VulnerabilityTransform;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class NotificationDataService extends AbstractDataService {
	private final IntLogger logger;
	private final NotificationRestService notificationService;
	private final ProjectVersionRestService projectVersionService;
	private final PolicyRestService policyService;
	private final VersionBomPolicyRestService bomVersionPolicyService;
	private final ComponentVersionRestService componentVersionService;
	private final Map<Class<?>, AbstractNotificationTransform> transformMap;
	private final ExecutorService executorService;
	private final ExecutorCompletionService<List<NotificationContentItem>> completionService;
	private final PolicyNotificationFilter policyFilter;
	private final VulnerabilityRestService vulnerabilityRestService;

	public NotificationDataService(final IntLogger logger, final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser,
			final PolicyNotificationFilter policyFilter) {
		super(restConnection, gson, jsonParser);
		this.logger = logger;
		notificationService = new NotificationRestService(restConnection, jsonParser);
		projectVersionService = new ProjectVersionRestService(restConnection, gson, jsonParser);
		policyService = new PolicyRestService(restConnection, gson, jsonParser);
		bomVersionPolicyService = new VersionBomPolicyRestService(restConnection, gson, jsonParser);
		componentVersionService = new ComponentVersionRestService(restConnection, gson, jsonParser);
		vulnerabilityRestService = new VulnerabilityRestService(restConnection, gson, jsonParser);
		this.policyFilter = policyFilter;
		transformMap = createTransformMap();
		final ThreadFactory threadFactory = Executors.defaultThreadFactory();
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
		completionService = new ExecutorCompletionService<>(executorService);
	}

	private Map<Class<?>, AbstractNotificationTransform> createTransformMap() {
		final Map<Class<?>, AbstractNotificationTransform> transformMap = new HashMap<>();
		transformMap.put(RuleViolationNotificationItem.class, new PolicyViolationTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService, policyFilter));
		transformMap.put(PolicyOverrideNotificationItem.class, new PolicyViolationOverrideTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService, policyFilter));
		transformMap.put(VulnerabilityNotificationItem.class, new VulnerabilityTransform(notificationService,
				projectVersionService, policyService, bomVersionPolicyService, componentVersionService));
		transformMap.put(RuleViolationClearedNotificationItem.class,
				new PolicyViolationClearedTransform(logger,
						notificationService, projectVersionService, policyService,
						bomVersionPolicyService, componentVersionService, policyFilter));

		return transformMap;
	}

	public SortedSet<NotificationContentItem> getAllNotifications(final Date startDate, final Date endDate,
			final int limit)
					throws IOException, URISyntaxException, BDRestException {
		final SortedSet<NotificationContentItem> contentList = new TreeSet<>();
		final List<NotificationItem> itemList = notificationService.getAllNotifications(startDate, endDate, limit);

		int submitted = 0;
		for (final NotificationItem item : itemList) {
			final Class<? extends NotificationItem> key = item.getClass();
			if (transformMap.containsKey(key)) {
				final AbstractNotificationTransform converter = transformMap.get(key);
				final TransformCallable callable = new TransformCallable(item, converter);
				completionService.submit(callable);
				submitted++;
			}
		}

		for (int index = 0; index < submitted; index++) {
			try {
				final Future<List<NotificationContentItem>> future = completionService.take();
				final List<NotificationContentItem> contentItems = future.get();
				contentList.addAll(contentItems);
			} catch (final ExecutionException | InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return contentList;
	}

	public List<ProjectAggregateData> getNotificationCounts(final Date startDate, final Date endDate, final int limit)
			throws IOException, URISyntaxException, BDRestException, InterruptedException {

		final Map<String, ProjectAggregateBuilder> projectCounterMap = new ConcurrentHashMap<>();
		final NotificationCounter counter = new NotificationCounter(projectCounterMap);
		final Set<NotificationContentItem> itemList = getAllNotifications(startDate, endDate, limit);
		for (final NotificationContentItem item : itemList) {
			counter.count(item);
		}

		final List<ProjectAggregateData> dataList = new ArrayList<>();
		for (final Map.Entry<String, ProjectAggregateBuilder> entry : projectCounterMap.entrySet()) {
			ProjectAggregateBuilder builder = entry.getValue().updateDateRange(startDate, endDate);
			builder = builder.updateVulnerabilitiesRestService(vulnerabilityRestService);
			dataList.add(builder.build());
		}
		return dataList;
	}

	private class TransformCallable implements Callable<List<NotificationContentItem>> {
		private final NotificationItem item;
		private final AbstractNotificationTransform converter;

		public TransformCallable(final NotificationItem item, final AbstractNotificationTransform converter) {
			this.item = item;
			this.converter = converter;
		}

		@Override
		public List<NotificationContentItem> call() throws Exception {
			return converter.transform(item);
		}
	}
}
