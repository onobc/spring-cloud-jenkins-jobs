package org.springframework.jenkins.cloud.e2e

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.AllCloudJobs

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class JdkBreweryEndToEndBuildMaker extends BreweryEndToEndBuildMaker {
	String jdkVersion = jdk8()

	JdkBreweryEndToEndBuildMaker(DslFactory dsl) {
		super(dsl)
	}

	void build() {
		super.build("brewery-${jdkVersion()}", AllCloudJobs.RELEASE_TRAIN_TO_BOOT_VERSION_MINOR.entrySet().first().key)
	}

	@Override
	protected String jdkVersion() {
		return this.jdkVersion
	}

	JdkBreweryEndToEndBuildMaker withJdk(String jdkVersion) {
		this.jdkVersion = jdkVersion
		return this
	}
}
