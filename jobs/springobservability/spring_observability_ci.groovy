package springobservability

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.observability.ci.ObservabilityDeployBuildMakerBuilder

DslFactory dsl = this

// CI BUILDS
new ObservabilityDeployBuildMakerBuilder(dsl).with {
	upload(true)
	cron(everyThreeHours())
	onGithubPush(true)
}.build().deploy("spring-observability")