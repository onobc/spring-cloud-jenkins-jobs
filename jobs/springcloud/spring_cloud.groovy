package springcloud

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.ci.ConsulSpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.ci.CustomJobFactory
import org.springframework.jenkins.cloud.ci.DocsAppBuildMaker
import org.springframework.jenkins.cloud.ci.SleuthBenchmarksBuildMaker
import org.springframework.jenkins.cloud.ci.SleuthMemoryBenchmarksBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudKubernetesDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudReleaseToolsBuildMaker
import org.springframework.jenkins.cloud.ci.VaultSpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.compatibility.BootCompatibilityBuildMaker
import org.springframework.jenkins.cloud.compatibility.ManualBootCompatibilityBuildMaker
import org.springframework.jenkins.cloud.e2e.CloudFoundryBreweryTestExecutor
import org.springframework.jenkins.cloud.e2e.CloudFoundryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.EdgwareBreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.EndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.JoshEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.NetflixEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SleuthEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesEndToEndBuilder
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesTestsBuildMaker
import org.springframework.jenkins.cloud.f2f.SpringCloudPipelinesGradleBuildMaker
import org.springframework.jenkins.cloud.f2f.SpringCloudPipelinesMavenBuildMaker
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMaker
import org.springframework.jenkins.cloud.sonar.ConsulSonarBuildMaker
import org.springframework.jenkins.cloud.sonar.SonarBuildMaker

import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_DEFAULT_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_JOBS_WITH_TESTS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.CUSTOM_BUILD_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.JOBS_WITHOUT_TESTS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.JOBS_WITH_BRANCHES

DslFactory dsl = this

println "Projects with tests $ALL_JOBS_WITH_TESTS"
println "Projects without tests $JOBS_WITHOUT_TESTS"

// BENCHMARK BUILDS
new SleuthBenchmarksBuildMaker(dsl).buildSleuth()
new SleuthMemoryBenchmarksBuildMaker(dsl).buildSleuth()

// CI BUILDS
new DocsAppBuildMaker(dsl).with {
	buildDocs(everyThreeHours())
}
// Branch build maker that allows you to build and deploy a branch - this will be done on demand
new SpringCloudDeployBuildMaker(dsl).with { SpringCloudDeployBuildMaker maker ->
	(ALL_DEFAULT_JOBS).each {
		maker.deploy(it)
		new BootCompatibilityBuildMaker(dsl).build(it, oncePerDay(), false)
	}
	JOBS_WITHOUT_TESTS.each {
		maker.deployWithoutTests(it)
	}
}

// Custom jobs builder
CustomJobFactory customJobFactory = new CustomJobFactory(dsl)
CUSTOM_BUILD_JOBS.each { String projectName ->
	customJobFactory.deploy(projectName)
	List<String> branches = JOBS_WITH_BRANCHES[projectName]
	if (branches) {
		branches.each {
			customJobFactory.deploy(projectName, it)
		}
	}
	new BootCompatibilityBuildMaker(dsl) {
		@Override
		protected String buildCommand() {
			return customJobFactory.compileOnlyCommand(projectName)
		}
	}.with {
		buildWithoutTests(projectName, oncePerDay(), false)
	}
}

new SpringCloudReleaseToolsBuildMaker(dsl).deploy()
new SpringCloudSamplesTestsBuildMaker(dsl).with {
	buildForEdgware()
	buildForFinchley()
}

// BRANCHES BUILD - spring-cloud organization
// Build that allows you to deploy, and build gh-pages of multiple branches. Used for projects
// where we support multiple versions
def branchMaker = new SpringCloudDeployBuildMaker(dsl)
JOBS_WITH_BRANCHES.each { String project, List<String> branches ->
	if (CUSTOM_BUILD_JOBS.contains(project)) {
		return
	}
	branches.each { String branch ->
		boolean checkTests = !JOBS_WITHOUT_TESTS.contains(project)
		branchMaker.deploy(project, branch, checkTests)
	}
}
// Release branches for Spring Cloud Release
branchMaker.deploy('spring-cloud-release', 'Dalston', false)
branchMaker.deploy('spring-cloud-release', 'Edgware', false)
branchMaker.deploy('spring-cloud-release', 'Finchley', false)

