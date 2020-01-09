package org.springframework.jenkins.reactor

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.helpers.wrapper.CredentialsBindingContext
import javaposse.jobdsl.dsl.jobs.FreeStyleJob

import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseMaker

/**
 * @author Marcin Grzejszczak
 */
class ReactorMetaReleaseMaker extends SpringCloudMetaReleaseMaker implements ReactorJobs {

	ReactorMetaReleaseMaker(DslFactory dsl) {
		super(dsl)
	}

	@Override
	protected void additionalConfiguration(FreeStyleJob job) {
		job.parameters {
			stringParam(contextUrlEnvVar(), "https://repo.spring.io", "Artifactory Publish Context Url")
			choiceParam(repoKeyEnvVar(), ["libs-snapshot-local", "libs-milestone-local", "libs-release-local"], "Artifactory Publish Repo Key")
		}
	}

	@Override
	protected void additionalCredentials(CredentialsBindingContext context) {
		context.with {
			usernamePassword(repoUserNameEnvVar(),
					repoPasswordEnvVar(),
					repoSpringIoUserCredentialId())
		}
	}

	@Override
	protected String additionalEnvVars() {
		return """\
export ORG_GRADLE_PROJECT_releaserDryRun=\${${DRY_RUN_PARAM}}
"""
	}
}
