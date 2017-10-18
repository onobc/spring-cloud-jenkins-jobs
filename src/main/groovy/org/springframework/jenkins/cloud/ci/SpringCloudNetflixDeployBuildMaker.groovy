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
class SpringCloudNetflixDeployBuildMaker implements JdkConfig, TestPublisher, Cron,
		SpringCloudJobs, Maven {
	private final DslFactory dsl
	final String organization
	final String repoName

	SpringCloudNetflixDeployBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'spring-cloud'
		this.repoName = "spring-cloud-netflix"
	}

	void deploy() {
		doDeploy("${prefixJob(repoName)}-${masterBranch()}-ci", masterBranch())
	}

	void deploy(String branchName) {
		doDeploy("${prefixJob(repoName)}-${branchName}-ci", branchName)
	}

	void branch() {
		doDeploy("${prefixJob(repoName)}-branch-ci", masterBranch(), false)
	}

	private void doDeploy(String projectName, String branchName, boolean trigger = true) {
		dsl.job(projectName) {
			if (trigger) {
				triggers {
					cron everyThreeHours()
					githubPush()
				}
			}
			parameters {
				stringParam(branchVarName(), branchName, 'Which branch should be built')
			}
			jdk jdk8()
			scm {
				git {
					remote {
						url "https://github.com/${organization}/${repoName}"
						branch branchVar()
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
				shell("""
						if [ -d "spring-cloud-netflix-hystrix-contract" ]; then cd spring-cloud-netflix-hystrix-contract && ../mvnw clean install && cd ..; fi
						./mvnw clean deploy -nsu -P docs,integration -U \$MVN_LOCAL_OPTS -Dmaven.test.redirectTestOutputToFile=true -Dsurefire.runOrder=random
				""")
				shell(buildDocsWithGhPages())
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
