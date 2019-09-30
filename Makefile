CURRENT=$(shell pwd)
NAME := $(or $(APP_NAME),$(shell basename $(CURRENT)))
OS := $(shell uname)

RELEASE_VERSION := $(or $(shell cat VERSION), $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout))
GROUP_ID := $(shell mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout)
ARTIFACT_ID := $(shell mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)
RELEASE_ARTIFACT := $(GROUP_ID):$(ARTIFACT_ID)

updatebot/push: 
	@echo doing updatebot push $(RELEASE_VERSION)
	updatebot push --ref $(RELEASE_VERSION)


updatebot/push-version: 
	@echo Resolving push versions for artifacts........
	$(eval ACTIVITI_CORE_COMMON_VERSION=$(shell mvn help:evaluate -Dexpression=activiti-core-common.version -q -DforceStdout))
	$(eval ACTIVITI_API_VERSION=$(shell mvn help:evaluate -Dexpression=activiti-api.version -q -DforceStdout))
	$(eval ACTIVITI_BUILD_VERSION=$(shell mvn help:evaluate -Dexpression=activiti-build.version -q -DforceStdout))

	@echo Doing updatebot push-version.....
	updatebot push-version --kind maven \
		org.activiti.dependencies:activiti-dependencies $(RELEASE_VERSION) \
		org.activiti.api:activiti-api-dependencies $(ACTIVITI_API_VERSION) \
		org.activiti.core.common:activiti-core-common-dependencies $(ACTIVITI_CORE_COMMON_VERSION) \
		org.activiti.build:activiti-parent $(ACTIVITI_BUILD_VERSION)
	updatebot push-version --kind  make  ACTIVITI_CORE_DEPENDENCIES_VERSION $(RELEASE_VERSION)	

updatebot/update:
	@echo doing updatebot update $(RELEASE_VERSION)
	updatebot update

updatebot/update-loop:
	@echo doing updatebot update-loop $(RELEASE_VERSION)
	updatebot update-loop --poll-time-ms 60000

