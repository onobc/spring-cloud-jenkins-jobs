package org.springframework.jenkins.cloud.e2e

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory
/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class EdgwareBreweryEndToEndBuildMaker extends BreweryEndToEndBuildMaker {
	private static final String RELEASE_TRAIN_NAME = "edgware"

	EdgwareBreweryEndToEndBuildMaker(DslFactory dsl) {
		super(dsl)
	}

	void build() {
		super.build(RELEASE_TRAIN_NAME)
	}

	@Override
	protected void buildWithSwitches(String prefix, String defaultSwitches) {
		super.buildWithSwitches(prefix, defaultSwitches)
		super.build("$prefix-sleuth-stream", repoName(), "runAcceptanceTests.sh -t SLEUTH_STREAM $defaultSwitches", oncePerDay())
	}

	@Override
	protected String branchName() {
		return "edgware"
	}
}
