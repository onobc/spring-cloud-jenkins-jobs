package springcloud

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.qa.KubernetesSonarBuildMaker
import org.springframework.jenkins.cloud.qa.SonarBuildMaker

import static org.springframework.jenkins.cloud.common.AllCloudJobs.ALL_JOBS_WITH_TESTS

DslFactory dsl = this

// QA
(ALL_JOBS_WITH_TESTS - ["spring-cloud-contract", "spring-cloud-consul", "spring-cloud-vault", "spring-cloud-function", "spring-cloud-kubernetes"]).each {
	new SonarBuildMaker(dsl).buildSonar(it)
	// new MutationBuildMaker(dsl).build(it)
}
// new ConsulMutationBuildMaker(dsl).build()
new KubernetesSonarBuildMaker(dsl).buildSonar()
// new MutationBuildMaker(dsl).build("spring-cloud-contract")
