package org.springframework.jenkins.cloud.ci

import javaposse.jobdsl.dsl.DslFactory

/**
 * @author Marcin Grzejszczak
 * @author Mark Paluch
 */
class VaultSpringCloudDeployBuildMaker extends AbstractHashicorpDeployBuildMaker {

	VaultSpringCloudDeployBuildMaker(DslFactory dsl) {
		super(dsl, 'spring-cloud', 'spring-cloud-vault')
	}

	@Override
	protected String jdkVersion(String branchName) {
		return jdk8()
	}

	@Override
	protected String preStep() {
		return preVaultShell()
	}

	@Override
	protected String postStep() {
		return postVaultShell()
	}

	@Override
	String compileOnlyCommand() {
		return "./mvnw clean install -DskipTests -U -fae"
	}

	@Override
	String projectName() {
		return "spring-cloud-vault"
	}

	@Override
	boolean checkTests() {
		return true
	}
}
