package springcloud

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.ci.SleuthBenchmarksBuildMaker
import org.springframework.jenkins.cloud.e2e.BreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.EndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.JdkBreweryEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.NetflixEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SleuthEndToEndBuildMaker
import org.springframework.jenkins.cloud.e2e.SpringCloudContractSamplesEndToEndBuilder
import org.springframework.jenkins.cloud.e2e.SpringCloudSamplesEndToEndBuildMaker

DslFactory dsl = this

// SLEUTH
new SleuthBenchmarksBuildMaker(dsl).buildSleuth()
new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	buildWithMavenTests("spring-cloud-sleuth-samples", masterBranch(), oncePerDay())
	buildWithMavenTests("spring-cloud-sleuth-samples", "3.1.x", oncePerDay())
	buildWithMavenTests("sleuth-issues", masterBranch(), oncePerDay())
	buildWithMavenTests("sleuth-issues", "2.2.x", oncePerDay())
	buildWithMavenTests("sleuth-documentation-apps", masterBranch(), oncePerDay())
	buildWithMavenTests("sleuth-documentation-apps", "2.2.x", oncePerDay())
}
new SleuthEndToEndBuildMaker(dsl).with {
	buildSleuth(oncePerDay())
}

// CONTRACT
new SpringCloudContractSamplesEndToEndBuilder().with {
	it.withBranchName("2.2.x")
			.withJdk(jdk8())
}.buildAll(dsl)
new SpringCloudContractSamplesEndToEndBuilder().with {
	it.withBranchName("2.2.x")
			.withJdk(jdk11())
}.buildAll(dsl)
new SpringCloudContractSamplesEndToEndBuilder().with {
	it.withJdk(jdk11())
}.buildAll(dsl)
new SpringCloudContractSamplesEndToEndBuilder().with {
	it.withJdk(jdk15())
}.buildAll(dsl)
new SpringCloudContractSamplesEndToEndBuilder().with {
	it.withJdk(jdk16())
}.buildAll(dsl)

new SpringCloudSamplesEndToEndBuildMaker(dsl).with {
	buildWithMavenTests("the-legacy-app", masterBranch(), oncePerDay())
	buildWithMavenTests("the-legacy-app", "2.2.x", oncePerDay())
	buildWithMavenTests("sc-contract-car-rental", masterBranch(), oncePerDay())
	buildWithMavenTests("sc-contract-car-rental", "2.2.x", oncePerDay())
}

new NetflixEndToEndBuildMaker(dsl).with {
	build(oncePerDay())
}
new JdkBreweryEndToEndBuildMaker(dsl).with { withJdk(jdk11()).build() }
new JdkBreweryEndToEndBuildMaker(dsl).with { withJdk(jdk15()).build() }
new JdkBreweryEndToEndBuildMaker(dsl).with { withJdk(jdk16()).build() }

// new LatestJdkBreweryEndToEndBuildMaker(dsl).build()
["Hoxton", "2020.0"].each {
	new BreweryEndToEndBuildMaker(dsl).build(it)
}

new EndToEndBuildMaker(dsl, "spring-cloud-samples").with {
	buildWithoutTests("eureka-release-train-interop", oncePerDay())
}
