package com.avaje.ebeaninternal.server.type;

import java.lang.reflect.Type;

/**
 * Factory for platform specific handling/ScalarTypes for DB ARRAY.
 */
public interface PlatformArrayTypeFactory {

  /**
   * Return the ScalarType to handle DB ARRAY for the given element type.
   */
  ScalarType<?> typeFor(Type valueType);

}
