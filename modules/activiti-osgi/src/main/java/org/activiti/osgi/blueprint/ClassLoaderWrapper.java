package org.activiti.osgi.blueprint;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: 11/5/10
 * Time: 11:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class ClassLoaderWrapper extends ClassLoader {

    private ClassLoader[] parents;

    public ClassLoaderWrapper(ClassLoader... parents) {
        this.parents = parents;
    }

    /**
     * {@inheritDoc}
     */
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
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
