package org.springframework.jenkins.cloud.release

import groovy.transform.builder.Builder

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
	String releaseTrainBomUrl = "https://github.com/spring-cloud/spring-cloud-release"
	String releaseThisTrainBom = "spring-cloud-dependencies/pom.xml"
}
