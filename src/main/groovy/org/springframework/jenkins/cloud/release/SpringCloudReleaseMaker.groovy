package org.springframework.jenkins.cloud.release

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
class SpringCloudReleaseMaker implements JdkConfig, TestPublisher,
		SpringCloudJobs {
	private static final String RELEASER_POM_BRANCH_VAR = "RELEASER_POM_BRANCH"
	private static final String RELEASER_ADDITIONAL_PROPS_VAR = "RELEASER_ADDITIONAL_PROPS"
	private static final String RELEASER_SAGAN_UPDATE_VAR= 'RELEASER_SAGAN_UPDATE'
	private static final String RELEASER_RELEASE_TRAIN_PROJECT_NAME_VAR = 'RELEASER_META_RELEASE_RELEASE_TRAIN_PROJECT_NAME'
	private static final String RELEASER_GIT_RELEASE_TRAIN_BOM_URL_VAR= 'RELEASER_GIT_RELEASE_TRAIN_BOM'
	private static final String RELEASER_POM_THIS_TRAIN_BOM_VAR = 'RELEASER_POM_THIS_TRAIN'
	private static final String RELEASER_POST_RELEASE_ONLY_VAR= 'RELEASER_POST_RELEASE_ONLY'

	private final DslFactory dsl
	final String organization

	SpringCloudReleaseMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'spring-cloud'
	}

	SpringCloudReleaseMaker(DslFactory dsl, String organization) {
		this.dsl = dsl
		this.organization = organization
	}

	void release(String project, ReleaserOptions options = new ReleaserOptions()) {
		dsl.job("$project-releaser") {
			parameters {
				stringParam(branchVarName(), masterBranch(), "Your project's branch")
				stringParam(RELEASER_POM_BRANCH_VAR, masterBranch(), 'Spring Cloud Release branch')
				stringParam(RELEASER_ADDITIONAL_PROPS_VAR, '', 'Additional system properties')
				stringParam(RELEASER_RELEASE_TRAIN_PROJECT_NAME_VAR, options.releaseTrainProjectName, 'Name of the project that represents the BOM of the release train')
				stringParam(RELEASER_GIT_RELEASE_TRAIN_BOM_URL_VAR, options.releaseTrainBomUrl, 'Subfolder of the pom that contains the versions for the release train')
				stringParam(RELEASER_POM_THIS_TRAIN_BOM_VAR, options.releaseThisTrainBom, 'URL to a project containing a BOM. Defaults to Spring Cloud Release Git repository')
				booleanParam(RELEASER_SAGAN_UPDATE_VAR, true, 'If true then will update documentation repository with the current URL')
				booleanParam(RELEASER_POST_RELEASE_ONLY_VAR, false, 'If set to true will run only post release tasks')
			}
			jdk jdk8()
			scm {
				git {
					remote {
						url "https://github.com/${organization}/${project}"
						branch "\$${branchVarName()}"
						credentials(githubUserCredentialId())
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
				currentDir="\$(pwd)"
				tmpDir="\$(mktemp -d)"
				trap "{ rm -f \${tmpDir}; }" EXIT
				echo "Cloning to [\${tmpDir}] and building the releaser"
				git clone -b master --single-branch https://github.com/spring-cloud/spring-cloud-release-tools.git "\${tmpDir}"
				pushd "\${tmpDir}"
				rm -rf ~/.m2/repository/org/springframework/cloud
				./mvnw clean install > "\${currentDir}/.git/releaser.log"
				popd
				echo "Run the releaser against the project"
				echo "Checking out branch"
				git checkout \$${branchVarName()}
				echo "Releasing the project"
				${setupGitCredentials()}
				set +x
				SYSTEM_PROPS="-Dgpg.secretKeyring="\$${gpgSecRing()}" -Dgpg.publicKeyring="\$${gpgPubRing()}" -Dgpg.passphrase="\$${gpgPassphrase()}" -DSONATYPE_USER="\$${sonatypeUser()}" -DSONATYPE_PASSWORD="\$${sonatypePassword()}"
				java \${${RELEASER_ADDITIONAL_PROPS_VAR}} -Dreleaser.git.username="\$${githubRepoUserNameEnvVar()}" -Dreleaser.git.password="\$${githubRepoPasswordEnvVar()}" -jar \${tmpDir}/spring-cloud-release-tools-spring/target/spring-cloud-release-tools-spring-1.0.0.BUILD-SNAPSHOT.jar ${releaserOptions()} || exit 1
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

	protected String releaserOptions() {
		return """\
--releaser.post-release-tasks-only=\${$RELEASER_POST_RELEASE_ONLY_VAR}
--releaser.meta-release.release-train-project-name=\${$RELEASER_RELEASE_TRAIN_PROJECT_NAME_VAR}
--releaser.git.release-train-bom-url=\${$RELEASER_GIT_RELEASE_TRAIN_BOM_URL_VAR}
--releaser.pom.this-train-bom=\${$RELEASER_POM_THIS_TRAIN_BOM_VAR}
--releaser.pom.branch=\${$RELEASER_POM_BRANCH_VAR}
--releaser.maven.wait-time-in-minutes=180
--spring.config.name=releaser
--releaser.maven.system-properties="\${SYSTEM_PROPS}"
--full-release
--releaser.sagan.update-sagan=\${$RELEASER_SAGAN_UPDATE_VAR}
--interactive=false""".split("\n").join(" ")
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
