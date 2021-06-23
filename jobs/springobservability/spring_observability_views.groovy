package springobservability

import javaposse.jobdsl.dsl.DslFactory

DslFactory dsl = this

dsl.listView('Seeds') {
	jobs {
		regex('.*-seed')
	}
	columns defaultColumns()
}

dsl.nestedView('Observability') {
	views {
		listView('CI') {
			jobs {
				regex('spring-observability.*-ci')
			}
			columns defaultColumns()
		}
		listView('CI.MAIN') {
			jobs {
				regex('spring-observability.*main-ci')
			}
			columns defaultColumns()
		}
		listView('Releaser') {
			jobs {
				regex('spring-observability.*-releaser')
			}
			columns defaultColumns()
		}
		listView('QA') {
			jobs {
				regex('spring-observability.*-qa-.*')
			}
			columns defaultColumns()
		}
		listView('All Observability') {
			jobs {
				regex('spring-observability.*')
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