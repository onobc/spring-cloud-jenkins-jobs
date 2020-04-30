package org.springframework.jenkins.cloud.e2e

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class LatestJdkBreweryEndToEndBuildMaker extends BreweryEndToEndBuildMaker {
	private static final String RELEASE_TRAIN_NAME = "hoxton"

	LatestJdkBreweryEndToEndBuildMaker(DslFactory dsl) {
		super(dsl)
	}

	void build() {
		super.build("brewery-${jdkVersion()}", RELEASE_TRAIN_NAME)
	}

	@Override
	protected String jdkVersion() {
		return jdk14()
	}
}
