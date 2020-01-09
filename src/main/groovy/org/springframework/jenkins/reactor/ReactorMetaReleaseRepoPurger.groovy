package org.springframework.jenkins.reactor

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseRepoPurger

class ReactorMetaReleaseRepoPurger extends SpringCloudMetaReleaseRepoPurger implements ReactorJobs {
	ReactorMetaReleaseRepoPurger(DslFactory dsl) {
		super(dsl)
	}
}
