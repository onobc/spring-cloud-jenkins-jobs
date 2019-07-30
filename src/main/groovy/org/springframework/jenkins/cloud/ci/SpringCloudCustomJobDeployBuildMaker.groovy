package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.CustomJob
import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher
/**
 * @author Marcin Grzejszczak
 */
abstract class SpringCloudCustomJobDeployBuildMaker implements JdkConfig, TestPublisher, Cron,
		SpringCloudJobs, Maven, CustomJob {
	private final DslFactory dsl
	final String organization
	private final SpringCloudDeployBuildMaker buildMaker

	SpringCloudCustomJobDeployBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'spring-cloud'
		this.buildMaker = new SpringCloudDeployBuildMaker(dsl, this.organization)
	}

	SpringCloudCustomJobDeployBuildMaker(DslFactory dsl, String organization) {
		this.dsl = dsl
		this.organization = organization
		this.buildMaker = new SpringCloudDeployBuildMaker(dsl, this.organization)
	}

	@Override
	void deploy() {
		this.buildMaker.deploy(projectName(), checkTests())
	}

	@Override
	void deploy(String branch) {
		this.buildMaker.deploy(projectName(), branch, checkTests())
	}

	@Override
	void jdkBuild(String jdkVersion) {
		def maker = new SpringCloudDeployBuildMaker(dsl, this.organization, "spring-cloud-${jdkVersion}")
		maker.jdkVersion = jdkVersion
		maker.deploy = false
		maker.onGithubPush = false
		maker.cronValue = oncePerDay()
		maker.deploy(projectName(), checkTests())
	}
}
