package org.springframework.jenkins.cloud.common

import groovy.transform.CompileStatic

/**
 * @author Marcin Grzejszczak
 */
@CompileStatic
trait HashicorpTrait {

	String preVaultShell() {
		return ''' #!/bin/bash
					echo "Kill Vault"
					pkill vault && echo "Vault killed" || echo "No Vault process was running"

					echo "Install Vault"
					./src/test/bash/create_certificates.sh
					./src/test/bash/install_vault.sh

					echo "Run Vault"
					./src/test/bash/local_run_vault.sh &
				'''
	}

	String postVaultShell() {
		return """pkill vault && echo 'Vault killed' || echo 'No Vault process was running';"""
	}
}