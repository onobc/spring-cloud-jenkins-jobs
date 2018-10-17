package org.springframework.jenkins.cloud.e2e

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class BreweryEndToEndBuildMaker extends EndToEndBuildMaker {
	private final String repoName = 'brewery'

	BreweryEndToEndBuildMaker(DslFactory dsl) {
		super(dsl, 'spring-cloud-samples')
	}

	void build(String releaseTrainName) {
		build(releaseTrainName, releaseTrainName)
	}

	void build(String prefix, String releaseTrainName) {
		buildWithSwitches(prefix, defaultSwitches(releaseTrainName))
	}

	protected void buildWithSwitches(String prefix, String defaultSwitches) {
		super.build("$prefix-zookeeper", repoName(), "runAcceptanceTests.sh -t ZOOKEEPER $defaultSwitches", oncePerDay())
		super.build("$prefix-sleuth", repoName(), "runAcceptanceTests.sh -t SLEUTH $defaultSwitches", oncePerDay())
		super.build("$prefix-eureka", repoName(), "runAcceptanceTests.sh -t EUREKA $defaultSwitches", oncePerDay())
	}

	protected String repoName() {
		return this.repoName
	}

	protected String defaultSwitches(String releaseTrainName) {
		String releaseTrain = releaseTrainName.capitalize()
		return "--killattheend -v ${releaseTrain}.BUILD-SNAPSHOT --branch ${branchName()} -r"
	}
}
