package org.springframework.jenkins.cloud.e2e

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.common.SpringCloudJobs
import org.springframework.jenkins.common.job.Cron
import org.springframework.jenkins.common.job.JdkConfig
import org.springframework.jenkins.common.job.Label
import org.springframework.jenkins.common.job.TestPublisher

class SpringCloudContractSamplesEndToEndBuilder extends SpringCloudSamplesEndToEndBuilder<SpringCloudContractSamplesEndToEndBuilder> {

    void buildAll(DslFactory dsl) {
        String builderBranchName = branchName
        String jdkVersion = jdk
        new SpringCloudSamplesEndToEndBuilder().with {
            it.withRepoName("spring-cloud-contract-samples")
                    .withProjectName("spring-cloud-contract-samples-build-maven-only")
                    .withBranchName(builderBranchName)
                    .withEnvs(["SKIP_COMPATIBILITY": "true", "SKIP_DOCS" : "true"])
                    .withCronExpr(oncePerDay())
                    .withJdk(jdkVersion)
                    .withScriptName("scripts/runMavenBuilds.sh")
                    .withMavenTests(true)
                    .withGradleTests(false)
        }.build(dsl)

        new SpringCloudSamplesEndToEndBuilder().with {
            it.withRepoName("spring-cloud-contract-samples")
                    .withProjectName("spring-cloud-contract-samples-build-gradle-only")
                    .withBranchName(builderBranchName)
                    .withEnvs(["SKIP_COMPATIBILITY": "true", "SKIP_DOCS" : "true"])
                    .withCronExpr(oncePerDay())
                    .withJdk(jdkVersion)
                    .withScriptName("scripts/runGradleBuilds.sh")
                    .withMavenTests(false)
                    .withGradleTests(true)
        }.build(dsl)

        new SpringCloudSamplesEndToEndBuilder().with {
            it.withRepoName("spring-cloud-contract-samples")
                    .withProjectName("spring-cloud-contract-samples-docs-only")
                    .withBranchName(builderBranchName)
                    .withEnvs(["SKIP_BUILD": "true", "SKIP_COMPATIBILITY" : "true"])
                    .withCronExpr(oncePerDay())
                    .withJdk(jdkVersion)
                    .withMavenTests(false)
                    .withGradleTests(false)
        }.build(dsl)
    }
}
