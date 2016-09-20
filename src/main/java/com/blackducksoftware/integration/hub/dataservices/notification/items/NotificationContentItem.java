package com.blackducksoftware.integration.hub.dataservices.notification.items;

import java.util.Date;
import java.util.UUID;

import com.blackducksoftware.integration.hub.api.project.ProjectVersion;

public class NotificationContentItem implements Comparable<NotificationContentItem> {
	private final ProjectVersion projectVersion;
	private final String componentName;
	private final String componentVersion;

	private final UUID componentId;
	private final UUID componentVersionId;

	// We need createdAt (from the enclosing notificationItem) so we can order
	// them after
	// they are collected multi-threaded
	public final Date createdAt;

	public NotificationContentItem(final Date createdAt, final ProjectVersion projectVersion,
			final String componentName,
			final String componentVersion, final UUID componentId, final UUID componentVersionId) {
		this.createdAt = createdAt;
		this.projectVersion = projectVersion;
		this.componentName = componentName;
		this.componentVersion = componentVersion;
		this.componentId = componentId;
		this.componentVersionId = componentVersionId;

	}

	public ProjectVersion getProjectVersion() {
		return projectVersion;
	}

	public String getComponentName() {
		return componentName;
	}

	public String getComponentVersion() {
		return componentVersion;
	}

	public UUID getComponentId() {
		return componentId;
	}

	public UUID getComponentVersionId() {
		return componentVersionId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("NotificationContentItem [projectVersion=");
		builder.append(projectVersion);
		builder.append(", componentName=");
		builder.append(componentName);
		builder.append(", componentVersion=");
		builder.append(componentVersion);
		builder.append(", componentId=");
		builder.append(componentId);
		builder.append(", componentVersionId=");
		builder.append(componentVersionId);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(final NotificationContentItem o) {
		final int createdAtComparison = getCreatedAt().compareTo(o.getCreatedAt());
		if (createdAtComparison != 0) {
			// If createdAt times are different, use createdAt to compare
			System.out.println("\tResult (based solely on createdAt): " + createdAtComparison);
			return createdAtComparison;
		}
		// If createdAt values are identical, see if they are truly equal
		if (equals(o)) {
			System.out.println("\tResult (the two are identical): " + 0);
			return 0;
		}
		// Identify same-time non-equal items as non-equal
		System.out.println("\tResult (same createdAt, different in some other field): " + 1);
		return 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((componentId == null) ? 0 : componentId.hashCode());
		result = prime * result + ((componentVersionId == null) ? 0 : componentVersionId.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((projectVersion == null) ? 0 : projectVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NotificationContentItem other = (NotificationContentItem) obj;
		if (componentId == null) {
			if (other.componentId != null) {
				return false;
			}
		} else if (!componentId.equals(other.componentId)) {
			return false;
		}
		if (componentVersionId == null) {
			if (other.componentVersionId != null) {
				return false;
			}
		} else if (!componentVersionId.equals(other.componentVersionId)) {
			return false;
		}
		if (createdAt == null) {
			if (other.createdAt != null) {
				return false;
			}
		} else if (!createdAt.equals(other.createdAt)) {
			return false;
		}
		if (projectVersion == null) {
			if (other.projectVersion != null) {
				return false;
			}
		} else if (!projectVersion.equals(other.projectVersion)) {
			return false;
		}
		return true;
	}


}
