package org.springframework.jenkins.cloud.common


import groovy.transform.CompileStatic

/**
 * Contains lists of jobs. By default we create the jobs and views in the following way
 *
 * ${project-name}-${branch-name}-ci
 *
 * e.g.
 *
 * spring-cloud-sleuth-main-ci
 * spring-cloud-netflix-1.0.x-ci
 *
 * @author Marcin Grzejszczak
 */
@CompileStatic
class AllCloudJobs {
	/**
	 * List of all Spring Cloud jobs. This list will be used to create the boot compatibility builds
	 * and will serve as basis for the default jobs
	 */
	public static final List<String> ALL_JOBS = ['spring-cloud-netflix', 'spring-cloud-zookeeper', 'spring-cloud-consul',
												 'spring-cloud-bus', 'spring-cloud-commons', 'spring-cloud-config',
												 'spring-cloud-cloudfoundry', 'spring-cloud-build',
												 'spring-cloud-cli', 'spring-cloud-contract', 'spring-cloud-vault', 'spring-cloud-gateway',
												 'spring-cloud-openfeign', 'spring-cloud-function', 'spring-cloud-kubernetes',
	                                             'spring-cloud-circuitbreaker', 'spring-cloud-release', 'spring-cloud-task']
	/**
	 * List of all Spring Cloud Stream jobs for the releaser. This list will be used to create the boot compatibility builds
	 * and will serve as basis for the default jobs
	 */
	public static final List<String> ALL_STREAM_JOBS_FOR_RELEASER = ["spring-cloud-stream",
																	 "spring-cloud-stream-binder-rabbit",
																	 "spring-cloud-stream-binder-kafka",
																	 "spring-cloud-schema-registry"]

	public static final List<String> ALL_JOBS_WITH_NO_MAIN_BRANCH_ACTIVE_IN_MAINTAINED_RELEASE_TRAIN = [
	        "spring-cloud-cli", "spring-cloud-cloudfoundry", "spring-cloud-sleuth"
	]

	/**
	 * List of all single project jobs to be used by the main releaser
	 */
	public static final List<String> ALL_MAIN_RELEASER_JOBS = ALL_JOBS - ALL_JOBS_WITH_NO_MAIN_BRANCH_ACTIVE_IN_MAINTAINED_RELEASE_TRAIN

	/**
	 * List of all single project jobs to be used by the releaser
	 */
	public static final List<String> ALL_RELEASER_JOBS = ALL_JOBS
	/**
	 * Some projects need to have the test report generation skipped (since they have no tests).
	 */
	public static final List<String> JOBS_WITHOUT_TESTS = ['spring-cloud-build', 'spring-cloud-release']

	/**
	 * Projects from this list will have the jobs with report generation
	 */
	public static final List<String> ALL_JOBS_WITH_TESTS = ALL_JOBS - JOBS_WITHOUT_TESTS

	/**
	 * Apart from projects containing libraries we also do have the samples. Currently the list
	 * is not really impressive but at least we have a hook for that
	 */
	public static final List<String> ALL_SAMPLES_JOBS = ['tests']

	/**
	 * There are some projects that require custom setup / teardown. Provide the list here.
	 * That way the default CI jobs will not get generated. You can see that there are duplicates
	 * in this list and {@link AllCloudJobs#ALL_JOBS}. That's intentional cause we need the list
	 * of names of all jobs that we have in the organization. Since some jobs are custom
	 * we will have custom implementations. Check out {@link org.springframework.jenkins.cloud.compatibility.ManualBootCompatibilityBuildMaker}
	 * for more info.
	 */
	public static final List<String> CUSTOM_BUILD_JOBS = ['spring-cloud-build', 'spring-cloud-contract',
														  'spring-cloud-netflix', 'spring-cloud-vault']

