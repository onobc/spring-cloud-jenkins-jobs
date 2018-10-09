package org.springframework.jenkins.cloud.e2e

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.AllCloudConstants

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
	protected String branchName() {
		return "edgware"
	}
}
