package org.springframework.jenkins.cloud.common
/**
 *
 * @author Marcin Grzejszczak
 */
class PitestPublisher {

	private final Node rootNode
	private final def propertiesNode
	private final def tap

	PitestPublisher(Node rootNode) {
		this.rootNode = rootNode
		this.propertiesNode = rootNode / 'publishers'
		this.tap = propertiesNode / 'org.jenkinsci.plugins.pitmutation.PitPublisher'
		killRatioMustImprove()
	}

	void killRatioMustImprove(boolean value = true) {
		(tap / 'killRatioMustImprove__').setValue(value)
	}

	static PitestPublisher cloudMutation(Node rootNode, @DelegatesTo(PitestPublisher) Closure closure = Closure.IDENTITY) {
		PitestPublisher publisher = new PitestPublisher(rootNode)
		closure.delegate = publisher
		closure.call()
		return publisher
	}
}
