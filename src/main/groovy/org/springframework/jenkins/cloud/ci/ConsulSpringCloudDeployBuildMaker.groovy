package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.AllCloudJobs
import org.springframework.jenkins.cloud.common.CustomJob

/**
 * @author Marcin Grzejszczak
 */
class ConsulSpringCloudDeployBuildMaker extends AbstractHashicorpDeployBuildMaker
		implements CustomJob {

	ConsulSpringCloudDeployBuildMaker(DslFactory dsl) {
		super(dsl, 'spring-cloud', 'spring-cloud-consul')
	}

	@Override
	protected String preStep() {
		return ""
	}

	@Override
	protected String postStep() {
		return ""
	}

	@Override
	String compileOnlyCommand() {
		return "./scripts/compileOnly.sh"
	}

	@Override
	String projectName() {
		return "spring-cloud-consul"
	}

	@Override
	boolean checkTests() {
		return true
	}
}
