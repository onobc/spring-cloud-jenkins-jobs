package org.springframework.jenkins.cloud.ci

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.CustomJob
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class CustomJobFactory implements JdkConfig, Cron {
	private final Map<String, CustomJob> jobs = [:]
	private final DslFactory dsl
	private final String organization

	CustomJobFactory(DslFactory dsl) {
		this.dsl = dsl
		this.organization = "spring-cloud"
		this.jobs.putAll(
				[
						(build().projectName())   : build(),
						(contract().projectName()): contract(),
						(netflix().projectName()) : netflix(),
						(vault().projectName())   : vault(),
						(kubernetes().projectName()) : kubernetes()
				]
		)
	}

	void deploy(String projectName, String branch = "") {
		CustomJob job = jobOrException(projectName)
		job.onGithubPush = true
		job.cronValue = everyThreeHours()
		if (branch) {
			job.deploy(branch)
		}
		else {
			job.deploy()
		}
	}

	void deployWithJdk(String projectName, String jdkVersion, String branch = "") {
		CustomJob job = jobOrException(projectName)
		job.onGithubPush = true
		job.cronValue = everyThreeHours()
		if (branch) {
			job.deploy(branch)
		}
		else {
			job.deploy()
		}
		job.jdkBuild(jdkVersion)
	}

	void jdkVersion(String projectName, String jdkVersion) {
		CustomJob job = jobOrException(projectName)
		job.onGithubPush = false
		job.cronValue = oncePerDay()
		job.jdkBuild(jdkVersion)
	}

	private CustomJob jobOrException(String projectName) {
		CustomJob job = this.jobs[projectName]
		if (job == null) {
			throw new IllegalStateException("No job [${projectName}] found. Available jobs ${this.jobs.keySet()}")
		}
		return job
	}

	String compileOnlyCommand(String projectName) {
		return jobOrException(projectName).compileOnlyCommand()
	}

	private CustomJob build() {
		return new SpringCloudCustomJobDeployBuildMaker(dsl) {
			@Override
			String compileOnlyCommand() {
				return cleanInstall()
			}

			@Override
			String projectName() {
				return "spring-cloud-build"
			}

			@Override
			boolean checkTests() {
				return false
			}
		}
	}

	private CustomJob contract() {
		SpringCloudContractDeployBuildMaker maker = new SpringCloudContractDeployBuildMaker(dsl) {
			@Override
			boolean checkTests() {
				return false
			}
		}
		maker.onGithubPush = false
		maker.cronValue = oncePerDay()
		return maker
	}

	private CustomJob netflix() {
		SpringCloudNetflixDeployBuildMaker maker = new SpringCloudNetflixDeployBuildMaker(dsl) {
			@Override
			boolean checkTests() {
				return false
			}
		}
		maker.cronValue = oncePerDay()
		maker.onGithubPush = false
		return maker
	}

	private CustomJob kubernetes() {
		SpringCloudKubernetesDeployBuildMaker maker = new SpringCloudKubernetesDeployBuildMaker(dsl) {
			@Override
			boolean checkTests() {
				return false
			}
		}
		maker.cronValue = oncePerDay()
		maker.onGithubPush = false
		return maker
	}

	private CustomJob vault() {
		VaultSpringCloudDeployBuildMaker maker = new VaultSpringCloudDeployBuildMaker(dsl) {
			@Override
			boolean checkTests() {
				return false
			}
		}
		maker.cronValue = oncePerDay()
		maker.onGithubPush = false
		return maker
	}
}
