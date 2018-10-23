package org.springframework.jenkins.cloud.release

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.AllCloudConstants
import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
class SpringCloudMetaReleaseMaker implements JdkConfig, TestPublisher,
		SpringCloudJobs {
	private static final String RELEASER_CONFIG_PARAM = "RELEASER_CONFIG"
	private static final String START_FROM_PARAM = "START_FROM"
	private static final String TASK_NAMES_PARAM = "TASK_NAMES"

	private final DslFactory dsl

	SpringCloudMetaReleaseMaker(DslFactory dsl) {
		this.dsl = dsl
	}

	void release() {
		dsl.job("spring-cloud-meta-releaser") {
			parameters {
				textParam(RELEASER_CONFIG_PARAM, AllCloudConstants.DEFAULT_RELEASER_PROPERTIES_FILE_CONTENT, "Properties file used by the meta-releaser")
				stringParam(START_FROM_PARAM, "", "Project name from which you'd like to start the meta-release process. E.g. spring-cloud-sleuth")
				stringParam(TASK_NAMES_PARAM, "", "Comma separated list of project names. E.g. spring-cloud-sleuth,spring-cloud-contract")
			}
			jdk jdk8()
			scm {
				git {
					remote {
						url "https://github.com/spring-cloud/spring-cloud-release-tools"
						branch masterBranch()
					}
					extensions {
						wipeOutWorkspace()
					}
				}
			}
			label(releaserLabel())
			wrappers {
				timestamps()
				colorizeOutput()
				maskPasswords()
				credentialsBinding {
					usernamePassword(dockerhubUserNameEnvVar(),
							dockerhubPasswordEnvVar(),
							dockerhubCredentialId())
					usernamePassword(githubRepoUserNameEnvVar(),
							githubRepoPasswordEnvVar(),
							githubUserCredentialId())
					file(gpgSecRing(), "spring-signing-secring.gpg")
					file(gpgPubRing(), "spring-signing-pubring.gpg")
					string(gpgPassphrase(), "spring-gpg-passphrase")
					string(githubToken(), githubTokenCredId())
					usernamePassword(sonatypeUser(), sonatypePassword(),
							"oss-token")
				}
				timeout {
					noActivity(300)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			steps {
				// build the releaser
				shell("""#!/bin/bash
				mkdir -p target
				echo "Building the releaser. Please wait..."
				./mvnw clean install > "target/releaser.log"
				echo "Run the meta-releaser!"
				${setupGitCredentials()}
				rm -rf config && mkdir -p config && echo "\$${RELEASER_CONFIG_PARAM}" > config/releaser.properties
				set +x
				SYSTEM_PROPS="-Dgpg.secretKeyring="\$${gpgSecRing()}" -Dgpg.publicKeyring="\$${gpgPubRing()}" -Dgpg.passphrase="\$${gpgPassphrase()}" -DSONATYPE_USER="\$${sonatypeUser()}" -DSONATYPE_PASSWORD="\$${sonatypePassword()}""
				if [[ \${$START_FROM_PARAM} != "" ]]; then
					START_FROM_OPTS="--start-from '\${$START_FROM_PARAM}'"
				fi
				if [[ \${$TASK_NAMES_PARAM} != "" ]]; then
					TASK_NAMES_OPTS="--task-names '\${$TASK_NAMES_PARAM}'"
				fi
				echo "Start from opts [\${START_FROM_OPTS}], task names [\${TASK_NAMES_OPTS}]"
				java -Dreleaser.git.username="\$${githubRepoUserNameEnvVar()}" -Dreleaser.git.password="\$${githubRepoPasswordEnvVar()}" -jar spring-cloud-release-tools-spring/target/spring-cloud-release-tools-spring-1.0.0.BUILD-SNAPSHOT.jar --releaser.maven.wait-time-in-minutes=180 --spring.config.name=releaser --releaser.maven.system-properties="\${SYSTEM_PROPS}" --interactive=false --meta-release=true --full-release \${START_FROM_OPTS} \${TASK_NAMES_OPTS}|| exit 1
				${cleanGitCredentials()}
				""")
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node) {
					notifyFailure()
					notifySuccess()
					notifyUnstable()
					includeFailedTests(false)
					includeTestSummary(false)
				}
			}
			publishers {
				archiveJunit mavenJUnitResults()
			}
		}
	}

	private String gpgSecRing() {
		return 'FOO_SEC'
	}

	private String gpgPubRing() {
		return 'FOO_PUB'
	}

	private String gpgPassphrase() {
		return 'FOO_PASSPHRASE'
	}

	private String sonatypeUser() {
		return 'SONATYPE_USER'
	}

	private String sonatypePassword() {
		return 'SONATYPE_PASSWORD'
	}

	private String githubToken() {
		return 'RELEASER_GIT_OAUTH_TOKEN'
	}

	private String githubTokenCredId() {
		return '7b3ebbea-7001-479b-8578-b8c464dab973'
	}
}
