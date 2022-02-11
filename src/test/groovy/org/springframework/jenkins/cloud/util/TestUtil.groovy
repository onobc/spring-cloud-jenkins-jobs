package org.springframework.jenkins.cloud.util

import groovy.io.FileType
import groovy.transform.CompileStatic

@CompileStatic
class TestUtil {

	static List<File> getJobFiles() {
		List<File> files = []
		new File('jobs/').eachFileRecurse(FileType.FILES) {
			if (it.name.endsWith('.groovy')) {
				files << it
			}
		}
		return files
	}

	/**
	 * Write a single XML file, creating any nested dirs.
	 */
	static void writeFile(File dir, String name, String xml) {
		String[] tokens = name.split('/')
		File folderDir = tokens[0..<-1].inject(dir) { File tokenDir, String token ->
			new File(tokenDir, token)
		}
		folderDir.mkdirs()
		File xmlFile = new File(folderDir, "${tokens[-1]}.xml")
		xmlFile.text = xml
	}
}
