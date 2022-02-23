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
	 * List of all Spring Cloud Stream jobs for the releaser. This list will be used to create the boot compatibility builds
	 * and will serve as basis for the default jobs
	 */
	public static final List<String> ALL_STREAM_JOBS_FOR_RELEASER = ["spring-cloud-stream",
																	 "spring-cloud-stream-binder-rabbit",
																	 "spring-cloud-stream-binder-kafka",
																	 "spring-cloud-schema-registry"]

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

}
