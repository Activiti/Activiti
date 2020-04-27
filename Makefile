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
	@echo Doing updatebot push-version.....
	updatebot push-version --kind maven \
		org.activiti:activiti-dependencies $(RELEASE_VERSION) \
		org.activiti:activiti-api-dependencies $(RELEASE_VERSION) \
		org.activiti:activiti-core-common-dependencies $(RELEASE_VERSION)

updatebot/update:
	@echo doing updatebot update $(RELEASE_VERSION)
	updatebot update

updatebot/update-loop:
	@echo doing updatebot update-loop $(RELEASE_VERSION)
	updatebot update-loop --poll-time-ms 60000

