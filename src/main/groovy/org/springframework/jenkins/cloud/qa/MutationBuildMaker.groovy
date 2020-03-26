package org.springframework.jenkins.cloud.qa

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.helpers.step.StepContext

import org.springframework.jenkins.cloud.common.PitestPublisher
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.cloud.common.TapPublisher
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.SonarTrait
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
class MutationBuildMaker implements JdkConfig, TestPublisher, SonarTrait, Cron {

	private final DslFactory dsl

	MutationBuildMaker(DslFactory dsl) {
		this.dsl = dsl
	}

	void build(String projectName) {
		build(projectName, everySunday())
	}

	void build(String projectName, String cronExpr) {
		dsl.job("$projectName-qa-mutation") {
			triggers {
				cron cronExpr
			}
			environmentVariables {
				env("CI", "jenkins")
			}
			scm {
				git {
					remote {
						url "https://github.com/spring-cloud/$projectName"
						branch 'master'
					}
					extensions {
						wipeOutWorkspace()
						localBranch("**")
					}
				}
			}
			jdk jdk8()
			steps defaultSteps()
			publishers {
				archiveArtifacts mavenJUnitResults()
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node)
				// PitestPublisher.cloudMutation(it as Node)
			}
		}
	}

	Closure defaultSteps() {
		return buildStep {
			shell("./mvnw clean verify -Pmutation org.pitest:pitest-maven:report-aggregate-module -U || ${postAction()}")
			shell('''\
ROOT_VIEW="Spring%20Cloud"
CURRENT_VIEW="QA"
INDEX_HTML="target/pit-reports/index.html"
echo "You can click here to see the PIT report [${JENKINS_URL}/view/${ROOT_VIEW}/view/${CURRENT_VIEW}/job/${JOB_NAME}/ws/${INDEX_HTML}]
''')
		}
	}

	protected Closure buildStep(@DelegatesTo(StepContext) Closure buildSteps) {
		return buildSteps
	}

	protected String postAction() {
		return 'exit 1'
	}
}
