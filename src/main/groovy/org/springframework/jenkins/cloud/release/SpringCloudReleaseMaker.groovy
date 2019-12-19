package org.springframework.jenkins.cloud.release

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.jobs.FreeStyleJob

import org.springframework.jenkins.cloud.common.Releaser
import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
class SpringCloudReleaseMaker implements JdkConfig, TestPublisher,
		SpringCloudJobs, Cron, Releaser {
	protected static final String RELEASE_VERSION_PARAM = "RELEASE_VERSION"
	protected static final String RELEASER_CONFIG_URL_PARAM = "RELEASER_CONFIG_URL"
	protected static final String RELEASER_CONFIG_BRANCH_PARAM = "RELEASER_CONFIG_BRANCH"
	protected static final String RELEASER_POM_BRANCH_VAR = "RELEASER_POM_BRANCH"
	protected static final String RELEASER_ADDITIONAL_PROPS_VAR = "RELEASER_ADDITIONAL_PROPS"
	protected static final String RELEASER_SAGAN_UPDATE_VAR= 'RELEASER_SAGAN_UPDATE'
	protected static final String RELEASER_RELEASE_TRAIN_PROJECT_NAME_VAR = 'RELEASER_META_RELEASE_RELEASE_TRAIN_PROJECT_NAME'
	protected static final String RELEASER_GIT_RELEASE_TRAIN_BOM_URL_VAR= 'RELEASER_GIT_RELEASE_TRAIN_BOM'
	protected static final String RELEASER_POM_THIS_TRAIN_BOM_VAR = 'RELEASER_POM_THIS_TRAIN'
	protected static final String RELEASER_POST_RELEASE_ONLY_VAR= 'RELEASER_POST_RELEASE_ONLY'

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
		dsl.job(projectName(project)) {
			parameters {
				stringParam(branchVarName(), masterBranch(), "Your project's branch")
				stringParam(RELEASE_VERSION_PARAM, "", "Name of the release (e.g. Hoxton.RELEASE). Will correspond to the properties file (e.g. hoxton_release.properties) in the branch with releaser properties")
				stringParam(RELEASER_CONFIG_URL_PARAM, options.releaserConfigUrl, "Root of the URL where the RAW version of the configuration file is present")
				stringParam(RELEASER_CONFIG_BRANCH_PARAM, options.releaserConfigBranch, "Branch, where the RAW version of the configuration file is present")
				stringParam(RELEASER_POM_BRANCH_VAR, masterBranch(), "Spring Cloud Release branch. If [${RELEASE_VERSION_PARAM}] was passed, then this will be ignored")
				stringParam(RELEASER_ADDITIONAL_PROPS_VAR, '', 'Additional system properties')
				stringParam(RELEASER_RELEASE_TRAIN_PROJECT_NAME_VAR, options.releaseTrainProjectName, 'Name of the project that represents the BOM of the release train')
				stringParam(RELEASER_GIT_RELEASE_TRAIN_BOM_URL_VAR, options.releaseTrainBomUrl, 'Subfolder of the pom that contains the versions for the release train')
				stringParam(RELEASER_POM_THIS_TRAIN_BOM_VAR, options.releaseThisTrainBom, 'URL to a project containing a BOM. Defaults to Spring Cloud Release Git repository')
				booleanParam(RELEASER_SAGAN_UPDATE_VAR, options.updateSagan, 'If true then will update documentation repository with the current URL')
				booleanParam(RELEASER_POST_RELEASE_ONLY_VAR, options.postReleaseOnly, 'If set to true will run only post release tasks')
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
						localBranch("**")
					}
				}
			}
			configureLabels(delegate as FreeStyleJob)
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
				set -o errexit
				${scriptPreconditions()}
				${buildReleaserForSingleProject()}
				releaserJarLocation="\${tmpDir}/spring-cloud-release-tools-spring/target/"
				additionalParams=""
				if [[ \$${RELEASE_VERSION_PARAM} != "" ]]; then
					echo "Found the release version parameter. Will use the properties file to set the versions"
					${fetchConfigurationFile("\${releaserJarLocation}")}
					additionalParams="--releaser.git.fetch-versions-from-git=false"
				fi
				echo "Run the releaser against the project"
				echo "Checking out branch"
				git checkout ${branchToCheck()}
				echo "Releasing the project"
				${setupGitCredentials()}
				set +x
				SPRING_CLOUD_RELEASE_REPO="https://github.com/spring-cloud/spring-cloud-release.git"
				SYSTEM_PROPS="-Dgpg.secretKeyring="\$${gpgSecRing()}" -Dgpg.publicKeyring="\$${gpgPubRing()}" -Dgpg.passphrase="\$${gpgPassphrase()}" -DSONATYPE_USER="\$${sonatypeUser()}" -DSONATYPE_PASSWORD="\$${sonatypePassword()}""
				java \${${RELEASER_ADDITIONAL_PROPS_VAR}} -Dreleaser.git.username="\$${githubRepoUserNameEnvVar()}" -Dreleaser.git.password="\$${githubRepoPasswordEnvVar()}" -jar \${releaserJarLocation}/spring-cloud-release-tools-spring-1.0.0.BUILD-SNAPSHOT.jar ${releaserOptions()} \$additionalParams || exit 1
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
				archiveJunit(mavenJUnitResults()) {
					allowEmptyResults()
				}
				archiveArtifacts {
					allowEmpty()
					pattern(".git/*.log")
					pattern("target/*.txt")
					pattern("target/*.md")
					pattern("target/*.adoc")
				}
				textFinder(".*BUILD UNSTABLE.*", "**/build_status,build_status", false, false, true)
			}
			additionalConfiguration(delegate as FreeStyleJob)
		}
	}

	protected void additionalConfiguration(FreeStyleJob job) {

	}

	protected void configureLabels(FreeStyleJob job) {
		job.label(releaserLabel())
	}

	protected String branchToCheck() {
		return '$' + branchVarName()
	}

	protected String projectName(String project) {
		return "$project-releaser"
	}

	protected String scriptPreconditions() {
		return ""
	}

	protected String releaserOptions() {
		return """\
--releaser.post-release-tasks-only=\${$RELEASER_POST_RELEASE_ONLY_VAR}
--releaser.meta-release.release-train-project-name=\${$RELEASER_RELEASE_TRAIN_PROJECT_NAME_VAR}
--releaser.git.release-train-bom-url=\${$RELEASER_GIT_RELEASE_TRAIN_BOM_URL_VAR}
--releaser.pom.this-train-bom=\${$RELEASER_POM_THIS_TRAIN_BOM_VAR}
--releaser.pom.branch=\${$RELEASER_POM_BRANCH_VAR}
--spring.config.name=releaser
--releaser.maven.wait-time-in-minutes=180
--spring.config.name=releaser
--releaser.maven.system-properties="\${SYSTEM_PROPS}"
--full-release
--releaser.sagan.update-sagan=\${$RELEASER_SAGAN_UPDATE_VAR}
--interactive=false""".split("\n").join(" ")
	}
}
