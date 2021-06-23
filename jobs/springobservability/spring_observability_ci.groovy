package springobservability

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMakerBuilder

DslFactory dsl = this

// CI BUILDS
new SpringCloudDeployBuildMakerBuilder(dsl).with {
	organization("spring-projects")
	jdkVersion(jdk16())
	upload(true)
	cron(everyThreeHours())
	onGithubPush(true)
	prefix("spring-observability")
}.build().deploy("spring-observability")