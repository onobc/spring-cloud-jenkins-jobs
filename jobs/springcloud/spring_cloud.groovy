package springcloud

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.ci.ConsulSpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.ci.CustomJobFactory
import org.springframework.jenkins.cloud.ci.SleuthBenchmarksBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMakerBuilder
import org.springframework.jenkins.cloud.ci.SpringCloudKubernetesDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudReleaseToolsBuildMaker
import org.springframework.jenkins.cloud.ci.VaultSpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.common.CloudJdkConfig
import org.springframework.jenkins.cloud.compatibility.ManualBootCompatibilityBuildMaker
import org.springframework.jenkins.cloud.e2e.BreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.CloudFoundryBreweryTestExecutor
import org.springframework.jenkins.cloud.e2e.CloudFoundryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.EndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.Jdk11BreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.NetflixEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SleuthEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesEndToEndBuilder
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesTestsBuildMaker
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseRepoPurger
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMasterMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaserOptions
import org.springframework.jenkins.cloud.sonar.ConsulSonarBuildMaker
import org.springframework.jenkins.cloud.sonar.KubernetesSonarBuildMaker
import org.springframework.jenkins.cloud.sonar.SonarBuildMaker

import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_DEFAULT_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_JOBS_WITH_TESTS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_RELEASER_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_STREAM_JOBS_FOR_RELEASER
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
				.deploy(false).upload(false).build().deploy(it)
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk13()}").jdkVersion(jdk13())
				.onGithubPush(false).cron(oncePerDay())
				.deploy(false).upload(false).build().deploy(it)
		// Normal CI build
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.build().deploy(it)
	}
	JOBS_WITHOUT_TESTS.each {
		// JDK compatibility
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk11()}").jdkVersion(jdk11()).deploy(false)
				.upload(false).build().deployWithoutTests(it)
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk13()}").jdkVersion(jdk13()).onGithubPush(false).cron(oncePerDay()).deploy(false)
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
		new CustomJobFactory(dsl).jdkVersion(projectName, jdk13())
	}
	List<String> branches = JOBS_WITH_BRANCHES[projectName]
	if (branches) {
		branches.each {
			new CustomJobFactory(dsl).deploy(projectName, it)
		}
	}
}

new SpringCloudReleaseToolsBuildMaker(dsl).deploy()

new SpringCloudSamplesTestsBuildMaker(dsl).with {
	buildForHoxton()
	[jdk11(), jdk12(), jdk13()].each {
		buildForHoxtonWithJdk(it)
	}
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
	}
}
// Release branches for Spring Cloud Release
new SpringCloudDeployBuildMaker(dsl)
		.deploy('spring-cloud-release', 'Hoxton', false)

new ConsulSpringCloudDeployBuildMaker(dsl).deploy()

// CI BUILDS FOR INCUBATOR
new SpringCloudKubernetesDeployBuildMaker(dsl).deploy()
new VaultSpringCloudDeployBuildMaker(dsl).with {
	deploy(masterBranch())
}
new SpringCloudDeployBuildMaker(dsl, "spring-cloud-incubator").with {
	deploy("spring-cloud-contract-raml")
	deploy("spring-cloud-rsocket")
}

// SLEUTH
new SleuthBenchmarksBuildMaker(dsl).buildSleuth()
new SpringCloudSamplesEndToEndBuildMaker(dsl, "openzipkin").with {
	buildWithoutTestsForNewUbuntu("sleuth-webmvc-example", masterBranch(), oncePerDay())
	buildWithoutTestsForNewUbuntu("sleuth-webmvc-example", "rabbitmq-sender", oncePerDay())
}
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	buildWithMavenTests("sleuth-issues", masterBranch(), oncePerDay())
	buildWithMavenTests("sleuth-issues", "2.2.x", oncePerDay())
	buildWithMavenTests("sleuth-documentation-apps", masterBranch(), oncePerDay())
	buildWithMavenTests("sleuth-documentation-apps", "2.2.x", oncePerDay())
}
new SleuthEndToEndBuildMaker(dsl).with {
	buildSleuth(oncePerDay())
}

