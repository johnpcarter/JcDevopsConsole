package com.jc.devops.funcs

// repoUrl - e.g. https://github.com/johnpcarter/api-deployment.git
def checkoutAPIs(repo, repoAccount) {
	def repoUrl = "https://github.com/${repoAccount}/${repo}.git"

	checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'src']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: repoAccount, url: repoUrl]]])
}