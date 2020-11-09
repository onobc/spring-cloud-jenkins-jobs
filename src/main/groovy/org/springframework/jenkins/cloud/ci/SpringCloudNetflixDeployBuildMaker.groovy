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
 * @author Marcin Grzejszczak
 */
class SpringCloudNetflixDeployBuildMaker implements JdkConfig, TestPublisher, CloudCron,
		SpringCloudJobs, Maven, CustomJob {
	private final DslFactory dsl
	final String organization
	final String repoName

	SpringCloudNetflixDeployBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'spring-cloud'
		this.repoName = 'spring-cloud-netflix'
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
		return "./scripts/compileOnly.sh"
	}

	@Override
	String projectName() {
		return "spring-cloud-netflix"
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
					usernamePassword(githubRepoUserNameEnvVar(),
							githubRepoPasswordEnvVar(),
							githubUserCredentialId())
					string(githubToken(), githubTokenCredId())
				}
				timeout {
					noActivity(300)
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
				shell(deploy ? cleanDeployWithDocs() : cleanInstallWithoutDocs())
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node)
			}
			publishers {
				archiveJunit mavenJUnitResults()
			}
		}
	}

	protected String firstBuildContractModule() {
		return """if [ -d "spring-cloud-netflix-hystrix-contract" ]; then cd spring-cloud-netflix-hystrix-contract && ../mvnw clean install deploy && cd ..; fi"""
	}
}
