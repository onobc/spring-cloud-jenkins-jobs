package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.CloudCron
import org.springframework.jenkins.cloud.common.CustomJob
import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Spencer Gibb
 */
class SpringCloudKubernetesDeployBuildMaker implements JdkConfig, TestPublisher, CloudCron,
		SpringCloudJobs, Maven, CustomJob {
	private final DslFactory dsl
	final String organization
	final String repoName

	SpringCloudKubernetesDeployBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'spring-cloud'
		this.repoName = 'spring-cloud-kubernetes'
	}

	@Override
	void deploy() {
		doDeploy("${prefixJob(repoName)}-${masterBranch()}-ci", masterBranch())
	}

	@Override
	void deploy(String branchName) {
		doDeploy("${prefixJob(repoName)}-${branchName}-ci", branchName)
	}

	@Override
	String compileOnlyCommand() {
		return "./mvnw clean install -DskipTests -U -fae"
	}

	@Override
	String projectName() {
		return "spring-cloud-kubernetes"
	}

	@Override
	boolean checkTests() {
		return true
	}

	@Override
	void jdkBuild(String jdkVersion) {
		doDeploy("spring-cloud-${jdkVersion}-${projectName()}-${masterBranch()}-ci", masterBranch(), jdkVersion, false)
	}

	private void doDeploy(String projectName, String branchName, String jdkVersion = jdk8(), boolean deploy = true) {
		dsl.job(projectName) {
			triggers {
				cron cronValue
				if (onGithubPush) {
					githubPush()
				}
			}
			parameters {
				stringParam(branchVarName(), branchName, 'Which branch should be built')
			}
			jdk jdkVersion
			if (jdkVersion != jdk8()) {
				label(ubuntu18_04())
			}
			scm {
				git {
					remote {
						url "https://github.com/${organization}/${repoName}"
						branch branchVar()
					}
					extensions {
						wipeOutWorkspace()
						localBranch("**")
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
					usernamePassword(githubRepoUserNameEnvVar(),
							githubRepoPasswordEnvVar(),
							githubUserCredentialId())
					string(githubToken(), githubTokenCredId())
				}
				timeout {
					noActivity(600)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			steps {
				shell(loginToDocker())
				shell(removeMavenInstallation())
				shell(stopRunningDocker())
				maven {
					mavenInstallation(maven33())
					goals('--version')
				}
//				Build Spring Cloud Kubernetes without deploying to generate images etc
//				Then run integration tests
//				 After integration tests pass then deploy artifacts (skip tests since they were already run during the first build)
				shell("./mvnw clean install -Pspring -B -U")
				shell("""#!/bin/bash
	cd spring-cloud-kubernetes-integration-tests
    ./run.sh
""")
				shell(deploy ? cleanDeployWithDocs() + " -DskipTests=true" : cleanInstallWithoutDocs() + " -DskipTests=true")
				shell("""
				 ./mvnw dockerfile:push -pl :spring-cloud-kubernetes-configuration-watcher -Pdockerpush
""")
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node)
			}
			publishers {
				archiveJunit mavenJUnitResults()
			}
		}
	}
}
