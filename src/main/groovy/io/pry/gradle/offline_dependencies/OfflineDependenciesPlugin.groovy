package io.pry.gradle.offline_dependencies

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.internal.artifacts.BaseRepositoryFactory
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.reflect.Instantiator

class OfflineDependenciesPlugin implements Plugin<Project> {

  static final String EXTENSION_NAME = 'offlineDependencies'

  @Override
  void apply(Project project) {

    if (!project.hasProperty("offlineRepositoryRoot")) {
      project.ext.offlineRepositoryRoot = "${project.projectDir}/offline-repository"
    }

    RepositoryHandler repositoryHandler = new DefaultRepositoryHandler(
        project.services.get(BaseRepositoryFactory.class) as BaseRepositoryFactory,
        project.services.get(Instantiator.class) as Instantiator
    )

    def extension = project.extensions.create(EXTENSION_NAME, OfflineDependenciesExtension, repositoryHandler)
    project.logger.info("Offline dependencies root configured at '${project.ext.offlineRepositoryRoot}'")
    def odTask = project.task('updateOfflineRepository', type: UpdateOfflineRepositoryTask) {
      conventionMapping.root = { "${project.offlineRepositoryRoot}" }
      conventionMapping.configurationNames = { extension.configurations }
      conventionMapping.buildscriptConfigurationNames = { extension.buildscriptConfigurations }
      conventionMapping.includeSources = { extension.includeSources }
      conventionMapping.includeJavadocs = { extension.includeJavadocs }
      conventionMapping.includePoms = { extension.includePoms }
      conventionMapping.includeIvyXmls = { extension.includeIvyXmls }
      conventionMapping.includeBuildscriptDependencies = { extension.includeBuildscriptDependencies }
    }
    odTask.group = "packing source"
    odTask.description = "packing all projects dependencies and source code"


    Task srcZipTask = project.task("srcZip", type: Zip){
      from project.projectDir
      exclude '*.iml','import-summary.txt','local.properties',
              '.gradle/','.idea/','build/','.svn/','.DS_Store',
              '.git/','/*/build/',project.name+'.zip'
      archiveName project.name+'.zip'
    }
    srcZipTask.group = "packing source"
    srcZipTask.description = "packing all projects dependencies and source code"


  }
}
