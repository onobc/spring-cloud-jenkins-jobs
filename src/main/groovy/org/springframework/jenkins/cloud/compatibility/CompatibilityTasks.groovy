package org.springframework.jenkins.cloud.compatibility

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import javaposse.jobdsl.dsl.helpers.step.StepContext
import org.springframework.jenkins.cloud.common.AllCloudConstants
import org.springframework.jenkins.common.job.Maven

/**
 * @author Marcin Grzejszczak
 */
@PackageScope
@CompileStatic
abstract class CompatibilityTasks implements Maven {

	protected static final String DEFAULT_BOOT_VERSION = AllCloudConstants.LATEST_2_2_BOOT_VERSION
	protected static final String SPRING_BOOT_VERSION_VAR = 'SPRING_BOOT_VERSION'
	protected static final String SPRING_BOOT_MINOR = AllCloudConstants.BOOT_MINOR_FOR_API_COMPATIBILITY
	protected static final String SPRING_VERSION_VAR = 'SPRING_VERSION'
	protected static final String SPRING_CLOUD_BUILD_BRANCH = 'SPRING_CLOUD_BUILD_BRANCH'
	protected static final String DEFAULT_BUILD_BRANCH = AllCloudConstants.SPRING_CLOUD_BUILD_BRANCH_FOR_COMPATIBILITY_BUILD

	Closure defaultStepsForBoot() {
		return buildStep {
			shell compileProductionForBoot()
		}
	}

	protected String printDepsForBoot() {
		return """
					echo -e "Printing the list of dependencies"
					./mvnw dependency:tree -U -Dspring-boot.version=\$${SPRING_BOOT_VERSION_VAR}
			"""
	}

	Closure defaultStepsWithTestsForBoot(String branch = "master") {
		return buildStep {
			shell runTestsForBoot(branch)
		}
	}

	protected String compileProductionForBoot() {
		return """#!/bin/bash -x
					set -o errexit
					${fetchLatestBootVersion()}
					${bumpBoot()}
					echo -e "Checking if prod code compiles against latest boot"
					${buildCommand()}
					${printDepsForBoot()}
"""
	}

	protected String buildCommand() {
		return "./mvnw clean package -U -fae -Dspring-boot.version=\$${SPRING_BOOT_VERSION_VAR} -DskipTests"
	}

	protected String fetchLatestBootVersion() {
		return """
		echo -e "Getting latest version of Spring Boot"
		# Uncomment this to get latest version at all (not necessarily 2.0.x)
		#${SPRING_BOOT_VERSION_VAR}="\$( curl https://repo.spring.io/libs-snapshot-local/org/springframework/boot/spring-boot-starter/maven-metadata.xml | sed -ne '/<latest>/s#\\s*<[^>]*>\\s*##gp')"
		[[ -z "\$${SPRING_BOOT_VERSION_VAR}" ]] && ${SPRING_BOOT_VERSION_VAR}="\$( curl https://repo.spring.io/libs-snapshot-local/org/springframework/boot/spring-boot-starter/maven-metadata.xml | grep "<version>${SPRING_BOOT_MINOR}." | tail -1 | sed -ne '/<version>/s#\\s*<[^>]*>\\s*##gp')"
		echo -e "Latest version of boot is [\$${SPRING_BOOT_VERSION_VAR}]"
"""
	}

	protected String runTestsForBoot(String branch = "master") {
		return """#!/bin/bash -x
					set -o errexit
					${fetchLatestBootVersion()}
					git checkout ${branch}
					${bumpBoot()}
					echo -e "Checking if the project can be built with Boot version [\$${SPRING_BOOT_VERSION_VAR}]"
					./mvnw clean install -U -fae
					${printDepsForBoot()}
					"""
	}

	protected String bumpBoot() {
		return """
		echo "Removing stored spring-cloud-release-tools"
		rm -rf ~/.m2/repository/org/springframework/cloud/internal
		echo -e "Will:
		1)Download releaser
		2)Clone SC-Build
		3)Use releaser to bump boot for SC-Build
		4)Install new SC-Build locally
		5)Build the project"
		rm -rf target
		mkdir -p target
		export MAVEN_PATH=${mavenBin()}
		pushd target
			\${MAVEN_PATH}/mvn dependency:get -DremoteRepositories=https://repo.spring.io/libs-snapshot-local -Dartifact=org.springframework.cloud.internal:spring-cloud:1.0.0.BUILD-SNAPSHOT -Dtransitive=false
			\${MAVEN_PATH}/mvn dependency:copy -Dartifact=org.springframework.cloud.internal:spring-cloud:1.0.0.BUILD-SNAPSHOT -Dproject.basedir=../
			mv dependency/*.jar dependency/spring-cloud-1.0.0-BUILD-SNAPSHOT.jar
			echo "Cloning Spring Cloud Build"
			git clone https://github.com/spring-cloud/spring-cloud-build.git
			pushd spring-cloud-build
				${SPRING_CLOUD_BUILD_BRANCH}="\${${SPRING_CLOUD_BUILD_BRANCH}:-master}"
				git checkout "\$${SPRING_CLOUD_BUILD_BRANCH}"
				echo -e "Updating SC-Build's Boot version [\$${SPRING_BOOT_VERSION_VAR}]"
				java -jar ../dependency/spring-cloud-1.0.0-BUILD-SNAPSHOT.jar --releaser.git.fetch-versions-from-git=false --"releaser.fixed-versions[spring-boot-dependencies]=\$${SPRING_BOOT_VERSION_VAR}" --releaser.git.oauth-token="token" -u -i=false
				./mvnw clean install -fae -U
			popd
		popd
"""
	}

	/**
	 * Dirty hack cause Jenkins is not inserting Maven to path...
	 * Requires using Maven3 installation before calling
	 */
	String mavenBin() {
		return "/opt/jenkins/data/tools/hudson.tasks.Maven_MavenInstallation/maven33/apache-maven-3.3.9/bin/"
	}

	Closure defaultStepsForSpring() {
		return buildStep {
			shell compileProductionForSpring()
		}
	}

	protected String printDepsForSpring() {
		return """
					echo -e "Printing the list of dependencies"
					./mvnw dependency:tree -U -Dspring.version=\$${SPRING_VERSION_VAR}
			"""
	}

	Closure defaultStepsWithTestsForSpring() {
		return buildStep {
			shell runTestsForSpring()
		}
	}

	protected String compileProductionForSpring() {
		return """
					echo -e "Checking if prod code compiles against latest spring"
					./mvnw clean compile -U -fae -Dspring.version=\$${SPRING_VERSION_VAR}
					${printDepsForSpring()}
"""
	}

	protected String runTestsForSpring() {
		return """
					echo -e "Checking if prod code compiles against latest spring"
					./mvnw clean install -U -fae -Dspring.version=\$${SPRING_VERSION_VAR}
					${printDepsForSpring()}
"""
	}

	private Closure buildStep(@DelegatesTo(StepContext) Closure buildSteps) {
		return buildSteps
	}

}
