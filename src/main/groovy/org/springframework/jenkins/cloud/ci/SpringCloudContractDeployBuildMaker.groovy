package org.springframework.jenkins.cloud.ci

import groovy.transform.CompileStatic

import org.springframework.jenkins.cloud.common.CustomJob
import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher
import javaposse.jobdsl.dsl.DslFactory
import org.springframework.jenkins.cloud.common.SpringCloudNotification

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class SpringCloudContractDeployBuildMaker implements JdkConfig, TestPublisher, Cron,
		SpringCloudJobs, Maven, CustomJob {
	private final DslFactory dsl
	final String organization
	final String projectName

	SpringCloudContractDeployBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'spring-cloud'
		this.projectName = 'spring-cloud-contract'
	}

	SpringCloudContractDeployBuildMaker(DslFactory dsl, String organization, String projectName = 'spring-cloud-contract') {
		this.dsl = dsl
		this.organization = organization
		this.projectName = projectName
	}

	@Override
	void deploy() {
		doDeploy("${prefixJob(projectName)}-${masterBranch()}-ci", this.projectName, masterBranch())
	}

	@Override
	void deploy(String branchName) {
		doDeploy("${prefixJob(projectName)}-${branchName}-ci", this.projectName, branchName)
	}

	@Override
	String compileOnlyCommand() {
		return "./scripts/compileOnly.sh"
	}

	@Override
	String projectName() {
		return "spring-cloud-contract"
	}

	@Override
	boolean checkTests() {
		return true
	}

	@Override
	void jdkBuild(String jdkVersion) {
		doDeploy("spring-cloud-${jdkVersion}-${prefixJob(projectName)}-${masterBranch()}-ci", this.projectName, masterBranch(), jdkVersion, false)
	}

	private void doDeploy(String projectName, String repoName, String branchName, String jdkVersion=jdk8(), boolean deploy = true) {
		dsl.job(projectName) {
			triggers {
				cron everyThreeHours()
				githubPush()
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
					}
					branch branchVar()
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
					usernamePassword(repoUserNameEnvVar(),
							repoPasswordEnvVar(),
							repoSpringIoUserCredentialId())
					usernamePassword(githubRepoUserNameEnvVar(),
							githubRepoPasswordEnvVar(),
							githubUserCredentialId())
					usernamePassword(gradlePublishKeyEnvVar(),
							gradlePublishSecretEnvVar(),
							pluginsGradleOrgUserCredentialId())
					string(githubToken(), githubTokenCredId())
				}
				timeout {
					noActivity(300)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			steps {
				shell("rm -rf /opt/jenkins/data/tools/hudson.tasks.Maven_MavenInstallation/maven33/")
				maven {
					mavenInstallation(maven33())
					goals('--version')
				}
				if (deploy) {
					shell(removeStubAndDeploy())
				}
				shell("""#!/bin/bash -x
					export MAVEN_PATH=${mavenBin()}
					${setupGitCredentials()}
					echo "Building Spring Cloud Contract docs"
					./scripts/generateDocs.sh
					${if (deploy) deployDocs() else cleanInstall() }
					${cleanGitCredentials()}
					""")
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node)
			}
			publishers {
				archiveJunit mavenJUnitResults()
				archiveJunit gradleJUnitResults()
			}
		}
	}
}
