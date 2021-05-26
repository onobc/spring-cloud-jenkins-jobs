package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.JmhPerformance

/**
 * @author Marcin Grzejszczak
 */
class SleuthBenchmarksBuildMaker implements JdkConfig, Cron {
	private final DslFactory dsl

	SleuthBenchmarksBuildMaker(DslFactory dsl) {
		this.dsl = dsl
	}

	void buildSleuth() {
		buildSleuth(oncePerDay())
	}

	void buildSleuth(String cronExpr) {
		dsl.job('spring-cloud-sleuth-benchmark-ci') {
			triggers {
				cron cronExpr
			}
			scm {
				git {
					remote {
						url "https://github.com/spring-cloud/spring-cloud-sleuth"
						branch "main"
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
				timeout {
					noActivity(300)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			jdk jdk8()
			steps {
				shell('''
				echo "Running JMH benchmark tests"
				./scripts/runJmhBenchmarks.sh
				''')
			}
			publishers {
				archiveArtifacts("**/jmh-result.csv")
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node)
			}
		}
	}
}
