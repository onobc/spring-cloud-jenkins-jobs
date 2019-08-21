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
			shell('./mvnw clean -Dservice.occurence=${_SERVICE_OCCURENCE} #org.jacoco:jacoco-maven-plugin:prepare-agent install -Psonar -U')
			shell("""\
				echo "Running sonar please wait..."
				set +x
				./mvnw \$SONAR_MAVEN_GOAL -Psonar -Dsonar.host.url=\$SONAR_HOST_URL -Dsonar.login=\$SONAR_AUTH_TOKEN || ${postAction()}
				set -x
				""")
		}
	}
}
