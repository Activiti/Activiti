/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.api.runtime.model.impl;

import java.util.Objects;

import org.activiti.api.model.shared.model.ApplicationElement;

public class ApplicationElementImpl implements ApplicationElement {

    private String appVersion;

    @Override
    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationElementImpl that = (ApplicationElementImpl) o;
        return Objects.equals(appVersion,
                              that.appVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appVersion);
    }

    @Override
    public String toString() {
        return "ApplicationElementImpl{" +
                "appVersion='" + appVersion + '\'' +
                '}';
    }
}
