/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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


package org.activiti.engine.impl.variable;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.util.ReflectUtil;


public class JPAEntityMappings {

  private Map<String, EntityMetaData> classMetaDatamap;

  private JPAEntityScanner enitityScanner;

  public JPAEntityMappings() {
    classMetaDatamap = new HashMap<String, EntityMetaData>();
    enitityScanner = new JPAEntityScanner();
  }

  public boolean isJPAEntity(Object value) {
    if (value != null) {
      // EntityMetaData will be added for all classes, even those who are
      // not
      // JPA-entities to prevent unneeded annotation scanning
      return getEntityMetaData(value.getClass()).isJPAEntity();
    }
    return false;
  }

  public EntityMetaData getEntityMetaData(Class<?> clazz) {
    EntityMetaData metaData = classMetaDatamap.get(clazz.getName());
    if (metaData == null) {
      // Class not present in meta-data map, create metaData for it and
      // add
      metaData = scanClass(clazz);
      classMetaDatamap.put(clazz.getName(), metaData);
    }
    return metaData;
  }

  private EntityMetaData scanClass(Class<?> clazz) {
    return enitityScanner.scanClass(clazz);
  }

  public String getJPAClassString(Object value) {
    if (value == null) {
      throw new ActivitiIllegalArgumentException("null value cannot be saved");
    }

    EntityMetaData metaData = getEntityMetaData(value.getClass());
    if (!metaData.isJPAEntity()) {
      throw new ActivitiIllegalArgumentException("Object is not a JPA Entity: class='" + value.getClass() + "', " + value);
    }

    // Extract the class from the Entity instance
    return metaData.getEntityClass().getName();
  }

  public String getJPAIdString(Object value) {
    EntityMetaData metaData = getEntityMetaData(value.getClass());
    if (!metaData.isJPAEntity()) {
      throw new ActivitiIllegalArgumentException("Object is not a JPA Entity: class='" + value.getClass() + "', " + value);
    }
    Object idValue = getIdValue(value, metaData);
    return getIdString(idValue);
  }

  public Object getIdValue(Object value, EntityMetaData metaData) {
    try {
      if (metaData.getIdMethod() != null) {
        return metaData.getIdMethod().invoke(value);
      } else if (metaData.getIdField() != null) {
        return metaData.getIdField().get(value);
      }
    } catch (IllegalArgumentException iae) {
      throw new ActivitiException("Illegal argument exception when getting value from id method/field on JPAEntity", iae);
    } catch (IllegalAccessException iae) {
      throw new ActivitiException("Cannot access id method/field for JPA Entity", iae);
    } catch (InvocationTargetException ite) {
      throw new ActivitiException("Exception occurred while getting value from id field/method on JPAEntity: " + ite.getCause().getMessage(), ite.getCause());
    }

    // Fall trough when no method and field is set
    throw new ActivitiException("Cannot get id from JPA Entity, no id method/field set");
  }

  public Object getJPAEntity(String className, String idString) {
    Class<?> entityClass = null;
    entityClass = ReflectUtil.loadClass(className);

    EntityMetaData metaData = getEntityMetaData(entityClass);

    // Create primary key of right type
    Object primaryKey = createId(metaData, idString);
    return findEntity(entityClass, primaryKey);
  }

  private Object findEntity(Class<?> entityClass, Object primaryKey) {
    EntityManager em = Context.getCommandContext().getSession(EntityManagerSession.class).getEntityManager();

    Object entity = em.find(entityClass, primaryKey);
    if (entity == null) {
      throw new ActivitiException("Entity does not exist: " + entityClass.getName() + " - " + primaryKey);
    }
    return entity;
  }

  public Object createId(EntityMetaData metaData, String string) {
    Class<?> type = metaData.getIdType();
    // According to JPA-spec all primitive types (and wrappers) are
    // supported, String, util.Date, sql.Date,
    // BigDecimal and BigInteger
    if (type == Long.class || type == long.class) {
      return Long.parseLong(string);
    } else if (type == String.class) {
      return string;
    } else if (type == Byte.class || type == byte.class) {
      return Byte.parseByte(string);
    } else if (type == Short.class || type == short.class) {
      return Short.parseShort(string);
    } else if (type == Integer.class || type == int.class) {
      return Integer.parseInt(string);
    } else if (type == Float.class || type == float.class) {
      return Float.parseFloat(string);
    } else if (type == Double.class || type == double.class) {
      return Double.parseDouble(string);
    } else if (type == Character.class || type == char.class) {
      return new Character(string.charAt(0));
    } else if (type == java.util.Date.class) {
      return new java.util.Date(Long.parseLong(string));
    } else if (type == java.sql.Date.class) {
      return new java.sql.Date(Long.parseLong(string));
    } else if (type == BigDecimal.class) {
      return new BigDecimal(string);
    } else if (type == BigInteger.class) {
      return new BigInteger(string);
    } else if (type == UUID.class) {
      return UUID.fromString(string);
    } else {
      throw new ActivitiIllegalArgumentException("Unsupported Primary key type for JPA-Entity: " + type.getName());
    }
  }

  public String getIdString(Object value) {
    if (value == null) {
      throw new ActivitiIllegalArgumentException("Value of primary key for JPA-Entity cannot be null");
    }
    // Only java.sql.date and java.util.date require custom handling, the
    // other types
    // can just use toString()
    if (value instanceof java.util.Date) {
      return "" + ((java.util.Date) value).getTime();
    } else if (value instanceof java.sql.Date) {
      return "" + ((java.sql.Date) value).getTime();
    } else if (value instanceof Long || value instanceof String || value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Float || value instanceof Double
        || value instanceof Character || value instanceof BigDecimal || value instanceof BigInteger || value instanceof UUID) {
      return value.toString();
    } else {
      throw new ActivitiIllegalArgumentException("Unsupported Primary key type for JPA-Entity: " + value.getClass().getName());
    }
  }
}
