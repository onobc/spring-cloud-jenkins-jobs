package org.springframework.jenkins.cloud.e2e

import org.springframework.jenkins.common.job.JdkConfig
import javaposse.jobdsl.dsl.DslFactory
import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
class SpringCloudSamplesTestsBuildMaker implements TestPublisher,
		JdkConfig, BreweryDefaults, Cron, SpringCloudJobs {

	private final DslFactory dsl
	private final String organization

	SpringCloudSamplesTestsBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = "spring-cloud-samples"
	}

	SpringCloudSamplesTestsBuildMaker(DslFactory dsl, String organization) {
		this.dsl = dsl
		this.organization = organization
	}

	void buildForGreenwich() {
		build("Greenwich.BUILD-SNAPSHOT", "tests", everySixHours(), "Greenwich")
	}

	void buildForHoxton() {
		build("Hoxton.BUILD-SNAPSHOT", "tests", everySixHours(), masterBranch())
	}

	void buildForGreenwichWithJdk(String jdk) {
		build("Greenwich.BUILD-SNAPSHOT", "tests-greenwich-${jdk}", everySixHours(), "Greenwich", jdk)
	}

	void buildForHoxtonWithJdk(String jdk) {
		build("Hoxton.BUILD-SNAPSHOT", "tests-${jdk}", everySixHours(), masterBranch(), jdk)
	}

	private void build(String cloudTrainVersion, String projectName, String cronExpr = everySixHours(),
					   String branchName = masterBranch(), String jdkVersion = jdk8()) {
		String organization = this.organization
		dsl.job("${prefixJob(projectName)}-${branchName}-e2e") {
			triggers {
				cron cronExpr
			}
			jdk jdkVersion
			wrappers {
				timestamps()
				colorizeOutput()
				timeout {
					noActivity(defaultInactivity())
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			scm {
				git {
					remote {
						url "https://github.com/${organization}/tests"
						branch branchName
					}
					extensions {
						wipeOutWorkspace()
						localBranch("**")
					}
				}
			}
			steps {
				shell("""#!/bin/bash
						echo "Current java version"
						java -version
						echo "Running the build with cloud version [${cloudTrainVersion}]"
						./mvnw --fail-at-end clean package -Dspring-cloud.version=${cloudTrainVersion} -U
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
