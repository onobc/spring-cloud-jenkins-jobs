package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.AllCloudJobs

/**
 * @author Marcin Grzejszczak
 */
class ConsulSpringCloudDeployBuildMaker extends AbstractHashicorpDeployBuildMaker {

	ConsulSpringCloudDeployBuildMaker(DslFactory dsl) {
		super(dsl, 'spring-cloud', 'spring-cloud-consul')
	}

	@Override
	protected String preStep() {
		return preConsulShell()
	}

	@Override
	protected String postStep() {
		return postConsulShell()
	}

	@Override
	void deploy() {
		AllCloudJobs.CONSUL_BRANCHES.each {
			super.deploy(it)
		}
	}
}
