package org.springframework.jenkins.cloud.compatibility

import javaposse.jobdsl.dsl.DslFactory
import org.springframework.jenkins.cloud.common.SpringCloudNotification

/**
 * @author Marcin Grzejszczak
 */
class BootCompatibilityBuildMaker extends CompatibilityBuildMaker {

	BootCompatibilityBuildMaker(DslFactory dsl) {
		super(dsl)
	}

	BootCompatibilityBuildMaker(DslFactory dsl, String suffix) {
		super(dsl, suffix)
	}

	BootCompatibilityBuildMaker(DslFactory dsl, String suffix, String organization) {
		super(dsl, suffix, organization)
	}

	protected void buildWithTests(String projectName, String repoName, String branchName, String cronExpr, boolean checkTests,
								  boolean parametrizedBoot = true) {
		String prefixedProjectName = prefixJob(projectName)
		dsl.job("${prefixedProjectName}-${suffix}") {
			concurrentBuild()
			if (parametrizedBoot) {
				parameters {
					stringParam(SPRING_BOOT_VERSION_VAR, DEFAULT_BOOT_MINOR_VERSION, 'Which version of Spring Boot should be used for the build')
					stringParam(SPRING_CLOUD_BUILD_BRANCH, DEFAULT_BUILD_BRANCH, 'Which branch of Spring Cloud Build should be checked out')
				}
			}
			triggers {
				if (cronExpr) {
					cron cronExpr
				}
			}
			jdk jdk8()
			scm {
				git {
					remote {
						url "https://github.com/${organization}/${repoName}"
						branch branchName
					}
					extensions {
						wipeOutWorkspace()
						localBranch("**")
					}
				}
			}
			steps {
				shell(loginToDocker())
				maven {
					mavenInstallation(maven33())
					goals('--version')
				}
			}
			steps checkTests ? defaultStepsWithTestsForBoot() : defaultStepsForBoot()
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

}
