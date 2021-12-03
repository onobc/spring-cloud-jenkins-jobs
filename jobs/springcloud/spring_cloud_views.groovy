package springcloud

import javaposse.jobdsl.dsl.DslFactory

DslFactory dsl = this

dsl.listView('Seeds') {
	jobs {
		regex('.*-seed')
	}
	columns defaultColumns()
}

dsl.nestedView('Spring Cloud') {
	views {
		listView('Boot.NEXT') {
			jobs {
				regex('spring-cloud.*-compatibility-check')
			}
			columns defaultColumns()
		}
		listView('Boot.MANUAL') {
			jobs {
				name('spring-cloud-compatibility-boot-check')
			}
			columns defaultColumns()
		}
		listView('Spring.NEXT') {
			jobs {
				regex('spring-cloud.*-compatibility-spring-check')
			}
			columns defaultColumns()
		}
		listView('Spring.MANUAL') {
			jobs {
				name('spring-cloud-compatibility-spring-check')
			}
			columns defaultColumns()
		}
		listView('CI') {
			jobs {
				regex('spring-cloud.*-ci')
			}
			columns defaultColumns()
		}
		listView('CI.MAIN') {
			jobs {
				regex('spring-cloud.*main-ci')
			}
			columns defaultColumns()
		}
		listView('CI.ilford') {
			jobs {
				name('spring-cloud-build-3.0.x-ci')
				name('spring-cloud-bus-3.0.x-ci')
				name('spring-cloud-circuitbreaker-2.0.x-ci')
				name('spring-cloud-cli-3.0.x-ci')
				name('spring-cloud-cloudfoundry-3.0.x-ci')
				name('spring-cloud-commons-3.0.x-ci')
				name('spring-cloud-config-3.0.x-ci')
				name('spring-cloud-consul-3.0.x-ci')
				name('spring-cloud-contract-3.0.x-ci')
				name('spring-cloud-function-3.1.x-ci')
				name('spring-cloud-gateway-3.0.x-ci')
				name('spring-cloud-kubernetes-2.0.x-ci')
				name('spring-cloud-netflix-3.0.x-ci')
				name('spring-cloud-openfeign-3.0.x-ci')
				name('spring-cloud-release-2020.0.x-ci')
				name('spring-cloud-sleuth-3.0.x-ci')
				name('spring-cloud-stream-3.1.x-ci')
				name('spring-cloud-task-2.3.x-ci')
				name('spring-cloud-vault-3.0.x-ci')
				name('spring-cloud-zookeeper-3.0.x-ci')
			}
			columns defaultColumns()
		}
		listView('CI.Jubilee') {
			jobs {
				name('spring-cloud-build-main-ci')
				name('spring-cloud-bus-main-ci')
				name('spring-cloud-circuitbreaker-main-ci')
				name('spring-cloud-cli-main-ci')
				name('spring-cloud-cloudfoundry-main-ci')
				name('spring-cloud-commons-main-ci')
				name('spring-cloud-config-main-ci')
				name('spring-cloud-consul-main-ci')
				name('spring-cloud-contract-main-ci')
				name('spring-cloud-function-main-ci')
				name('spring-cloud-gateway-main-ci')
				name('spring-cloud-kubernetes-main-ci')
				name('spring-cloud-netflix-main-ci')
				name('spring-cloud-openfeign-main-ci')
				name('spring-cloud-release-main-ci')
				name('spring-cloud-sleuth-main-ci')
				name('spring-cloud-stream-main-ci')
				name('spring-cloud-task-main-ci')
				name('spring-cloud-vault-main-ci')
				name('spring-cloud-zookeeper-main-ci')
			}
			columns defaultColumns()
		}
		listView('JDK') {
			jobs {
				regex('spring-cloud.*jdk.*')
			}
			columns defaultColumns()
		}
		listView('E2E') {
			jobs {
				regex('spring-cloud.*-e2e')
			}
			columns defaultColumns()
		}
		listView('Releaser') {
			jobs {
				regex('spring-cloud.*-releaser')
			}
			columns defaultColumns()
		}
		listView('Sonar') {
			jobs {
				regex('spring-cloud.*-sonar')
			}
			columns defaultColumns()
		}
		listView('QA') {
			jobs {
				regex('spring-cloud.*-qa-.*')
			}
			columns defaultColumns()
		}
		nestedView('CD-pipelines') {
			def nested = delegate
			['github-analytics', 'github-webhook', 'app-monolith'].each {
				String artifactName = it
				String projectName = "${artifactName}-pipeline"
				nested.views {
					deliveryPipelineView(projectName) {
						allowPipelineStart()
						pipelineInstances(5)
						showAggregatedPipeline(false)
						columns(1)
						updateInterval(5)
						enableManualTriggers()
						showAvatars()
						showChangeLog()
						pipelines {
							component("Deploy ${artifactName} to production", "${projectName}-build")
						}
						allowRebuild()
						showDescription()
						showPromotions()
						showTotalBuildTime()
						configure {
							(it / 'showTestResults').setValue(true)
							(it / 'pagingEnabled').setValue(true)
						}
					}
				}
			}
		}
		listView('All Cloud') {
			jobs {
				regex('spring-cloud.*')
			}
			columns defaultColumns()
		}
	}
}

private Closure defaultColumns() {
	return {
		status()
		name()
		lastSuccess()
		lastFailure()
		lastBuildConsole()
		buildButton()
	}
}