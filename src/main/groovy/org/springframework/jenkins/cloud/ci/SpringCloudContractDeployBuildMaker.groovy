package org.springframework.jenkins.cloud.ci

import groovy.transform.CompileStatic
import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.CloudCron
import org.springframework.jenkins.cloud.common.CustomJob
import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.cloud.common.SpringCloudNotification
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Maven
import org.springframework.jenkins.common.job.TestPublisher

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
class SpringCloudContractDeployBuildMaker implements JdkConfig, TestPublisher, CloudCron,
		SpringCloudJobs, Maven, CustomJob {
	private static final Map<String, String> JDKS = [
			"jdk8" : "8.0.272.hs-adpt",
			"jdk11" : "11.0.9.hs-adpt",
			"openjdk11" : "11.0.9.hs-adpt",
			"openjdk14" : "14.0.2.hs-adpt",
			"openjdk15" : "15.0.1.hs-adpt",
	]
	private final DslFactory dsl
	final String organization
	final String projectName

	SpringCloudContractDeployBuildMaker(DslFactory dsl) {
		this.dsl = dsl
		this.organization = 'spring-cloud'
		this.projectName = 'spring-cloud-contract'
	}

	SpringCloudContractDeployBuildMaker(DslFactory dsl, String organization, String projectName = 'spring-cloud-contract') {
		this.dsl = dsl
		this.organization = organization
		this.projectName = projectName
	}

	@Override
	void deploy() {
		doDeploy("${prefixJob(projectName)}-${masterBranch()}-ci", this.projectName, masterBranch())
	}

	@Override
	void deploy(String branchName) {
		doDeploy("${prefixJob(projectName)}-${branchName}-ci", this.projectName, branchName)
	}

	@Override
	String compileOnlyCommand() {
		return "./scripts/compileOnly.sh"
	}

	@Override
	String projectName() {
		return "spring-cloud-contract"
	}

	@Override
	boolean checkTests() {
		return true
	}

	@Override
	void jdkBuild(String jdkVersion) {
		doDeploy("spring-cloud-${jdkVersion}-${prefixJob(projectName)}-${masterBranch()}-ci", this.projectName, masterBranch(), jdkVersion, false)
	}

	private void doDeploy(String projectName, String repoName, String branchName, String jdkVersion = jdk8(), boolean deploy = true) {
		dsl.job(projectName) {
			triggers {
				cron cronValue
				if (onGithubPush) {
					githubPush()
				}
			}
			parameters {
				stringParam(branchVarName(), branchName, 'Which branch should be built')
			}
			jdk jdkVersion
			if (jdkVersion != jdk8()) {
				label(ubuntu18_04())
			}
			scm {
				git {
					remote {
						url "https://github.com/${organization}/${repoName}"
					}
					branch branchVar()
					extensions {
						wipeOutWorkspace()
						localBranch("**")
					}
				}
			}
			wrappers {
				timestamps()
				colorizeOutput()
				maskPasswords()
				credentialsBinding {
					usernamePassword(dockerhubUserNameEnvVar(),
							dockerhubPasswordEnvVar(),
							dockerhubCredentialId())
					usernamePassword(repoUserNameEnvVar(),
							repoPasswordEnvVar(),
							repoSpringIoUserCredentialId())
					usernamePassword(githubRepoUserNameEnvVar(),
							githubRepoPasswordEnvVar(),
							githubUserCredentialId())
					string(gradlePublishKeyEnvVar(), gradlePublishKeySecretId())
					string(gradlePublishSecretEnvVar(), gradlePublishSecretSecretId())
					string(githubToken(), githubTokenCredId())
				}
				timeout {
					noActivity(300)
					failBuild()
					writeDescription('Build failed due to timeout after {0} minutes of inactivity')
				}
			}
			steps {
				shell(loginToDocker())
				shell("""#!/bin/bash
	rm -rf /opt/jenkins/data/tools/hudson.tasks.Maven_MavenInstallation/maven33/
	rm -rf ~/.m2/repository/org/springframework/boot/spring-boot-loader-tools/
""")
				if (deploy) {
					shell("""#!/bin/bash -x
echo "Removes old installed stubs and deploys all projects (except for docs)"
rm -rf ~/.m2/repository/com/example && rm -rf ~/.m2/repository/org/springframework/cloud/contract/verifier/stubs/ && ./mvnw clean deploy -nsu -P integration,spring -U \$MVN_LOCAL_OPTS -Dmaven.test.redirectTestOutputToFile=true -Dsurefire.runOrder=random
""")
				}
				boolean jdkIs8 = jdkVersion == jdk8()
				shell("""#!/bin/bash -x
					echo "Building Spring Cloud Contract docs"
					./scripts/generateDocs.sh
					${
					if (deploy) {
						"./mvnw deploy -Pdocs,spring -pl docs -Dsdkman-java-installation.version=${JDKS.get(jdkVersion) ?: JDKS.get(jdk8())} ${!jdkIs8 ? '-Djavadoc.failOnError=false -Djavadoc.failOnWarnings=false' : ''}"
					}
					else {
						"./mvnw clean install -U -Pintegration,spring -Dsdkman-java-installation.version=${JDKS.get(jdkVersion) ?: JDKS.get(jdk8())} ${!jdkIs8 ? '-Djavadoc.failOnError=false -Djavadoc.failOnWarnings=false' : ''}"
					}
				}
					""")
			}
			configure {
				SpringCloudNotification.cloudSlack(it as Node)
			}
			publishers {
				archiveJunit mavenJUnitResults()
				archiveJunit gradleJUnitResults()
			}
		}
	}
}
