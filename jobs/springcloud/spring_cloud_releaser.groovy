package springcloud

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.compatibility.ManualBootCompatibilityBuildMaker
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseRepoPurger
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMainMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaserOptions

import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_MAIN_RELEASER_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_RELEASER_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_STREAM_JOBS_FOR_RELEASER

DslFactory dsl = this

// RELEASER
ALL_MAIN_RELEASER_JOBS.each {
	new SpringCloudReleaseMainMaker(dsl).release(it, SpringCloudReleaserOptions.springCloudMain())
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
new ManualBootCompatibilityBuildMaker(dsl).build()

// Incubator
new SpringCloudReleaseMaker(dsl, "spring-cloud-incubator").release("spring-cloud-square", SpringCloudReleaserOptions.springCloudIncubator())