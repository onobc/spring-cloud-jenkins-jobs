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
	boolean updateStartSpringIo = true
	String releaseTrainProjectName = "spring-cloud-release"
	String releaserConfigUrl = "https://raw.githubusercontent.com/spring-cloud/spring-cloud-release"
	String releaserConfigBranch = "jenkins-releaser-config"
	List<String> releaseTrainDependencyNames = ["spring-cloud", "spring-cloud-dependencies", "spring-cloud-starter"]
	String releaseTrainBomUrl = "https://github.com/spring-cloud/spring-cloud-release"
	String releaseThisTrainBom = "spring-cloud-dependencies/pom.xml"
	String projectsToSkip = AllCloudConstants.DEFAULT_RELEASER_SKIPPED_PROJECTS
}
