package org.springframework.jenkins.cloud.release

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.jobs.FreeStyleJob

/**
 * @author Marcin Grzejszczak
 */
class SpringCloudReleaseMasterMaker extends SpringCloudReleaseMaker {

	SpringCloudReleaseMasterMaker(DslFactory dsl) {
		super(dsl)
	}

	SpringCloudReleaseMasterMaker(DslFactory dsl, String organization) {
		super(dsl, organization)
	}

	@Override
	protected String projectName(String project) {
		return "${project}-${masterBranch()}-releaser"
	}

	@Override
	protected String branchToCheck() {
		return masterBranch()
	}

	@Override
	protected void additionalConfiguration(FreeStyleJob job) {
		job.triggers {
			cron oncePerDay()
		}
		job.parameters {
			booleanParam(RELEASER_SAGAN_UPDATE_VAR, false, 'If true then will update documentation repository with the current URL')
		}
	}

	@Override
	protected String scriptPreconditions() {
		return '''\
echo "Running version check"
VERSION=$( sed '\\!<parent!,\\!</parent!d' `pwd`/pom.xml | grep '<version' | head -1 | sed -e 's/.*<version>//' -e 's!</version>.*$!!' )
echo "The found version is [${VERSION}]"

if ! echo $VERSION | egrep -q 'SNAPSHOT'; then
    echo "Version is NOT SNAPSHOT, will not do anything. Something is wrong!"
    exit 1
else
	echo "Version is a SNAPSHOT one, will continue with the build"
fi
'''
	}

	@Override
	protected void configureLabels(FreeStyleJob job) {

	}
}