// CONTRACT
["master", "3.0.x"].each { String branch ->
	new SpringCloudSamplesEndToEndBuilder().with {
		it.withProjectAndRepoName("spring-cloud-contract-samples")
		  .withBranchName(branch)
		  .withCronExpr(oncePerDay())
		// for postman <-> swagger
		  .withNodeJs(true)
		  .withMavenTests(false)
		  .withGradleTests(false)
	}.build(dsl)
}
new SpringCloudSamplesEndToEndBuilder().with {
	it.withProjectAndRepoName("spring-cloud-contract-samples")
	  .withBranchName("master")
	  .withCronExpr(oncePerDay())
	// for postman <-> swagger
	  .withNodeJs(true)
	  .withJdk(jdk11())
	  .withEnvs([SKIP_DOCS: "true", SKIP_COMPATIBILITY: "true"])
	  .withMavenTests(false)
	  .withGradleTests(false)
}.build(dsl)
new SpringCloudSamplesEndToEndBuilder().with {
	it.withProjectAndRepoName("spring-cloud-contract-samples")
	  .withBranchName("master")
	  .withCronExpr(oncePerDay())
	// for postman <-> swagger
	  .withNodeJs(true)
	  .withJdk(jdk13())
	  .withEnvs([SKIP_DOCS: "true", SKIP_COMPATIBILITY: "true"])
	  .withMavenTests(false)
	  .withGradleTests(false)
}.build(dsl)
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	buildWithMavenTests("the-legacy-app", masterBranch(), oncePerDay())
	buildWithMavenTests("the-legacy-app", "2.2.x", oncePerDay())
	buildWithMavenTests("sc-contract-car-rental", masterBranch(), oncePerDay())
	buildWithMavenTests("sc-contract-car-rental", "2.2.x", oncePerDay())
}
new SpringCloudSamplesEndToEndBuilder().with {
	it.withRepoName("Pearson-Contracts")
	  .withProjectName("pearson-contracts")
	  .withOrganization("marcingrzejszczak")
	  .withCronExpr(oncePerDay())
	  .withJdk(jdk8())
	  .withMavenTests(true)
	  .withGradleTests(true)
	  .withWipeOutWorkspace(false)
}.build(dsl)

// BREWERY
new CloudFoundryEndToEndBuildMaker(dsl).with {
	buildBreweryForDocs()
	buildSpringCloudStream()
}
new CloudFoundryEndToEndBuildMaker(dsl).with {
	buildSleuthDocApps()
}
new NetflixEndToEndBuildMaker(dsl).with {
	build(oncePerDay())
}
new CloudFoundryBreweryTestExecutor(dsl).buildBreweryForDocsTests()
new Jdk11BreweryEndToEndBuildMaker(dsl).build()
// new LatestJdkBreweryEndToEndBuildMaker(dsl).build()
["Hoxton", "Ilford"].each {
	new BreweryEndToEndBuildMaker(dsl).build(it)
}

// E2E
new EndToEndBuildMaker(dsl, "spring-cloud-samples").with {
	buildWithoutTests("eureka-release-train-interop", oncePerDay())
}

// Pilo's apps
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	build("messaging-application", oncePerDay())
}

// SONAR
(ALL_JOBS_WITH_TESTS - ["spring-cloud-contract", "spring-cloud-consul", "spring-cloud-vault", "spring-cloud-aws", "spring-cloud-function", "spring-cloud-kubernetes"]).each {
	new SonarBuildMaker(dsl).buildSonar(it)
}
new ConsulSonarBuildMaker(dsl).buildSonar()
new KubernetesSonarBuildMaker(dsl).buildSonar()

// RELEASER
ALL_RELEASER_JOBS.each {
	new SpringCloudReleaseMasterMaker(dsl).release(it, SpringCloudReleaserOptions.springCloudMaster())
	new SpringCloudReleaseMaker(dsl).release(it, SpringCloudReleaserOptions.springCloud())
}

ALL_STREAM_JOBS_FOR_RELEASER.each {
	new SpringCloudReleaseMaker(dsl).release(it, SpringCloudReleaserOptions.springCloudStream())
}
new SpringCloudMetaReleaseMaker(dsl).release("spring-cloud-meta-releaser", SpringCloudReleaserOptions.springCloud())
new SpringCloudMetaReleaseMaker(dsl).release("spring-cloud-stream-meta-releaser", SpringCloudReleaserOptions.springCloudStream())
new SpringCloudMetaReleaseRepoPurger(dsl).build()

// Compatibility builds
new ManualBootCompatibilityBuildMaker(dsl).build()
