package com.vast.gradle.plugin.docker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class DockerTask extends DefaultTask {
    DockerExtension ext

    @TaskAction
    def run() {
        if (ext.image == null)
            ext.image = project.name

        if (ext.version == null)
            ext.version = project.version

        assert ext.dockerfile != null

        def resultImage = ext.registry ? ext.registry + '/' : ''
        resultImage += "${ext.image}:${ext.version}"

        println "Preparing to build image $resultImage"

        project.exec {
            commandLine = ['docker', 'build', '-t', ext.image, ext.dockerfile]
            println '> ' + commandLine.join(' ')
        }

        if (ext.registry) {
            project.exec {
                commandLine = ['docker', 'tag', ext.image, resultImage]
                println '> ' + commandLine.join(' ')
            }

            if (ext.credentials) {
                project.exec {
                    commandLine = ['docker', 'login', '-u', ext.credentials[0], '-p', ext.credentials[1], ext.registry]
                    println '> docker login ' + ext.registry
                }
            }

            project.exec {
                commandLine = ['docker', 'push', resultImage]
                println '> ' + commandLine.join(' ')
            }

            project.exec {
                commandLine = ['docker', 'image', 'rm', resultImage]
                println '> ' + commandLine.join(' ')
            }

            project.exec {
                commandLine = ['docker', 'image', 'rm', ext.image]
                println '> ' + commandLine.join(' ')
            }
        }
    }
}
