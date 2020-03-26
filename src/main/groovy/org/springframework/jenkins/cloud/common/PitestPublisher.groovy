package org.springframework.jenkins.cloud.common
/**
 *
 * @author Marcin Grzejszczak
 */
class PitestPublisher {

	private final Node rootNode
	private final def propertiesNode
	private final def tap
	private final def buildConditions

	PitestPublisher(Node rootNode) {
		this.rootNode = rootNode
		this.propertiesNode = rootNode / 'publishers'
		this.tap = propertiesNode / 'org.jenkinsci.plugins.pitmutation.PitPublisher'
		this.buildConditions = this.tap / 'buildConditions__'
		killRatioMustImprove()
		minimumKillRatio()
		mustImproveCondition()
		percentageThresholdCondition()
	}

	void killRatioMustImprove(boolean value = true) {
		(tap / 'killRatioMustImprove__').setValue(value)
	}

	void minimumKillRatio(double value = 0.0) {
		(tap / 'minimumKillRatio__').setValue(value)
	}

	void mustImproveCondition() {
		(buildConditions / 'org.jenkinsci.plugins.pitmutation.PitPublisher_-MustImproveCondition').appendNode('outer-class', reference: "../../..")
	}

	void percentageThresholdCondition() {
		def condition = (buildConditions / 'org.jenkinsci.plugins.pitmutation.PitPublisher_-PercentageThresholdCondition')
		condition.appendNode('outer-class', reference: "../../..")
		(condition / 'percentage').setValue(0.0)
	}

	static PitestPublisher cloudMutation(Node rootNode, @DelegatesTo(PitestPublisher) Closure closure = Closure.IDENTITY) {
		PitestPublisher publisher = new PitestPublisher(rootNode)
		closure.delegate = publisher
		closure.call()
		return publisher
	}
}
