package org.springframework.jenkins.cloud.release

import groovy.transform.CompileStatic

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class ReleaserOptions {
	final boolean dryRun
	final boolean updateSagan
	final boolean updateDocumentationRepos
	final boolean updateSpringProjects
	final boolean updateReleaseTrainWiki
	final boolean runUpdatedSamples
	final boolean updateAllTestSamples
	final boolean updateReleaseTrainDocs
	final boolean updateSpringGuides
	final boolean updateStartSpringIo
	final boolean updateGithubMilestones
	final boolean postReleaseOnly
	final String releaseTrainProjectName
	final String releaserConfigUrl
	final String releaserConfigBranch
	final List<String> releaseTrainDependencyNames
	final String releaseTrainBomUrl
	final String releaseThisTrainBom
	final String projectsToSkip

	protected ReleaserOptions(boolean dryRun, boolean updateSagan, boolean updateDocumentationRepos, boolean updateSpringProjects, boolean updateReleaseTrainWiki, boolean runUpdatedSamples, boolean updateAllTestSamples, boolean updateReleaseTrainDocs, boolean updateSpringGuides, boolean updateStartSpringIo, boolean updateGithubMilestones, boolean postReleaseOnly, String releaseTrainProjectName, String releaserConfigUrl, String releaserConfigBranch, List<String> releaseTrainDependencyNames, String releaseTrainBomUrl, String releaseThisTrainBom, String projectsToSkip) {
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
