/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.4/userguide/building_swift_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "it.unicam.quasylab.sibilla"

include("core:simulator")
include("core:network")
include("core:optimization")
include("langs:util")
include("langs:pm")
include("langs:lio")
include("langs:markov")
include("langs:yoda")
include("langs:slam")
include("tools:tracing")
include("tools:synthesis")
include("tools:monitoring")
include("core:runtime")
include("shell")
