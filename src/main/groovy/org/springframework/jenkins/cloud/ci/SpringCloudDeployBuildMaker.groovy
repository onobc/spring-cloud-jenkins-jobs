package org.springframework.jenkins.cloud.ci


import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.AllCloudJobs
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
	boolean deploy = true
	boolean upload = true
	String jdkVersion = jdk8()

	SpringCloudDeployBuildMaker(DslFactory dsl) {
		this(dsl, 'spring-cloud', '')
	}

	SpringCloudDeployBuildMaker(DslFactory dsl, String organization) {
		this(dsl, organization, '')
	}

	SpringCloudDeployBuildMaker(DslFactory dsl, String organization, String prefix) {
		this.dsl = dsl
		this.organization = organization ?: 'spring-cloud'
		this.prefix = prefix ?: ''
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
			if (jdkVersion != jdk8()) {
				label(ubuntu18_04())
			}
			jdk jdkVersion
			scm {
				git {
					remote {
						url "https://github.com/${organization}/${project}"
					}
					branch "\$${branchVarName()}"
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
					string(githubToken(), githubTokenCredId())
				}
				timeout {
					noActivity(300)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			steps {
				shell(removeMavenInstallation())
				shell(stopRunningDocker())
				maven {
					mavenInstallation(maven33())
					goals('--version')
				}
				if (upload) shell(buildDocsWithGhPages())
				shell(buildCommand())
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node)
			}
			if (checkTests) {
				publishers {
					archiveJunit mavenJUnitResults()
				}
			}
			List<String> emails = AllCloudJobs.EMAIL_NOTIFICATIONS.get(project)
			if (emails) {
				publishers {
					mailer(emails.join(","), false, true)
				}
			}
		}
	}

	String buildCommand() {
		return this.deploy ? cleanAndDeploy() : cleanInstall()
	}

	void deployWithoutTests(String project) {
		deploy(project, false)
	}
}
