package org.springframework.jenkins.cloud.common

import groovy.transform.CompileStatic

import org.springframework.jenkins.common.job.Cron

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
trait CloudCron extends Cron {
	String cronValue = everyThreeHours()
	boolean onGithubPush = true
}