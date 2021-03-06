/*

    gl-config  Property and environment variable configuration module.
    Copyright (c) 2012-2015 National Marrow Donor Program (NMDP)

    This library is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation; either version 3 of the License, or (at
    your option) any later version.

    This library is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
    License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this library;  if not, write to the Free Software Foundation,
    Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.

    > http://www.fsf.org/licensing/licenses/lgpl.html
    > http://www.opensource.org/licenses/lgpl-license.php

*/
package org.nmdp.gl.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.inject.name.Names.named;
import static java.lang.String.format;

import java.io.InputStream;
import java.io.IOException;

import java.lang.annotation.Annotation;

import java.util.Map;
import java.util.Properties;

import com.google.inject.ProvisionException;

import org.nnsoft.guice.rocoto.configuration.binder.PropertyValueBindingBuilder;

/**
 * Configuration module.
 */
public abstract class ConfigurationModule
    extends org.nnsoft.guice.rocoto.configuration.ConfigurationModule {

    /**
     * Bind properties from the specified classpath resource property file, overriding those values
     * with system properties and with environment variables after replacing key underscores '_' by dots '.'.
     *
     * @param propertyFile classpath resource property file, must not be null
     */
    protected final void bindPropertiesWithOverrides(final String propertyFile) {
        checkNotNull(propertyFile, "classpath resource property file must not be null");

        Properties properties = new Properties();

        // load classpath resource properties
        InputStream inputStream = getClass().getResourceAsStream(propertyFile);
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            }
            catch (IOException e) {
                // ignore
            }
        }

        // override with system properties
        properties.putAll(System.getProperties());

        // override with environment variables
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String reformattedKey = entry.getKey().replace('_', '.');

            // only replace existing keys
            if (properties.containsKey(reformattedKey)) {
                properties.put(reformattedKey, entry.getValue());
            }
        }

        // bind merged properties
        bindProperties(properties);
    }


    @Override
    protected final PropertyValueBindingBuilder bindProperty(final String name) {
        checkNotNull(name, "property name must not be null");
        return new PropertyValueBindingBuilder() {
            @Override
            public void toValue(final String value) {
                checkNotNull(value, "value for property '%s' must not be null", name);
                if (looksLikeAnnotationName(name)) {
                    bindConstant().annotatedWith(annotation(name)).to(value);
                }
                else {
                    bindConstant().annotatedWith(named(name)).to(value);
                }
            }
        };
    }

    /**
     * Return true if the specified property name looks like an annotation name.
     *
     * @param name property name
     * @return true if the specified property name looks like an annotation name
     */
    protected static final boolean looksLikeAnnotationName(final String name) {
        return name.startsWith("org.nmdp") && Character.isUpperCase(name.charAt(name.lastIndexOf(".") + 1));
    }

    /**
     * Return an instance of an annotation with the specified class name.
     *
     * @param annotationClassName annotation class name
     * @return an instance of an annotation with the specified class name
     */
    @SuppressWarnings("unchecked")
    protected static final <A extends Annotation> Class<A> annotation(final String annotationClassName) {
        try {
            return (Class<A>) Class.forName(annotationClassName);
        }
        catch (ClassNotFoundException e) {
            throw new ProvisionException(format("could not create annotation class '%s': %s", annotationClassName, e.getMessage()));
        }
    }
}
