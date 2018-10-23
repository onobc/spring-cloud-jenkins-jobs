package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Spencer Gibb
 */
class SpringCloudKubernetesDeployBuildMaker extends SpringCloudDeployBuildMaker {

	SpringCloudKubernetesDeployBuildMaker(DslFactory dsl) {
		super(dsl, 'spring-cloud')
	}

	void deploy() {
		super.deploy("spring-cloud-kubernetes", masterBranch())
	}
}
