package org.springframework.jenkins.cloud.release

import groovy.transform.CompileStatic

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class ReleaserOptions {
	String projectName
	boolean dryRun
	boolean updateSagan
	boolean updateDocumentationRepos
	boolean updateSpringProjects
	boolean updateReleaseTrainWiki
	boolean runUpdatedSamples
	boolean updateAllTestSamples
	boolean updateReleaseTrainDocs
	boolean updateSpringGuides
	boolean updateStartSpringIo
	boolean updateGithubMilestones
	boolean postReleaseOnly
	String releaseTrainProjectName
	String releaserConfigUrl
	String releaserConfigBranch
	List<String> releaseTrainDependencyNames
	String releaseTrainBomUrl
	String releaseThisTrainBom
	String projectsToSkip

	protected ReleaserOptions(String projectName, boolean dryRun, boolean updateSagan, boolean updateDocumentationRepos, boolean updateSpringProjects, boolean updateReleaseTrainWiki, boolean runUpdatedSamples, boolean updateAllTestSamples, boolean updateReleaseTrainDocs, boolean updateSpringGuides, boolean updateStartSpringIo, boolean updateGithubMilestones, boolean postReleaseOnly, String releaseTrainProjectName, String releaserConfigUrl, String releaserConfigBranch, List<String> releaseTrainDependencyNames, String releaseTrainBomUrl, String releaseThisTrainBom, String projectsToSkip) {
		this.projectName = projectName;
		this.dryRun = dryRun
		this.updateSagan = updateSagan
		this.updateDocumentationRepos = updateDocumentationRepos
		this.updateSpringProjects = updateSpringProjects
		this.updateReleaseTrainWiki = updateReleaseTrainWiki
		this.runUpdatedSamples = runUpdatedSamples
		this.updateAllTestSamples = updateAllTestSamples
		this.updateReleaseTrainDocs = updateReleaseTrainDocs
		this.updateSpringGuides = updateSpringGuides
		this.updateStartSpringIo = updateStartSpringIo
		this.updateGithubMilestones = updateGithubMilestones
		this.postReleaseOnly = postReleaseOnly
		this.releaseTrainProjectName = releaseTrainProjectName
		this.releaserConfigUrl = releaserConfigUrl
		this.releaserConfigBranch = releaserConfigBranch
		this.releaseTrainDependencyNames = releaseTrainDependencyNames
		this.releaseTrainBomUrl = releaseTrainBomUrl
		this.releaseThisTrainBom = releaseThisTrainBom
		this.projectsToSkip = projectsToSkip
		assert projectName != null
		assert releaseTrainProjectName != null
		assert releaserConfigUrl != null
		assert releaserConfigBranch != null
		assert releaseTrainDependencyNames != null
		assert releaseTrainBomUrl != null
		assert releaseThisTrainBom != null
	}

	static ReleaserOptionsBuilder builder() {
		return new ReleaserOptionsBuilder()
	}
}
