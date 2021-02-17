package springcloud

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseRepoPurger
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMasterMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaserOptions

import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_MASTER_RELEASER_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_RELEASER_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_STREAM_JOBS_FOR_RELEASER

DslFactory dsl = this

// RELEASER
ALL_MASTER_RELEASER_JOBS.each {
	new SpringCloudReleaseMasterMaker(dsl).release(it, SpringCloudReleaserOptions.springCloudMaster())
}
ALL_RELEASER_JOBS.each {
	new SpringCloudReleaseMaker(dsl).release(it, SpringCloudReleaserOptions.springCloud())
}
ALL_STREAM_JOBS_FOR_RELEASER.each {
	new SpringCloudReleaseMaker(dsl).release(it, SpringCloudReleaserOptions.springCloudStream())
}
new SpringCloudMetaReleaseMaker(dsl)
		.release("spring-cloud-meta-releaser", SpringCloudReleaserOptions.springCloud())
new SpringCloudMetaReleaseMaker(dsl)
		.release("spring-cloud-stream-meta-releaser", SpringCloudReleaserOptions.springCloudStream())
new SpringCloudMetaReleaseRepoPurger(dsl).build()

// Compatibility builds
// new ManualBootCompatibilityBuildMaker(dsl).build()
