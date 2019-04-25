package org.springframework.jenkins.cloud.common

import org.springframework.jenkins.common.job.BuildAndDeploy

/**
 * @author Marcin Grzejszczak
 */
trait SpringCloudJobs extends BuildAndDeploy {

	@Override
	String projectSuffix() {
		return 'spring-cloud'
	}

	String releaserLabel() {
		return "releaser"
	}

	String ubuntu18_04() {
		return "ubuntu1804"
	}

	String openJdk7() {
		return "linux&&jdk7"
	}

	String setupGitCredentials() {
		return """
					set +x
					git config user.name "${githubUserName()}"
					git config user.email "${githubEmail()}"
					git config credential.helper "store --file=/tmp/gitcredentials"
					echo "https://\$${githubRepoUserNameEnvVar()}:\$${githubRepoPasswordEnvVar()}@github.com" > /tmp/gitcredentials
					export SPRING_CLOUD_STATIC_REPO="https://\$${githubRepoUserNameEnvVar()}:\$${githubRepoPasswordEnvVar()}@github.com/spring-cloud/spring-cloud-static.git"
					trap "{ rm -f /tmp/gitcredentials; }" EXIT
					set -x
				"""
	} 

	String buildDocsWithGhPages(String additionalCommand = "") {
		return """#!/bin/bash -x
					git checkout \$${branchVarName()} && git pull
					export MAVEN_PATH=${mavenBin()}
					${setupGitCredentials()}
					${(additionalCommand ? "${additionalCommand}\n" : "") + buildDocs()}
					echo "Downloading ghpages script from Spring Cloud Build"
					mkdir -p target
					rm -rf target/ghpages.sh
					curl https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/master/docs/src/main/asciidoc/ghpages.sh -o target/ghpages.sh
					chmod +x target/ghpages.sh
					. ./target/ghpages.sh && ${cleanGitCredentials()} || ${cleanGitCredentials()}
					"""
	}

	String removeMavenInstallation() {
		return "rm -rf /opt/jenkins/data/tools/hudson.tasks.Maven_MavenInstallation/maven33/"
	}

	String stopRunningDocker() {
		return """#!/bin/bash
if [ -n "\$(type -t timeout)" ]; then timeout 10s docker ps -a -q | xargs -n 1 -P 8 -I {} docker stop {} || echo "Failed to stop docker... Hopefully you know what you're doing"; fi
if [ -n "\$(type gtimeout)" ]; then gtimeout 10s docker ps -a -q | xargs -n 1 -P 8 -I {} docker stop {} || echo "Failed to stop docker... Hopefully you know what you're doing"; fi
"""
	}

	/**
	 * Dirty hack cause Jenkins is not inserting Maven to path...
	 * Requires using Maven3 installation before calling
	 */
	String mavenBin() {
		return "/opt/jenkins/data/tools/hudson.tasks.Maven_MavenInstallation/maven33/apache-maven-3.3.9/bin/"
	}

	String cleanGitCredentials() {
		return "rm -rf /tmp/gitcredentials"
	}

	String cleanInstall() {
		return "./mvnw clean install -U -Pintegration"
	}

	String buildDocs() {
		return '''./mvnw clean install -P docs -q -U -DskipTests=true -Dmaven.test.redirectTestOutputToFile=true'''
	}

	String buildDocsWithoutCleaning() {
		return '''./mvnw install -P docs -q -U -DskipTests=true -Dmaven.test.redirectTestOutputToFile=true'''
	}

	String repoUserNameEnvVar() {
		return 'REPO_USERNAME'
	}

	String repoPasswordEnvVar() {
		return 'REPO_PASSWORD'
	}

	String githubRepoUserNameEnvVar() {
		return 'GITHUB_REPO_USERNAME'
	}

	String githubRepoPasswordEnvVar() {
		return 'GITHUB_REPO_PASSWORD'
	}

	String gradlePublishKeyEnvVar() {
		return 'GRADLE_PUBLISH_KEY'
	}

	String gradlePublishSecretEnvVar() {
		return 'GRADLE_PUBLISH_SECRET'
	}

	String pluginsGradleOrgUserCredentialId() {
		return 'plugins-gradle-org'
	}

	String repoSpringIoUserCredentialId() {
		return '02bd1690-b54f-4c9f-819d-a77cb7a9822c'
	}

	String githubUserCredentialId() {
		return '3a20bcaa-d8ad-48e3-901d-9fbc941376ee'
	}
	
	String dockerhubUserNameEnvVar() {
		return 'DOCKER_HUB_USERNAME'
	}

	String dockerhubPasswordEnvVar() {
		return 'DOCKER_HUB_PASSWORD'
	}

	String dockerhubCredentialId() {
		return 'hub.docker.com-springbuildmaster'
	}

	String githubUserName() {
		return 'spring-buildmaster'
	}

	String githubEmail() {
		return 'buildmaster@springframework.org'
	}

	String gpgSecRing() {
		return 'FOO_SEC'
	}

	String gpgPubRing() {
		return 'FOO_PUB'
	}

	String gpgPassphrase() {
		return 'FOO_PASSPHRASE'
	}

	String sonatypeUser() {
		return 'SONATYPE_USER'
	}

	String sonatypePassword() {
		return 'SONATYPE_PASSWORD'
	}

	String githubToken() {
		return 'RELEASER_GIT_OAUTH_TOKEN'
	}

	String githubTokenCredId() {
		return '7b3ebbea-7001-479b-8578-b8c464dab973'
	}

}