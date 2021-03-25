package springcloud

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.ci.ConsulSpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.ci.CustomJobFactory
import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMakerBuilder
import org.springframework.jenkins.cloud.ci.SpringCloudKubernetesDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudReleaseToolsBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudReleaseTrainDocsMaker
import org.springframework.jenkins.cloud.ci.VaultSpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.common.CloudJdkConfig
import org.springframework.jenkins.cloud.compatibility.BootCompatibilityBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesTestsBuildMaker

import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_DEFAULT_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_JOBS_WITH_TESTS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.CUSTOM_BUILD_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.JOBS_WITHOUT_TESTS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.JOBS_WITH_BRANCHES

DslFactory dsl = this

println "Projects with tests $ALL_JOBS_WITH_TESTS"
println "Projects without tests $JOBS_WITHOUT_TESTS"

// CI BUILDS
// Branch build maker that allows you to build and deploy a branch - this will be done on demand
new SpringCloudDeployBuildMaker(dsl).with { SpringCloudDeployBuildMaker maker ->
	(ALL_DEFAULT_JOBS).each {
		// JDK compatibility
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk11()}").jdkVersion(jdk11())
				.upload(false).build().deploy(it)
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk15()}").jdkVersion(jdk15())
				.onGithubPush(false).cron(oncePerDay())
				.upload(false).build().deploy(it)
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk16()}").jdkVersion(jdk16())
				.onGithubPush(false).cron(oncePerDay())
				.upload(false).build().deploy(it)
		// Normal CI build
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.build().deploy(it)
	}
	JOBS_WITHOUT_TESTS.each {
		// JDK compatibility
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk11()}").jdkVersion(jdk11())
				.upload(false).build().deployWithoutTests(it)
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk15()}").jdkVersion(jdk15()).onGithubPush(false).cron(oncePerDay())
				.upload(false).build().deployWithoutTests(it)
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk16()}").jdkVersion(jdk16()).onGithubPush(false).cron(oncePerDay())
				.upload(false).build().deployWithoutTests(it)
		// Normal CI build
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.build().deployWithoutTests(it)
	}
}

// Custom jobs builder
CUSTOM_BUILD_JOBS.each { String projectName ->
	new CloudJdkConfig().with {
		new CustomJobFactory(dsl).deploy(projectName)
		new CustomJobFactory(dsl).jdkVersion(projectName, jdk11())
		new CustomJobFactory(dsl).jdkVersion(projectName, jdk15())
		new CustomJobFactory(dsl).jdkVersion(projectName, jdk16())
	}
	List<String> branches = JOBS_WITH_BRANCHES[projectName]
	if (branches) {
		branches.each {
			new CustomJobFactory(dsl).deploy(projectName, it)
		}
	}
}

new SpringCloudReleaseToolsBuildMaker(dsl).with {
	deploy()
	deploy("1.0.x")
}

new SpringCloudSamplesTestsBuildMaker(dsl).with {
	buildForIlford()
	[jdk11(), jdk15(), jdk16()].each {
		buildForIlfordWithJdk(it)
	}
}

new SpringCloudReleaseTrainDocsMaker(dsl).with {
	deploy(masterBranch())
	deploy("Hoxton")
}

// BRANCHES BUILD - spring-cloud organization
// Build that allows you to deploy, and build gh-pages of multiple branches. Used for projects
// where we support multiple versions
JOBS_WITH_BRANCHES.each { String project, List<String> branches ->
	if (CUSTOM_BUILD_JOBS.contains(project)) {
		return
	}
	branches.each { String branch ->
		boolean checkTests = !JOBS_WITHOUT_TESTS.contains(project)
		new SpringCloudDeployBuildMaker(dsl).deploy(project, branch, checkTests)
		new BootCompatibilityBuildMaker(dsl).with {
			it.buildWithTests("${project}-${branch}", project, branch, oncePerDay(), checkTests)
		}
	}
}
// Release branches for Spring Cloud Release
new SpringCloudDeployBuildMaker(dsl)
		.deploy('spring-cloud-release', 'Hoxton', false)

new ConsulSpringCloudDeployBuildMaker(dsl).deploy()
new SpringCloudKubernetesDeployBuildMaker(dsl).deploy()
new VaultSpringCloudDeployBuildMaker(dsl).with {
	deploy(masterBranch())
}

// CI BUILDS FOR INCUBATOR
new SpringCloudDeployBuildMaker(dsl, "spring-cloud-incubator").with {
	String projectName = "spring-cloud-sleuth-otel"
	deploy(projectName)

	new SpringCloudDeployBuildMakerBuilder(dsl)
			.organization("spring-cloud-incubator")
			.prefix("spring-cloud-${jdk15()}").jdkVersion(jdk15())
			.onGithubPush(false).cron(oncePerDay())
			.upload(false).build().deploy(projectName)

	new SpringCloudDeployBuildMakerBuilder(dsl)
			.organization("spring-cloud-incubator")
			.prefix("spring-cloud-${jdk16()}").jdkVersion(jdk16())
			.onGithubPush(false).cron(oncePerDay())
			.upload(false).build().deploy(projectName)
}

