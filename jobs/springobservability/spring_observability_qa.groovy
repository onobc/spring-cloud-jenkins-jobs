package springobservability

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.qa.MutationBuildMaker

DslFactory dsl = this

new MutationBuildMaker(dsl, "spring-projects-experimental").with {
	rootView("Observability")
	jdk(jdk16())
}.build("spring-observability")
