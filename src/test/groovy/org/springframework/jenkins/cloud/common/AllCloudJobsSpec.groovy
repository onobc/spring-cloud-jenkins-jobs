package org.springframework.jenkins.cloud.common

import spock.lang.Specification

class AllCloudJobsSpec extends Specification {

	def 'get correct boot version for cloud release train'() {
		given:

		when:
			String bootMinor = AllCloudJobs.bootForReleaseTrain(cloud)

		then:
			bootMinor == boot

		where:
			cloud               | boot
			"2020.0.0"          | "2.4"
			"2020.0.0-SNAPSHOT" | "2.4"
			"Hoxton.RELEASE"    | "2.3"
			"Greenwich.SR1"     | "2.1"
	}

}
