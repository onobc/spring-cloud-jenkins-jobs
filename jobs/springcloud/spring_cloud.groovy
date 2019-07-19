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
import org.springframework.jenkins.cloud.common.AllCloudConstants
import org.springframework.jenkins.cloud.compatibility.ManualBootCompatibilityBuildMaker
import org.springframework.jenkins.cloud.e2e.BreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.CloudFoundryBreweryTestExecutor
import org.springframework.jenkins.cloud.e2e.CloudFoundryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.EdgwareBreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.EndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.Jdk11BreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.NetflixEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SleuthEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesEndToEndBuilder
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesTestsBuildMaker
import org.springframework.jenkins.cloud.release.ReleaserOptions
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMaker
import org.springframework.jenkins.cloud.sonar.ConsulSonarBuildMaker
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
		// Normal CI build
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.build().deploy(it)
	}
	JOBS_WITHOUT_TESTS.each {
		// JDK compatibility
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.prefix("spring-cloud-${jdk11()}").jdkVersion(jdk11()).deploy(false)
				.upload(false).build().deployWithoutTests(it)
		// Normal CI build
		new SpringCloudDeployBuildMakerBuilder(dsl)
				.build().deployWithoutTests(it)
	}
}

// Custom jobs builder
CUSTOM_BUILD_JOBS.each { String projectName ->
	new CustomJobFactory(dsl).with {
		new CustomJobFactory(dsl).deploy(projectName)
		new CustomJobFactory(dsl).jdkVersion(projectName, jdk11())
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
	// TODO: Remember to remove this
	buildForEdgware()
	buildForFinchley()
	buildForGreenwich()
	buildForHoxton()
	[jdk11(), jdk12()].each {
		buildForHoxtonWithJdk(it)
		buildForGreenwichWithJdk(it)
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
		.deploy('spring-cloud-release', 'Edgware', false)
new SpringCloudDeployBuildMaker(dsl)
		.deploy('spring-cloud-release', 'Finchley', false)
new SpringCloudDeployBuildMaker(dsl)
		.deploy('spring-cloud-release', 'Greenwich', false)

new ConsulSpringCloudDeployBuildMaker(dsl).deploy()

// CI BUILDS FOR INCUBATOR
new SpringCloudKubernetesDeployBuildMaker(dsl).deploy()
new VaultSpringCloudDeployBuildMaker(dsl).with {
	deploy(masterBranch())
	deploy('1.0.x')
	deploy('1.1.x')
}
new SpringCloudDeployBuildMaker(dsl, "spring-cloud-incubator").with {
	deploy("spring-cloud-circuitbreaker", masterBranch())
	deploy("spring-cloud-contract-raml")
}

// SLEUTH
new SleuthBenchmarksBuildMaker(dsl).buildSleuth()
new SpringCloudSamplesEndToEndBuildMaker(dsl, "openzipkin").with {
	buildWithoutTestsForNewUbuntu("sleuth-webmvc-example", masterBranch(), everyThreeHours())
	buildWithoutTestsForNewUbuntu("sleuth-webmvc-example", "rabbitmq-sender", everyThreeHours())
}
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	buildWithMavenTests("sleuth-issues", masterBranch(), everyThreeHours())
	buildWithMavenTests("sleuth-issues", "2.1.x", everyThreeHours())
	buildWithGradleTests("sleuth-documentation-apps", masterBranch(), everyThreeHours())
	buildWithGradleTests("sleuth-documentation-apps", "2.1.x", everyThreeHours())
	buildWithGradleTests("sleuth-documentation-apps", "2.0.x", everyThreeHours())
	buildWithGradleTests("sleuth-documentation-apps", "1.3.x", everyThreeHours())
}
new SleuthEndToEndBuildMaker(dsl).with {
	buildSleuth(oncePerDay())
}

// CONTRACT
["master", "2.0.x", "1.2.x", "2.2.x"].each { String branch ->
	new SpringCloudSamplesEndToEndBuilder().with {
		it.withProjectAndRepoName("spring-cloud-contract-samples")
				.withBranchName(branch)
				.withCronExpr(everyThreeHours())
				// for postman <-> swagger
				.withNodeJs(true)
				.withMavenTests(false)
				.withGradleTests(false)
	}.build(dsl)
}
new SpringCloudSamplesEndToEndBuilder().with {
	it.withProjectAndRepoName("spring-cloud-contract-samples")
			.withBranchName("master")
			.withCronExpr(everyThreeHours())
			// for postman <-> swagger
			.withNodeJs(true)
			.withJdk(jdk11())
			.withEnvs([SKIP_DOCS: "true", SKIP_COMPATIBILITY: "true"])
			.withMavenTests(false)
			.withGradleTests(false)
}.build(dsl)
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	buildWithMavenTests("the-legacy-app", masterBranch(), everyThreeHours())
	buildWithMavenTests("the-legacy-app", "2.1.x", everyThreeHours())
	buildWithMavenTests("the-legacy-app", "2.0.x", everyThreeHours())
	buildWithMavenTests("sc-contract-car-rental", masterBranch(), everyThreeHours())
	buildWithMavenTests("sc-contract-car-rental", "2.1.x", everyThreeHours())
	buildWithMavenTests("sc-contract-car-rental", "2.0.x", everyThreeHours())
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
	buildSleuthDocApps()
	buildSpringCloudStream()
}
new NetflixEndToEndBuildMaker(dsl).with {
	build(oncePerDay())
}
new CloudFoundryBreweryTestExecutor(dsl).buildBreweryForDocsTests()
new EdgwareBreweryEndToEndBuildMaker(dsl).build()
new Jdk11BreweryEndToEndBuildMaker(dsl).build()
["Finchley", "Greenwich", "Hoxton"].each {
	new BreweryEndToEndBuildMaker(dsl).build(it)
}

// E2E
new EndToEndBuildMaker(dsl, "spring-cloud-samples").with {
	buildWithoutTests("eureka-release-train-interop", oncePerDay())
}

// Josh's CI APP
/*
new JoshEndToEndBuildMaker(dsl, 'bootiful-microservices').with {
	build('bootiful-microservices-edgware',
			'scripts/scenario_edgware_tester.sh',
			everyThreeHours(),
			'scripts/kill_all.sh')
}
new JoshEndToEndBuildMaker(dsl, 'bootiful-reactive-microservices').with {
	build('bootiful-reactive-microservices-finchley',
			'scripts/scenario_finchley_tester.sh',
			everyThreeHours(),
			'scripts/kill_all.sh')
	build('bootiful-reactive-microservices-greenwich',
			'scripts/scenario_greenwich_tester.sh',
			everyThreeHours(),
			'scripts/kill_all.sh')
}
 */

// Pilo's apps
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	build("messaging-application", everyThreeHours())
}

