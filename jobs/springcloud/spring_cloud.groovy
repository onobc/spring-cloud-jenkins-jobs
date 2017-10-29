package springcloud

import org.springframework.jenkins.cloud.ci.ConsulSpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.ci.DocsAppBuildMaker
import org.springframework.jenkins.cloud.ci.SleuthBenchmarksBuildMaker
import org.springframework.jenkins.cloud.ci.SleuthMemoryBenchmarksBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudBranchBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudContractDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudKubernetesDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudNetflixDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudPipelinesBaseDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudPipelinesDeployBuildMaker
import org.springframework.jenkins.cloud.ci.SpringCloudReleaseToolsBuildMaker
import org.springframework.jenkins.cloud.ci.VaultSpringCloudDeployBuildMaker
import org.springframework.jenkins.cloud.e2e.CloudFoundryBreweryTestExecutor
import org.springframework.jenkins.cloud.e2e.CloudFoundryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.DalstonBreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.EndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.FinchleyBreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.JoshEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.NetflixEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SleuthEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesTestsBuildMaker
import org.springframework.jenkins.cloud.f2f.SpringCloudPipelinesGradleBuildMaker
import org.springframework.jenkins.cloud.f2f.SpringCloudPipelinesMavenBuildMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMaker
import org.springframework.jenkins.cloud.sonar.ConsulSonarBuildMaker
import org.springframework.jenkins.cloud.sonar.SonarBuildMaker
import javaposse.jobdsl.dsl.DslFactory

import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_DEFAULT_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_JOBS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_JOBS_WITH_TESTS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.JOBS_WITHOUT_TESTS
import static org.springframework.jenkins.cloud.common.AllCloudJobs.JOBS_WITH_BRANCHES

DslFactory dsl = this

println "Projects with tests $ALL_JOBS_WITH_TESTS"
println "Projects without tests $JOBS_WITHOUT_TESTS"

// BENCHMARK BUILDS
new SleuthBenchmarksBuildMaker(dsl).buildSleuth()
new SleuthMemoryBenchmarksBuildMaker(dsl).buildSleuth()

// CI BUILDS
new DocsAppBuildMaker(dsl).buildDocs(everyThreeHours())
// Branch build maker that allows you to build and deploy a branch - this will be done on demand
SpringCloudBranchBuildMaker branchBuildMaker = new SpringCloudBranchBuildMaker(dsl)
new SpringCloudDeployBuildMaker(dsl).with { SpringCloudDeployBuildMaker maker ->
	(ALL_DEFAULT_JOBS).each {
		maker.deploy(it)
		branchBuildMaker.deploy(it)
	}
	JOBS_WITHOUT_TESTS.each {
		maker.deployWithoutTests(it)
		branchBuildMaker.deployWithoutTests(it)
	}
}
new SpringCloudPipelinesDeployBuildMaker(dsl).deploy()
new SpringCloudPipelinesBaseDeployBuildMaker(dsl).deploy()
new SpringCloudReleaseToolsBuildMaker(dsl).deploy()

// BRANCHES BUILD - spring-cloud organization
// Build that allows you to deploy, and build gh-pages of multiple branches. Used for projects
// where we support multiple versions
def branchMaker = new SpringCloudDeployBuildMaker(dsl)
JOBS_WITH_BRANCHES.each { String project, List<String> branches ->
	branches.each { String branch ->
		boolean checkTests = !JOBS_WITHOUT_TESTS.contains(project)
		branchMaker.deploy(project, branch, checkTests)
	}
}
// Release branches for Spring Cloud Release
// TODO: Remove once Edgware  is done
//branchMaker.deploy('spring-cloud-release', 'Camden', false)
branchMaker.deploy('spring-cloud-release', 'Dalston', false)
branchMaker.deploy('spring-cloud-release', 'Finchley', false)

new ConsulSpringCloudDeployBuildMaker(dsl).deploy()
// CI BUILDS FOR INCUBATOR
new SpringCloudKubernetesDeployBuildMaker(dsl).deploy()

new VaultSpringCloudDeployBuildMaker(dsl).with {
	deploy(masterBranch())
	deploy('1.0.x')
	deploy('2.0.x')
}
new SpringCloudDeployBuildMaker(dsl, "spring-cloud-incubator").deploy("spring-cloud-contract-raml")
// CI BUILDS FOR SPRING CLOUD CONTRACTS
new SpringCloudContractDeployBuildMaker(dsl).with {
	deploy(masterBranch())
	deploy("1.1.x")
	deploy("2.0.x")
	branch()
}
new SpringCloudNetflixDeployBuildMaker(dsl).with {
	deploy(masterBranch())
	deploy("1.2.x")
	deploy("1.3.x")
	deploy("2.0.x")
	branch()
}
// issue #159
new SpringCloudSamplesEndToEndBuildMaker(dsl, "marcingrzejszczak").build("spring-cloud-contract-159", everyThreeHours())
new SpringCloudSamplesEndToEndBuildMaker(dsl, "openzipkin").buildWithoutTests("sleuth-webmvc-example", everyThreeHours())

// E2E BUILDS
new NetflixEndToEndBuildMaker(dsl).with {
	build(everySixHours())
}

// CUSTOM E2E FOR SPRING CLOUD PROJECTS
['spring-cloud-zookeeper', 'spring-cloud-consul'].each { String projectName ->
	def maker = new EndToEndBuildMaker(dsl)
	maker.build(projectName, maker.everySixHours())
}
new SleuthEndToEndBuildMaker(dsl).with {
	buildSleuth(everySixHours())
	buildSleuthStream(everySixHours())
	buildSleuthStreamKafka(everySixHours())
}
// All jobs for e2e with Brewery
new DalstonBreweryEndToEndBuildMaker(dsl).build()
new FinchleyBreweryEndToEndBuildMaker(dsl).build()
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	buildWithGradleAndMavenTests("spring-cloud-contract-samples", everySixHours())
	buildWithGradleAndMavenTests("spring-cloud-contract-samples", everySixHours(), "1.1.x")
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
/*new JoshEndToEndBuildMaker(dsl, 'bootiful-reactive-microservices').with {
	build('bootiful-reactive-microservices-finchley',
			'scripts/scenario_finchley_tester.sh',
			everyThreeHours(),
			'scripts/kill_all.sh')
}*/
new SpringCloudSamplesTestsBuildMaker(dsl).with {
	buildForDalston()
	buildForEdgware()
}

// SONAR

['spring-cloud-bus', 'spring-cloud-commons', 'spring-cloud-sleuth', 'spring-cloud-netflix',
 'spring-cloud-zookeeper', 'spring-cloud-contract'].each {
	new SonarBuildMaker(dsl).buildSonar(it)
}
// TODO: Fix Consul Sonar Build
new ConsulSonarBuildMaker(dsl).buildSonar()

// F2F
new SpringCloudPipelinesMavenBuildMaker(dsl).build('github-webhook')
new SpringCloudPipelinesGradleBuildMaker(dsl).build('github-analytics')

// RELEASER
ALL_JOBS.each {
	new SpringCloudReleaseMaker(dsl).release(it)
}
// ========== FUNCTIONS ==========

String everyThreeHours() {
	return "H H/3 * * *"
}

String every12Hours() {
	return "H H/12 * * *"
}

String everyDay() {
	return "H H * * *"
}
