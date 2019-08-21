package org.springframework.jenkins.cloud.sonar

import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Ryan Baxter
 */
class KubernetesSonarBuildMaker extends SonarBuildMaker {
	KubernetesSonarBuildMaker(DslFactory dsl) {
		super(dsl)
	}

	void buildSonar() {
		super.buildSonar('spring-cloud-kubernetes')
	}

	@Override
	Closure defaultSteps() {
		return buildStep {
			environmentVariables {
				env("_SERVICE_OCCURENCE", "5")
			}
			shell('./mvnw -s .settings.xml clean install -Dservice.occurence=${_SERVICE_OCCURENCE} org.jacoco:jacoco-maven-plugin:prepare-agent install -U -P sonar -nsu --batch-mode -Dmaven.test.redirectTestOutputToFile=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn')
			shell("""\
				echo "Running sonar please wait..."
				set +x
				./mvnw \$SONAR_MAVEN_GOAL -Psonar -Dsonar.host.url=\$SONAR_HOST_URL -Dsonar.login=\$SONAR_AUTH_TOKEN || ${postAction()}
				set -x
				""")
		}
	}
}