	/**
	 * {@link AllCloudJobs#ALL_DEFAULT_JOBS} creates jobs for main branch. Sometimes you need other branches.
	 * That's why it's enough to provide the name of the project and the list of branches to build
	 */
	public static final Map<String, List<String>> JOBS_WITH_BRANCHES = ['spring-cloud-aws'           : ['2.2.x'],
																		'spring-cloud-build'         : ['2.3.x', '3.0.x', '3.1.x'],
																		'spring-cloud-bus'           : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-circuitbreaker': ['1.0.x', '2.0.x', '2.1.x'],
																		'spring-cloud-cli'           : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-cloudfoundry'  : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-commons'       : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-config'        : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-consul'        : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-contract'      : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-function'      : ['3.0.x', '3.1.x', '3.2.x'],
																		'spring-cloud-gateway'       : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-kubernetes'    : ['1.1.x', '2.0.x', '2.1.x'],
																		'spring-cloud-netflix'       : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-openfeign'     : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-security'      : ['2.2.x'],
																		'spring-cloud-sleuth'        : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-task'          : ['2.2.x', '2.3.x', '2.4.x'],
																		'spring-cloud-vault'         : ['2.2.x', '3.0.x', '3.1.x'],
																		'spring-cloud-zookeeper'     : ['2.2.x', '3.0.x', '3.1.x']]

	public static final List<String> INCUBATOR_JOBS = ['spring-cloud-sleuth-otel',
													   'spring-cloud-square']


	public static final Map<String, List<String>> INCUBATOR_JOBS_WITH_BRANCHES = ['spring-cloud-sleuth-otel'      : ['1.0.x']]

	/**
	 * Mapping of a lowercase release train name to a given boot version ordered in time. If you provide
	 * full MAJOR.MINOR.PATCH then we will use exactly that version.
	 *
	 * IMPORTANT: The latest Boot needs to be the first entry in the map.
	 */
	public static final Map<String, String> RELEASE_TRAIN_TO_BOOT_VERSION_MINOR = [
			// TODO: 2020.0 is the train, 2020.0.0 is like Hoxton.RELEASE not Hoxton
			"2022.0"   : "3.0",
			"2021.0"   : "2.6",
			"2020.0"   : "2.5",
			"hoxton"   : "2.3",
			"greenwich": "2.1"
	]

	static String bootForReleaseTrain(String releaseTrain) {
		String defaultBoot = RELEASE_TRAIN_TO_BOOT_VERSION_MINOR.entrySet().first().value
		if (!releaseTrain) {
			return defaultBoot
		}
		// release trains can now have dots so splitting won't work
		def lastDot = releaseTrain.lastIndexOf(".")
		String train = lastDot == -1 ? releaseTrain : releaseTrain.substring(0, lastDot);
		return RELEASE_TRAIN_TO_BOOT_VERSION_MINOR.get(train.toLowerCase()) ?: defaultBoot
	}

	/**
	 * List of default jobs. Default means that `./mvnw clean deploy` will be executed to publish artifacts.
	 */
	public static final List<String> ALL_DEFAULT_JOBS = ALL_JOBS - CUSTOM_BUILD_JOBS

	/**
	 * List of jobs that don't need boot compatibility tests.
	 */
	public static final List<String> JOBS_WITHOUT_BOOT_COMPATIBILITY = ['spring-cloud-cli']

	/**
	 * List of all jobs that need to be executed when doing compatibility builds against
	 * latest version of boot. This is a list of names of jobs. The proper implementations
	 * like {@link org.springframework.jenkins.cloud.compatibility.ManualBootCompatibilityBuildMaker} or
	 * {@link org.springframework.jenkins.cloud.compatibility.BootCompatibilityBuildMaker} will try
	 * to execute the jobs having those predefined names (with a proper suffix). It's up to
	 * the implementors to ensure that those jobs really exist.
	 */
	public static final List<String> BOOT_COMPATIBILITY_BUILD_JOBS = ALL_JOBS + ALL_SAMPLES_JOBS - JOBS_WITHOUT_BOOT_COMPATIBILITY

}
