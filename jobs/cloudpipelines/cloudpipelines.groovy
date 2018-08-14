package cloudpipelines

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.ci.SpringCloudDeployBuildMaker
import org.springframework.jenkins.cloudpipelines.ci.CloudPipelinesBaseDeployBuildMaker
import org.springframework.jenkins.cloudpipelines.ci.CloudPipelinesDeployBuildMaker

DslFactory dsl = this

["scripts", "jenkins", "concourse"].each { String project ->
	new CloudPipelinesDeployBuildMaker(dsl, project).deploy()
}

new CloudPipelinesBaseDeployBuildMaker(dsl).deploy()
SpringCloudDeployBuildMaker.cloudPipelines(dsl).deploy("project-crawler")