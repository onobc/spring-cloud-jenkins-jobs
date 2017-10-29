package org.springframework.jenkins.cloud.ci

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
class SpringCloudPipelinesBaseDeployBuildMaker implements JdkConfig, TestPublisher, Cron,
		SpringCloudJobs, Maven {
	private final DslFactory dsl
	final String organization
	final String project

	SpringCloudPipelinesBaseDeployBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'spring-cloud'
		this.project = "pipeline-base"
	}

	void deploy() {
		dsl.job("spring-cloud-${project}-${masterBranch()}-ci") {
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
				docker build . -t springcloud/pipeline-base
				docker tag springcloud/pipeline-base springcloud/pipeline-base:\${tagName}
				docker push springcloud/pipeline-base:latest
				docker push springcloud/pipeline-base:\${tagName}
				echo "Removing all local images"
				docker rmi -f springcloud/pipeline-base
				""")
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node)
			}
		}
	}
}
