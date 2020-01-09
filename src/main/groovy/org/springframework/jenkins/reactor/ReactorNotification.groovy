package org.springframework.jenkins.reactor

import org.springframework.jenkins.common.job.Slack
import org.springframework.jenkins.common.job.SlackPlugin

/**
 *
 * @author Marcin Grzejszczak
 */
class ReactorNotification {

	public static final String ROOM = "reactor-ci"

	static Slack reactorCiSlack(Node rootNode) {
		return SlackPlugin.slackNotification(rootNode) {
			room(ROOM)
			notifySuccess(false)
			notifyAborted(false)
			notifyNotBuilt(false)
			notifyUnstable(true)
			notifyRegression(false)
			notifyFailure(false)
			notifyBackToNormal(false)
			notifyRepeatedFailure(true)
			includeTestSummary(true)
			includeFailedTests(true)
		}
	}
}
