/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.activiti.osgi.blueprint;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ClassLoaderWrapper extends ClassLoader {

  private ClassLoader[] parents;

  public ClassLoaderWrapper(ClassLoader... parents) {
    this.parents = parents;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected synchronized Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
    //
    // Check if class is in the loaded classes cache
    //
    Class cachedClass = findLoadedClass(name);
    if (cachedClass != null) {
      if (resolve) {
        resolveClass(cachedClass);
      }
      return cachedClass;
    }

    //
    // Check parent class loaders
    //
    for (int i = 0; i < parents.length; i++) {
      ClassLoader parent = parents[i];
      try {
        Class clazz = parent.loadClass(name);
        if (resolve) {
          resolveClass(clazz);
        }
        return clazz;
      } catch (ClassNotFoundException ignored) {
        // this parent didn't have the class; try the next one
      }
    }

    throw new ClassNotFoundException(name);
  }

  /**
   * {@inheritDoc}
   */
  public URL getResource(String name) {
    //
    // Check parent class loaders
    //
    for (int i = 0; i < parents.length; i++) {
      ClassLoader parent = parents[i];
      URL url = parent.getResource(name);
      if (url != null) {
        return url;
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Enumeration findResources(String name) throws IOException {
    List resources = new ArrayList();

    //
    // Add parent resources
    //
    for (int i = 0; i < parents.length; i++) {
      ClassLoader parent = parents[i];
      List parentResources = Collections.list(parent.getResources(name));
      resources.addAll(parentResources);
    }

    return Collections.enumeration(resources);
  }

}
