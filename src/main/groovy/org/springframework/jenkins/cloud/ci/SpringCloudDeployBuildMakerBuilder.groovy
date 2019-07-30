package org.springframework.jenkins.cloud.ci

import groovy.transform.CompileStatic
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
	private final DslFactory dsl
	String organization
	String prefix
	String jdkVersion = jdk8()
	boolean deploy = true
	boolean upload = true
	String cronValue = everyThreeHours()
	boolean onGithubPush = true

	SpringCloudDeployBuildMakerBuilder(DslFactory dsl) {
		this.dsl = dsl
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

	SpringCloudDeployBuildMakerBuilder deploy(boolean deploy) {
		this.deploy = deploy
		return this
	}

	SpringCloudDeployBuildMakerBuilder upload(boolean upload) {
		this.upload = upload
		return this
	}

	SpringCloudDeployBuildMakerBuilder cron(String cron) {
		this.cronValue = cron
		return this
	}

	SpringCloudDeployBuildMakerBuilder onGithubPush(boolean onGithubPush) {
		this.onGithubPush = onGithubPush
		return this
	}

	SpringCloudDeployBuildMaker build() {
		def maker = new SpringCloudDeployBuildMaker(this.dsl, this.organization, this.prefix)
		if (this.jdkVersion) maker.jdkVersion = this.jdkVersion
		if (this.upload) maker.upload = this.upload
		maker.deploy = this.deploy
		maker.cronValue = this.cronValue
		maker.onGithubPush = this.onGithubPush
		return maker
	}
}
