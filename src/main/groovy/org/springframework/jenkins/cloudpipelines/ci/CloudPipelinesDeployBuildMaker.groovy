package org.springframework.jenkins.cloudpipelines.ci

import org.springframework.jenkins.cloud.common.TapPublisher
import org.springframework.jenkins.common.job.JdkConfig
import javaposse.jobdsl.dsl.DslFactory
import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
class CloudPipelinesDeployBuildMaker implements JdkConfig, TestPublisher, Cron,
		SpringCloudJobs, Maven {
	private final DslFactory dsl
	final String organization
	final String project

	CloudPipelinesDeployBuildMaker(DslFactory dsl, String project) {
		this.dsl = dsl
		this.organization = 'CloudPipelines'
		this.project = project
	}

	void deploy() {
		dsl.job("cloudpipelines-${this.project}-${masterBranch()}-ci") {
			triggers {
				cron everyThreeHours()
				githubPush()
			}
			jdk jdk8()
			scm {
				git {
					remote {
						url "https://github.com/${organization}/${project}"
						branch masterBranch()
					}
					extensions {
						wipeOutWorkspace()
						submoduleOptions {
							recursive()
						}
					}
				}
			}
			wrappers {
				timestamps()
				colorizeOutput()
				maskPasswords()
				credentialsBinding {
					usernamePassword(githubRepoUserNameEnvVar(),
							githubRepoPasswordEnvVar(),
							githubUserCredentialId())
					usernamePassword(dockerhubUserNameEnvVar(),
							dockerhubPasswordEnvVar(),
							dockerhubCredentialId())
					string(githubToken(), githubTokenCredId())
				}
				timeout {
					noActivity(300)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			steps {
				shell(buildWithDocs())
			}
			configure {
				SpringCloudNotification.cloudPipelinesSlack(it as Node)
				TapPublisher.cloudTap(it as Node) {
					failIfNoResults(false)
				}
			}
			publishers {
				archiveJunit(gradleJUnitResults()) {
					allowEmptyResults()
				}
			}
		}
	}

	private String buildWithDocs() {
		return """#!/bin/bash -x
					${setupGitCredentials()}
					${setOrigin()}
					${checkoutMaster()}
					${build()} || exit 1 
					${syncDocs()} || echo "Failed to sync docs"
					${cleanGitCredentials()}
					"""
	}

	private String setOrigin() {
		return "git remote set-url --push origin `git config remote.origin.url | sed -e 's/^git:/https:/'`"
	}

	private String checkoutMaster() {
		return "git checkout master && git pull origin master"
	}

	private String build() {
		return "./gradlew clean build generateDocs -PskipDist -PreleaseDocker"
	}

	private String syncDocs() {
		return """git commit -a -m "Sync docs" && git push origin ${masterBranch()}"""
	}

	private String buildNumber() {
		return '${BUILD_NUMBER}'
	}
}
