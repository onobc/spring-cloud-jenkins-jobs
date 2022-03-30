package org.springframework.jenkins.cloud.common

import groovy.transform.CompileStatic
import org.springframework.jenkins.common.job.JdkConfig

@CompileStatic
class StreamProject extends Project implements JdkConfig, SpringCloudJobs {
    @Override
    List<String> customBuildCommand(BuildContext context) {
        String scriptDir = "binders/rabbit-binder/ci-docker-compose"
        String startScript = "docker-compose-RABBITMQ.sh"
        String stopScript = "docker-compose-RABBITMQ-stop.sh"
        List<String> cmd = []
        cmd.add("""\
        ${scriptToExecute(scriptDir, startScript)}
        ${context.upload ? cleanDeployWithDocs() : cleanInstallWithoutDocs()}
        ${scriptToExecute(scriptDir, stopScript)} 
        """ as String)
        return cmd
    }

    String scriptToExecute(String scriptDir, String script) {
        return """
                        echo "cd to ${scriptDir}"
                        cd ${scriptDir}
						echo "Running script"
						bash ${script}
                        cd -
					"""
    }

}
