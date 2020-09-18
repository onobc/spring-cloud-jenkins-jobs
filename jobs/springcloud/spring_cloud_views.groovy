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
		listView('CI.MASTER') {
			jobs {
				regex('spring-cloud.*master-ci')
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