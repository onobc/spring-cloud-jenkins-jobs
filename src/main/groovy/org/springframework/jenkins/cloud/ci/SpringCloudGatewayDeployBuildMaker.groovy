package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Spencer Gibb
 */
class SpringCloudGatewayDeployBuildMaker extends SpringCloudDeployBuildMaker {

	private final String projectName = "spring-cloud-gateway"

	SpringCloudGatewayDeployBuildMaker(DslFactory dsl) {
		super(dsl, 'spring-cloud')
	}

	void deploy() {
		super.deploy(projectName)
	}

	void deploy(String branchName) {
		super.deploy(projectName, branchName)
	}
}
