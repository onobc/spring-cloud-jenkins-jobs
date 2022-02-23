package org.springframework.jenkins.cloud.common

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@CompileStatic
@EqualsAndHashCode
@ToString
class Project {

	enum BuildSystem { MAVEN, GRADLE, BOTH }

	// repo spring-cloud-build
	String repo;

	// name, defaults to repo
	String name

	// org default to spring-cloud, allow spring-projects-experimental
	String org = "spring-cloud"

	// boolean has tests?
	boolean hasTests = true

	// enum build system, maven, gradle, both
	BuildSystem buildSystem = BuildSystem.MAVEN

	// included in boot compatibility?
	boolean checkJdkCompatibility = true

	// included in boot compatibility?
	boolean checkBootCompatibility = true

	String getName() {
		return name ?: repo
	}

	// custom build stuff (branch param, jdk param)
		// see CustomJob
	List<String> customBuildCommand(BuildContext context) {
		return null;
	}
	String labelExpression(BuildContext context) {
		return null;
	}

	static void verify(Project project, BuildContext buildContext) {
		assert project.org?.trim() : "Project org is required"
		assert project.repo?.trim() : "Project repo is required"
		assert project.buildSystem : "Project buildSystgem is required"
		assert buildContext.jdk?.trim() : "Project jdk is required"
		assert buildContext.branch?.trim() : "Project branch is required"
	}

	/**
	 * Provides information to project methods unkown ahead of time.
	 */
	@CompileStatic
	static class BuildContext {
		boolean upload = true
		String branch
		String jdk
	}

}