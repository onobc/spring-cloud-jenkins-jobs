package springcommon

import javaposse.jobdsl.dsl.DslFactory

DslFactory dsl = this

dsl.listView('Seeds') {
	jobs {
		regex('.*-seed')
	}
	columns defaultColumns()
}

dsl.nestedView('Common') {
	views {
		listView('Common Libraries') {
			jobs {
				regex('jenkins-common.*-ci')
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