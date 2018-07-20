package org.springframework.jenkins.cloud.ci

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.CustomJob

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class CustomJobFactory {
	private final Map<String, CustomJob> jobs = [:]
	private final DslFactory dsl
	private final String organization

	CustomJobFactory(DslFactory dsl) {
		this.dsl = dsl
		this.organization = "spring-cloud"
		this.jobs.putAll(
				[
						(consul().projectName()) : consul(),
						(build().projectName()) : build(),
						(contract().projectName()) : contract(),
						(netflix().projectName()) : netflix(),
						(vault().projectName()) : vault()
				]
		)
	}

	void deploy(String projectName, String branch = "") {
		CustomJob job = jobOrException(projectName)
		if (branch) {
			job.deploy(branch)
		} else {
			job.deploy()
		}
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

	private CustomJob consul() {
		return new ConsulSpringCloudDeployBuildMaker(dsl) {
			@Override
			boolean checkTests() {
				return false
			}
		}
	}

	private CustomJob build() {
		return new SpringCloudCustomJobDeployBuildMaker(dsl) {
			@Override
			String compileOnlyCommand() {
				return "./mvnw clean install -DskipTests"
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
		return new SpringCloudContractDeployBuildMaker(dsl) {
			@Override
			boolean checkTests() {
				return false
			}
		}
	}

	private CustomJob netflix() {
		return new SpringCloudNetflixDeployBuildMaker(dsl) {
			@Override
			boolean checkTests() {
				return false
			}
		}
	}

	private CustomJob vault() {
		return new VaultSpringCloudDeployBuildMaker(dsl) {
			@Override
			boolean checkTests() {
				return false
			}
		}
	}
}
