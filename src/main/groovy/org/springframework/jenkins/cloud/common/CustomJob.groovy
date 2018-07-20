package org.springframework.jenkins.cloud.common

/**
 * @author Marcin Grzejszczak
 */
interface CustomJob {
	void deploy()
	void deploy(String branch)
	String compileOnlyCommand()
	String projectName()
	boolean checkTests()
}