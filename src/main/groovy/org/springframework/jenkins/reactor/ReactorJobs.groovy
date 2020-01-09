package org.springframework.jenkins.reactor

import org.springframework.jenkins.cloud.common.SpringCloudJobs

/**
 * @author Marcin Grzejszczak
 */
trait ReactorJobs extends SpringCloudJobs {

	String contextUrlEnvVar() {
		return "ORG_GRADLE_PROJECT_artifactory_publish_contextUrl"
	}

	String repoKeyEnvVar() {
		return "ORG_GRADLE_PROJECT_artifactory_publish_repoKey"
	}

	@Override
	String projectSuffix() {
		return 'reactor'
	}

	@Override
	void slackNotification(Node node) {
		ReactorNotification.reactorCiSlack(node)
	}

	@Override
	String repoUserNameEnvVar() {
		return 'ORG_GRADLE_PROJECT_artifactory_publish_username'
	}

	@Override
	String repoPasswordEnvVar() {
		return 'ORG_GRADLE_PROJECT_artifactory_publish_password'
	}
}