package org.springframework.jenkins.cloud.ci

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.ExternalStrategy
import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class SpringCloudDeployBuildMakerBuilder implements JdkConfig, TestPublisher, Cron,
		SpringCloudJobs, Maven {
	DslFactory dsl
	String organization
	String prefix
	String jdkVersion = jdk8()

	SpringCloudDeployBuildMakerBuilder dsl(DslFactory dsl) {
		this.dsl = dsl
		return this
	}

	SpringCloudDeployBuildMakerBuilder organization(String organization) {
		this.organization = organization
		return this
	}

	SpringCloudDeployBuildMakerBuilder prefix(String prefix) {
		this.prefix = prefix
		return this
	}

	SpringCloudDeployBuildMakerBuilder jdkVersion(String jdkVersion) {
		this.jdkVersion = jdkVersion
		return this
	}

	static SpringCloudDeployBuildMakerBuilder builder() {
		return new SpringCloudDeployBuildMakerBuilder()
	}

	SpringCloudDeployBuildMaker build() {
		def maker = new SpringCloudDeployBuildMaker(this.dsl, this.organization, this.prefix)
		if (this.jdkVersion) maker.jdkVersion = this.jdkVersion
		return maker
	}
}
