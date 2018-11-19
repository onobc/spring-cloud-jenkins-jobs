package org.springframework.jenkins.cloud.release

import groovy.transform.builder.Builder

import org.springframework.jenkins.cloud.common.AllCloudConstants

/**
 * @author Marcin Grzejszczak
 */
@Builder
class ReleaserOptions {
	boolean updateSagan = true
	boolean updateDocumentationRepos = true
	boolean updateSpringProjects = true
	boolean updateReleaseTrainWiki = true
	boolean runUpdatedSamples = true
	boolean updateAllTestSamples = true
	boolean updateReleaseTrainDocs = true
	boolean updateSpringGuides = true
	String releaseTrainProjectName = "spring-cloud-release"
	List<String> releaseTrainDependencyNames = ["spring-cloud", "spring-cloud-dependencies", "spring-cloud-starter"]
	String releaseTrainBomUrl = "https://github.com/spring-cloud/spring-cloud-release"
	String releaseThisTrainBom = "spring-cloud-dependencies/pom.xml"
	String releaserVersions = AllCloudConstants.DEFAULT_RELEASER_PROPERTIES_FILE_CONTENT
	String projectsToSkip = AllCloudConstants.DEFAULT_RELEASER_SKIPPED_PROJECTS
}
