package org.springframework.jenkins.cloud.e2e

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class Jdk11BreweryEndToEndBuildMaker extends BreweryEndToEndBuildMaker {
	private static final String RELEASE_TRAIN_NAME = "2020.0.0"

	Jdk11BreweryEndToEndBuildMaker(DslFactory dsl) {
		super(dsl)
	}

	void build() {
		super.build("brewery-${jdkVersion()}", RELEASE_TRAIN_NAME)
	}

	@Override
	protected String jdkVersion() {
		return jdk11()
	}
}
