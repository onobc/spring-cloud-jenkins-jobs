package org.springframework.jenkins.cloud.e2e

import javaposse.jobdsl.dsl.DslFactory

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

        builderBranchName = "2021.0.x"
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
    }
}
