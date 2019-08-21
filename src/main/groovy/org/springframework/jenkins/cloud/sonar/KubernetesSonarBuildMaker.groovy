package org.springframework.jenkins.cloud.sonar

import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Ryan Baxter
 */
class KubernetesSonarBuildMaker extends SonarBuildMaker {
	KubernetesSonarBuildMaker(DslFactory dsl) {
		super(dsl)
	}

	void buildSonar() {
		super.buildSonar('spring-cloud-kubernetes')
	}

	@Override
	Closure defaultSteps() {
		return buildStep {

		} << super.defaultSteps() <<  buildStep {

		}
	}
}
