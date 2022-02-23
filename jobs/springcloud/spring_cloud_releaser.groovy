package springcloud

import javaposse.jobdsl.dsl.DslFactory
import org.springframework.jenkins.cloud.common.Projects
import org.springframework.jenkins.cloud.common.ReleaseTrains
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseRepoPurger
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMainMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaserOptions

import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_STREAM_JOBS_FOR_RELEASER

DslFactory dsl = this

// RELEASER
ReleaseTrains.CURRENT_ACTIVE.projectsWithBranch.each { project, branch ->
	new SpringCloudReleaseMainMaker(dsl).release(project.repo, SpringCloudReleaserOptions.springCloudMain())
}
Projects.ALL.each { project ->
	new SpringCloudReleaseMaker(dsl).release(project.repo, SpringCloudReleaserOptions.springCloud())
}
ALL_STREAM_JOBS_FOR_RELEASER.each {
	new SpringCloudReleaseMaker(dsl).release(it, SpringCloudReleaserOptions.springCloudStream())
}
new SpringCloudMetaReleaseMaker(dsl)
		.release("spring-cloud-meta-releaser", SpringCloudReleaserOptions.springCloud())
new SpringCloudMetaReleaseMaker(dsl)
		.release("spring-cloud-stream-meta-releaser", SpringCloudReleaserOptions.springCloudStream())
new SpringCloudMetaReleaseRepoPurger(dsl).build()

// TODO: move this
// Compatibility builds
//new ManualBootCompatibilityBuildMaker(dsl).build()

// Experimental
ReleaseTrains.EXPERIMENTAL.projectsWithBranch.each { project, branch ->
new SpringCloudReleaseMaker(dsl, project.org).release(project.repo, SpringCloudReleaserOptions.springProjectsExperimental()) }