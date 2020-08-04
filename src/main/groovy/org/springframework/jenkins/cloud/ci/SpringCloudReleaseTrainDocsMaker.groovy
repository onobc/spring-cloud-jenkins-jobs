package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Marcin Grzejszczak
 */
class SpringCloudReleaseTrainDocsMaker extends SpringCloudDeployBuildMaker {

	SpringCloudReleaseTrainDocsMaker(DslFactory dsl) {
		super(dsl, 'spring-cloud')
	}

	void deploy(String branchToBuild) {
		super.deploy("spring-cloud-release", branchToBuild)
	}

	@Override
	String prefixedName(String project) {
		return super.prefixedName(project) + "-train-docs"
	}

	@Override
	String buildCommand() {
		return this.deploy ? "./mvnw clean deploy -Pdocs,deploy,train-docs,spring -B -pl train-docs" : "./mvnw clean install -Pdocs,deploy,train-docs,spring -B -pl train-docs"
	}
}
