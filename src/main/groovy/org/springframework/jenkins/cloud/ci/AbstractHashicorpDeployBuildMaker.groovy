package org.springframework.jenkins.cloud.ci

import groovy.transform.PackageScope

import org.springframework.jenkins.cloud.common.CustomJob
import org.springframework.jenkins.cloud.common.HashicorpTrait
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
@PackageScope
abstract class AbstractHashicorpDeployBuildMaker implements JdkConfig, TestPublisher, HashicorpTrait,
		Cron, SpringCloudJobs, Maven, CustomJob {
	protected final DslFactory dsl
	protected final String organization
	protected final String project

	AbstractHashicorpDeployBuildMaker(DslFactory dsl, String organization, String project) {
		this.dsl = dsl
		this.organization = organization
		this.project = project
	}

	@Override
	void deploy(String branchName = 'master') {
		dsl.job("$project-$branchName-ci") {
			triggers {
				cron everyThreeHours()
				githubPush()
			}
			jdk(jdkVersion(branchName))
			label(openJdk7())
			scm {
				git {
					remote {
						url "https://github.com/${organization}/${project}"
						branch branchName
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
				environmentVariables {
					env('BRANCH', branchName)
				}
				timeout {
					noActivity(300)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			steps {
				maven {
					mavenInstallation(maven33())
					goals('--version')
				}
				shell(antiPermgenAndJava7TlsHack() + "\n" + buildDocsWithGhPages())
				shell("""\
						${antiPermgenAndJava7TlsHack()}
						${preStep()}
						trap "{ ${postStep()} }" EXIT
						${cleanAndDeploy()}
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

	protected String antiPermgenAndJava7TlsHack() {
		return '#!/bin/bash -x\nexport MAVEN_OPTS="-Xms256M -Xmx1024M -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -XX:+UseConcMarkSweepGC -XX:+CMSPermGenSweepingEnabled -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=4096M -Dhttps.protocols=TLSv1.2"'
	}

	protected String jdkVersion(String branchName) {
        //TODO: better matching. All 1.*.x branches are jdk7
		return branchName.startsWith('1.') && branchName.endsWith('.x') ? jdk7() : jdk8()
	}

	protected abstract String preStep()
	protected abstract String postStep()
}
