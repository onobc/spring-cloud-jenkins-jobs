package org.springframework.jenkins.cloud.common

import spock.lang.Specification

class AllCloudJobsSpec extends Specification {

	def 'get correct boot version [#boot] for cloud release train [#cloud]'() {
		expect:
			AllCloudJobs.bootForReleaseTrain(cloud).startsWith(boot)

		where:
			cloud               || boot
			null                || "2.4"
			"null"              || "2.4"
			"2020.0.0"          || "2.4"
			"2020.0.0-SNAPSHOT" || "2.4"
			"Hoxton.RELEASE"    || "2.3"
			"Greenwich.SR1"     || "2.1"
	}

}
