package org.springframework.jenkins.cloud.release


import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.TestPublisher
/**
 * @author Marcin Grzejszczak
 */
class SpringCloudMetaReleaseRepoPurger implements SpringCloudJobs {

	private final DslFactory dsl

	SpringCloudMetaReleaseRepoPurger(DslFactory dsl) {
		this.dsl = dsl
	}

	void build() {
		dsl.job(projectSuffix() + "-purge-worker-artifacts-releaser") {
			label(releaserLabel())
			wrappers {
				timestamps()
				colorizeOutput()
				maskPasswords()
				timeout {
					noActivity(600)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			steps {
				shell('''#!/bin/bash
				set -o errexit
				artifactsFolder="~/.m2/repository/org/springframework/cloud/"
				echo "Purging the [${artifactsFolder}] folder"
				rm -rf "${artifactsFolder}"
				''')
			}
			configure {
				slackNotification(it as Node)
			}
		}
	}

}
