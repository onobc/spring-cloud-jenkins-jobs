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

	private void buildWithSwitches(String prefix, String defaultSwitches) {
		super.build("$prefix-zookeeper", repoName, "runAcceptanceTests.sh -t ZOOKEEPER $defaultSwitches", oncePerDay())
		super.build("$prefix-sleuth", repoName, "runAcceptanceTests.sh -t SLEUTH $defaultSwitches", oncePerDay())
		super.build("$prefix-sleuth-stream", repoName, "runAcceptanceTests.sh -t SLEUTH_STREAM $defaultSwitches", oncePerDay())
		super.build("$prefix-sleuth-stream-kafka", repoName, "runAcceptanceTests.sh -t SLEUTH_STREAM -k $defaultSwitches", oncePerDay())
		super.build("$prefix-eureka", repoName, "runAcceptanceTests.sh -t EUREKA $defaultSwitches", oncePerDay())
		super.build("$prefix-consul", repoName, "runAcceptanceTests.sh -t CONSUL $defaultSwitches", oncePerDay())
	}


	private String defaultSwitches(String releaseTrainName) {
		String releaseTrain = releaseTrainName.capitalize()
		return "--killattheend -v ${releaseTrain}.BUILD-SNAPSHOT --branch ${branchName()} -r"
	}
}
