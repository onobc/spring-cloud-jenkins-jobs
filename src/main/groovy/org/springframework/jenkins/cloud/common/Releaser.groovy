package org.springframework.jenkins.cloud.common

import org.springframework.jenkins.cloud.release.ReleaserOptions

/**
 * @author Marcin Grzejszczak
 */
trait Releaser {

	public static final String RELEASE_VERSION_PARAM = "RELEASE_VERSION"
	public static final String RELEASER_CONFIG_URL_PARAM = "RELEASER_CONFIG_URL"
	public static final String RELEASER_CONFIG_BRANCH_PARAM = "RELEASER_CONFIG_BRANCH"
	public static final String RELEASER_BRANCH_PARAM = "RELEASER_BRANCH"

	String buildReleaserForSingleProject(ReleaserOptions options) {
		return '''\
			currentDir="$(pwd)"
			tmpDir="$(mktemp -d)"
			trap "{ rm -rf ${tmpDir}; }" EXIT
			echo "Cloning to [${tmpDir}] and building the releaser"
			git clone -b "${RELEASER_BRANCH:-main}" --single-branch https://github.com/spring-cloud/spring-cloud-release-tools.git "${tmpDir}"
			pushd "${tmpDir}"
				rm -rf ~/.m2/repository/org/springframework/cloud
				ROOT_VIEW="Spring%20Cloud"
				CURRENT_VIEW="Releaser"
				echo "Building the releaser. If the build fails after this then it means that the releaser failed to get built. Then please check the build's workspace under [.git/releaser.log] for logs. You can click here to see it [${JENKINS_URL}/view/${ROOT_VIEW}/view/${CURRENT_VIEW}/job/${JOB_NAME}/ws/.git/releaser.log]"
				./mvnw clean install -DskipTests -am -pl :''' + options.projectName + ''' > "${currentDir}/.git/releaser.log"
			popd'''
	}

	String fetchConfigurationFile(String folder) {
		return """\
			version=\$( echo "\$${RELEASE_VERSION_PARAM}" | tr '[:upper:]' '[:lower:]' | tr '.' '_' )
			configFile="\${version}.properties"
			configUrl="\${$RELEASER_CONFIG_URL_PARAM}/\${$RELEASER_CONFIG_BRANCH_PARAM}/\${configFile}"
			echo "Downloading the configuration properties file from [\${configUrl}]"
			rm -rf config && mkdir -p ${folder} && curl --fail "\${configUrl}" -o ${folder}/application.properties
			echo "Cause nobody remembers to set the releaser branch properly, I'll do it for you"
			if [[ \$version == *"hoxton"* ]]; then
				echo "You're using the Hoxton release train, I'm switching the releaser branch [\$${RELEASER_BRANCH_PARAM}] to [1.0.x] !"
				${RELEASER_BRANCH_PARAM}="1.0.x"
			fi
		"""
	}


}