new ConsulSpringCloudDeployBuildMaker(dsl).deploy()

// CI BUILDS FOR INCUBATOR
new SpringCloudKubernetesDeployBuildMaker(dsl).deploy()
new VaultSpringCloudDeployBuildMaker(dsl).with {
	deploy(masterBranch())
	deploy('1.0.x')
	deploy('1.1.x')
}
new SpringCloudDeployBuildMaker(dsl, "spring-cloud-incubator")
		.deploy("spring-cloud-contract-raml")

new SpringCloudSamplesEndToEndBuildMaker(dsl, "marcingrzejszczak").with {
	build("spring-cloud-contract-159", everyThreeHours())
	buildWithMavenTests("sc-contract-car-rental", masterBranch(), everyThreeHours())
	buildWithMavenTests("sc-contract-car-rental", "2.0.x", everyThreeHours())
}

// SLEUTH
new SpringCloudSamplesEndToEndBuildMaker(dsl, "openzipkin").with {
	buildWithoutTests("sleuth-webmvc-example", everyThreeHours())
	buildWithoutTests("sleuth-webmvc-example", "rabbitmq-sender", everyThreeHours())
}
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	buildWithMavenTests("sleuth-issues", masterBranch(), everyThreeHours())
	buildWithGradleTests("sleuth-documentation-apps", masterBranch(), everyThreeHours())
	buildWithGradleTests("sleuth-documentation-apps", "edgware", everyThreeHours())
}

// E2E BUILDS
new NetflixEndToEndBuildMaker(dsl).with {
	build(oncePerDay())
}

// Eureka Interop
new EndToEndBuildMaker(dsl, "spring-cloud-samples").with {
	buildWithoutTests("eureka-release-train-interop", oncePerDay())
}

// Finchley
new SleuthEndToEndBuildMaker(dsl).with {
	buildSleuth(oncePerDay())
}
// All jobs for e2e with Brewery
new EdgwareBreweryEndToEndBuildMaker(dsl).build()

// Spring Cloud Contract samples
["master", "2.1.x", "1.2.x"].each { String branch ->
	new SpringCloudSamplesEndToEndBuilder().with {
		it.withProjectAndRepoName("spring-cloud-contract-samples")
				.withBranchName(branch)
				.withCronExpr(everyThreeHours())
				.withWithNodeJs(true)
				.withMavenTests(false)
				.withGradleTests(false)
	}.build(dsl)
}

// E2E on CF
new CloudFoundryEndToEndBuildMaker(dsl).with {
	buildBreweryForDocs()
	buildSleuthDocApps()
	buildSpringCloudStream()
}
new CloudFoundryBreweryTestExecutor(dsl).buildBreweryForDocsTests()

// CUSTOM E2E
// Josh's CI APP
new JoshEndToEndBuildMaker(dsl, 'bootiful-microservices').with {
	// TODO: Remove once Edgware is done
	build('bootiful-microservices-dalston',
			'scripts/scenario_dalston_tester.sh',
			everyThreeHours(),
			'scripts/kill_all.sh')
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
}

// Pilo's apps
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	build("messaging-application", everyThreeHours())
}

// SONAR
['spring-cloud-bus', 'spring-cloud-commons', 'spring-cloud-sleuth', 'spring-cloud-netflix',
 'spring-cloud-zookeeper', 'spring-cloud-contract'].each {
	new SonarBuildMaker(dsl).buildSonar(it)
}
new ConsulSonarBuildMaker(dsl).buildSonar()

// F2F
new SpringCloudPipelinesMavenBuildMaker(dsl).build('github-webhook')
new SpringCloudPipelinesGradleBuildMaker(dsl).build('github-analytics')

// RELEASER
ALL_JOBS.each {
	new SpringCloudReleaseMaker(dsl).release(it)
}
// META-RELEASER
new SpringCloudMetaReleaseMaker(dsl).release()

// Compatibility builds
new ManualBootCompatibilityBuildMaker(dsl).build()
