package springcloud

import javaposse.jobdsl.dsl.DslFactory
import org.springframework.jenkins.cloud.common.Projects
import org.springframework.jenkins.cloud.qa.KubernetesSonarBuildMaker
import org.springframework.jenkins.cloud.qa.SonarBuildMaker

DslFactory dsl = this

// QA
Projects.ALL.findAll { it.hasTests }.each { project ->
	new SonarBuildMaker(dsl).buildSonar(project.repo)
}
// new ConsulMutationBuildMaker(dsl).build()
new KubernetesSonarBuildMaker(dsl).buildSonar()
// new MutationBuildMaker(dsl).build("spring-cloud-contract")
