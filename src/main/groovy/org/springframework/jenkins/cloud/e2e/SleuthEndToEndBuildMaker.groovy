package org.springframework.jenkins.cloud.e2e

import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Marcin Grzejszczak
 */
class SleuthEndToEndBuildMaker extends EndToEndBuildMaker {
	SleuthEndToEndBuildMaker(DslFactory dsl) {
		super(dsl)
		setBranchName("3.1.x")
	}

	void buildSleuth(String cronExpr) {
		super.build("spring-cloud-sleuth", cronExpr)
	}

	@Override
	protected String jdkVersion() {
		return jdk11()
	}
}
