package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Spencer Gibb
 */
class SpringCloudAlibabaDeployBuildMaker extends SpringCloudDeployBuildMaker {

	SpringCloudAlibabaDeployBuildMaker(DslFactory dsl) {
		super(dsl, 'spring-cloud-incubator')
	}

	void deploy() {
		super.deploy("spring-cloud-alibaba", masterBranch())
	}


	void deploy(String branchToBuild) {
		super.deploy("spring-cloud-alibaba", branchToBuild)
	}
}
