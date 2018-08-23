package com.wurmcraft.common.support.utils;

import com.wurmcraft.api.WurmTweak2API;
import com.wurmcraft.api.script.converter.IDataConverter;

public class Converter {

  public IDataConverter attemptToFindConverter(String line) {
    for (IDataConverter converter : WurmTweak2API.dataConverters) {
      if (converter.isValid(line)) {
        return converter;
      }
    }
    return null;
  }

  public Object convert(String line, int extraData) {
    IDataConverter converter = attemptToFindConverter(line);
    if (converter != null) {
      return converter.getData(line);
    }
    return null;
  }

  public Object convert(String line) {
    return convert(line, 0);
  }

  public static IDataConverter getFromName(String name) {
    for (IDataConverter a : WurmTweak2API.dataConverters) {
      if (a.getName().equalsIgnoreCase(name)) {
        return a;
      }
    }
    return null;
  }

}