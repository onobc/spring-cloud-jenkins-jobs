package org.springframework.jenkins.cloud.qa

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.HashicorpTrait

/**
 * @author Marcin Grzejszczak
 */
class ConsulMutationBuildMaker extends MutationBuildMaker implements HashicorpTrait {

	ConsulMutationBuildMaker(DslFactory dsl) {
		super(dsl)
	}

	void build() {
		super.build('spring-cloud-consul')
	}

	@Override
	Closure defaultSteps() {
		return buildStep {
			shell postConsulShell()
		} << super.defaultSteps() <<  buildStep {
			shell preConsulShell()
		}
	}

	@Override
	protected String postAction() {
		return postConsulShell() + " exit 1"
	}
}
