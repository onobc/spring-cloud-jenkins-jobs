package org.springframework.jenkins.cloud.common

import spock.lang.Specification

class AllCloudJobsSpec extends Specification {

	def 'get correct boot version for cloud release train'() {
		expect:
			boot == AllCloudJobs.bootForReleaseTrain(cloud)

		where:
			cloud               || boot
			null                || "2.4.0-RC1"
			"null"              || "2.4.0-RC1"
			"2020.0.0"          || "2.4.0-RC1"
			"2020.0.0-SNAPSHOT" || "2.4.0-RC1"
			"Hoxton.RELEASE"    || "2.3"
			"Greenwich.SR1"     || "2.1"
	}

}
