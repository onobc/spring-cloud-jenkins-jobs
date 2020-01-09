package reactor

import javaposse.jobdsl.dsl.DslFactory

import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudMetaReleaseRepoPurger
import org.springframework.jenkins.cloud.release.SpringCloudReleaseMaker
import org.springframework.jenkins.cloud.release.SpringCloudReleaserOptions

DslFactory dsl = this

// RELEASER
["reactor-core", "reactor-addons", "reactor-pool", "reactor-netty", "reactor-kafka", "reactor-kotlin-extensions"].each {
//	new SpringCloudReleaseMasterMaker(dsl).release(it, SpringCloudReleaserOptions.reactorMaster())
	new SpringCloudReleaseMaker(dsl).release(it, SpringCloudReleaserOptions.reactor())
}

new SpringCloudMetaReleaseMaker(dsl).release("reactor-meta-releaser", SpringCloudReleaserOptions.reactor())
new SpringCloudMetaReleaseRepoPurger(dsl) {
	@Override
	String prefixJob(String s) {
		return "reactor"
	}
}.build()
