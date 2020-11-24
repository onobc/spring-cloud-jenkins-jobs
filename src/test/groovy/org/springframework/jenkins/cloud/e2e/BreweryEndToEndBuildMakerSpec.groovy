package org.springframework.jenkins.cloud.e2e

import spock.lang.Specification

class BreweryEndToEndBuildMakerSpec extends Specification {

	def 'should not use boot function for a release train that uses a concrete boot version'() {
		given:
			BreweryEndToEndBuildMaker maker = new BreweryEndToEndBuildMaker()
		when:
			String switches = maker.defaultSwitches("2020.0.0")
		then:
			!switches.contains("bootVersion")
	}

	def 'should use boot function for a release train that does not use a concrete boot version'() {
		given:
			BreweryEndToEndBuildMaker maker = new BreweryEndToEndBuildMaker()
		when:
			String switches = maker.defaultSwitches("hoxton")
		then:
			switches.contains("bootVersion")
	}
}
