package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
class SpringCloudDeployBuildMaker implements JdkConfig, TestPublisher, Cron,
		SpringCloudJobs, Maven {
	private final DslFactory dsl
	final String organization
	final String prefix

	SpringCloudDeployBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'spring-cloud'
		this.prefix = ""
	}

	SpringCloudDeployBuildMaker(DslFactory dsl, String organization) {
		this.dsl = dsl
		this.organization = organization
		this.prefix = ""
	}

	SpringCloudDeployBuildMaker(DslFactory dsl, String organization, String prefix) {
		this.dsl = dsl
		this.organization = organization
		this.prefix = prefix
	}

	static SpringCloudDeployBuildMaker cloudPipelines(DslFactory dsl) {
		return new SpringCloudDeployBuildMaker(dsl, "CloudPipelines", "cloudpipelines")
	}

	void deploy(String project, boolean checkTests = true) {
		deploy(project, masterBranch(), checkTests)
	}

	private String prefix(String project) {
		if (this.prefix) {
			return this.prefix.endsWith("-") ? this.prefix : this.prefix + "-"
		}
		return project.startsWith("spring-cloud-") ? "" : "spring-cloud-"
	}

	void deploy(String project, String branchToBuild, boolean checkTests = true) {
		String projectNameWithBranch = branchToBuild ? "$branchToBuild-" : ''
		String prefixedName = prefix(project) + project
		dsl.job("${prefixedName}-${projectNameWithBranch}ci") {
			triggers {
				cron everyThreeHours()
				githubPush()
			}
			parameters {
				stringParam(branchVarName(), branchToBuild ?: masterBranch(), 'Which branch should be built')
			}
			jdk jdk8()
			scm {
				git {
					remote {
						url "https://github.com/${organization}/${project}"
						branch "\$${branchVarName()}"
					}
					extensions {
						wipeOutWorkspace()
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
				shell(buildDocsWithGhPages())
				shell(cleanAndDeploy())
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node)
			}
			if (checkTests) {
				publishers {
					archiveJunit mavenJUnitResults()
				}
			}
		}
	}

	void deployWithoutTests(String project) {
		deploy(project, false)
	}
}
