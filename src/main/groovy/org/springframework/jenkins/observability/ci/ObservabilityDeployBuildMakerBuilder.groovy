package org.springframework.jenkins.observability.ci

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMakerBuilder
import org.springframework.jenkins.cloud.common.CloudCron
import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class ObservabilityDeployBuildMakerBuilder extends SpringCloudDeployBuildMakerBuilder {
	String organization
	String prefix
	String jdkVersion = jdk16()
	boolean upload = true

	ObservabilityDeployBuildMakerBuilder(DslFactory dsl) {
		super(dsl)
		jdkVersion(jdk16())
		organization("spring-projects-experimental")
		prefix("spring-observability")
		slack({ Node node -> SpringCloudNotification.observabilitySlack(node) } as Closure<Node>)
	}
}
