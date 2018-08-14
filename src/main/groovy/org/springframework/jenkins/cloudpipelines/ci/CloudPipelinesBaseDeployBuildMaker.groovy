package org.springframework.jenkins.cloudpipelines.ci

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.cloud.common.TapPublisher
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
class CloudPipelinesBaseDeployBuildMaker implements JdkConfig, TestPublisher, Cron,
		SpringCloudJobs, Maven {
	private final DslFactory dsl
	final String organization
	final String project

	CloudPipelinesBaseDeployBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'CloudPipelines'
		this.project = "pipeline-base"
	}

	void deploy() {
		dsl.job("cloudpipelines-${project}-${masterBranch()}-ci") {
			triggers {
				githubPush()
			}
			parameters {
				stringParam("tagName", "latest", 'Which tag to use')
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
					usernamePassword(dockerhubUserNameEnvVar(),
							dockerhubPasswordEnvVar(),
							dockerhubCredentialId())
				}
				timeout {
					noActivity(300)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			steps {
				shell("""#!/bin/bash
				echo "Deploying image to DockerHub"
				docker login -u \$${dockerhubUserNameEnvVar()} -p \$${dockerhubPasswordEnvVar()}
				echo "Docker images"
				docker images
				echo "Building image"
				docker build . -t cloudpipelines/pipeline-base
				docker tag cloudpipelines/pipeline-base springcloud/pipeline-base:\${tagName}
				docker push cloudpipelines/pipeline-base:\${tagName}
				docker push cloudpipelines/pipeline-base:latest
				echo "Removing all local images"
				docker rmi -f cloudpipelines/pipeline-base
				""")
			}
			configure {
				SpringCloudNotification.cloudPipelinesSlack(it as Node)
			}
		}
	}
}