// SONAR
(ALL_JOBS_WITH_TESTS - ["spring-cloud-consul", "spring-cloud-vault", "spring-cloud-aws", "spring-cloud-function"]).each {
	new SonarBuildMaker(dsl).buildSonar(it)
}
new ConsulSonarBuildMaker(dsl).buildSonar()


// RELEASER
ALL_RELEASER_JOBS.each {
	new SpringCloudReleaseMaker(dsl).release(it)
}
def streamOptions = ReleaserOptions.builder()
		.releaseThisTrainBom("spring-cloud-stream-dependencies/pom.xml")
		.releaseTrainBomUrl("https://github.com/spring-cloud/spring-cloud-stream-starters")
		.releaserConfigUrl("https://raw.githubusercontent.com/spring-cloud/spring-cloud-stream-starters")
		.releaserConfigBranch("jenkins-releaser-config")
		.projectsToSkip(AllCloudConstants.DEFAULT_STREAM_RELEASER_SKIPPED_PROJECTS)
		.releaseTrainProjectName("spring-cloud-stream-starters")
		.releaseTrainDependencyNames(["spring-cloud-stream-dependencies"])
		.runUpdatedSamples(false)
		.updateAllTestSamples(false)
		.updateDocumentationRepos(false)
		.updateReleaseTrainDocs(false)
		.updateReleaseTrainWiki(false)
		.updateSpringGuides(false)
		.updateSpringProjects(false)
		.updateSagan(true)
		.build()
ALL_STREAM_JOBS_FOR_RELEASER.each {
	new SpringCloudReleaseMaker(dsl).release(it, streamOptions)
}
new SpringCloudMetaReleaseMaker(dsl).release("spring-cloud-meta-releaser")
new SpringCloudMetaReleaseMaker(dsl).release("spring-cloud-stream-meta-releaser", streamOptions)

// Compatibility builds
new ManualBootCompatibilityBuildMaker(dsl).build()
