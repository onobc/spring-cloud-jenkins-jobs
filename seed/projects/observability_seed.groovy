job('observability-seed') {
    triggers {
        githubPush()
    }
    jdk("jdk8")
    scm {
        git {
            remote {
                github('spring-cloud/spring-cloud-jenkins-jobs')
            }
            branch("main")
        }
    }
    steps {
        gradle("clean build")
        dsl {
            external('jobs/springobservability/*.groovy')
            removeAction('DISABLE')
            removeViewAction('DELETE')
            ignoreExisting(false)
            additionalClasspath([
                    'src/main/groovy', 'src/main/resources', 'build/lib/*.jar'
            ].join("\n"))
        }
    }
}