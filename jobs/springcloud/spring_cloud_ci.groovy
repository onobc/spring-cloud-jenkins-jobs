package springcloud

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.ci.ProjectDeployBuildMaker
import org.springframework.jenkins.cloud.common.ReleaseTrains
import org.springframework.jenkins.cloud.compatibility.ProjectBootCompatibilityBuildMaker

DslFactory dsl = this

// CI BUILDS
// Branch build maker that allows you to build and deploy a branch - this will be done on demand
ReleaseTrains.ALL.findAll { it.active }.each { train ->
	// each jdk in the train
	boolean first = true
	train.jdks.each { jdk ->
		 // each project and branch
		 train.projectsWithBranch.each { project, branch ->
			 if (first || project.checkJdkCompatibility) {
				 def maker = new ProjectDeployBuildMaker(dsl, train, project)
				 maker.buildContext.jdk = jdk
				 maker.buildContext.upload = first
				 maker.buildContext.branch = branch
				 maker.deploy()
			 }
		 }
		 first = false // only upload baseline jdk
	}
}

// Boot compatibility BUILDS
// Branch build maker that allows you to build and deploy a branch - this will be done on demand
ReleaseTrains.ALL.findAll { it.active }.each { train ->
	// default jdk for boot compatibility
	String jdk = train.jdks.get(0)
	// ci builds above test default jdk, so we want everything but first
	train.bootVersions.subList(1, train.bootVersions.size()).each { bootVersion ->
		 // each project and branch that wants jdk compatibility
		 train.projectsWithBranch.findAll { it.key.checkJdkCompatibility }.each { project, branch ->
			  def maker = new ProjectBootCompatibilityBuildMaker(dsl, train, project)
			  maker.buildContext.jdk = jdk
			  maker.buildContext.upload = false
			  maker.buildContext.branch = branch
			  maker.build(bootVersion)
		 }
	}
}

//new VaultCompatibilityBuildMaker(dsl).with {
//	it.buildWithTests("spring-cloud-vault", "spring-cloud-vault", "main", oncePerDay(), true)
//}

