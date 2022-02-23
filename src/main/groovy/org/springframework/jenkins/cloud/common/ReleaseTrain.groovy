package org.springframework.jenkins.cloud.common

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * TBD
 *
 * @author Spencer Gibb
 */
@CompileStatic
@EqualsAndHashCode
@ToString
class ReleaseTrain {

	// list of Projects
	// mapping of Project to branch
	LinkedHashMap<Project, String> projectsWithBranch = new LinkedHashMap<>()

	boolean active = true

	// version (calver 2022.0)
	// not just a string?
	String version

	// codename
	String codename

	// boot generation compatibility, first is baseline
	List<String> bootVersions = []

	// jdk compatibility, first is baseline
	List<String> jdks = []

	String jdkBaseline() {
		jdks.isEmpty() ? null : jdks[0]
	}

	Collection<Project> projects() {
		projectsWithBranch.keySet()
	}

	String releaseBranch() {
		projectsWithBranch[Projects.RELEASE]
	}

}