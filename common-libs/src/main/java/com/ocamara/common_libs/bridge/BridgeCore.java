package com.ocamara.common_libs.bridge;

import java.util.HashMap;

public class BridgeCore {
   public static final String APP_MODEL = "app_model";

   private HashMap<String, Object> mInterfaceImplMap = new HashMap();
   private static volatile BridgeCore instance;

   private BridgeCore() {
   }

   public static BridgeCore getInstance() {
      if (instance == null) {
         synchronized (BridgeCore.class) {
            if (instance == null) {
               instance = new BridgeCore();
            }
         }
      }
      return instance;
   }

   public void registerApp(AppInterface impl) {
      mInterfaceImplMap.put(APP_MODEL, impl);
   }

   public AppInterface getAppInterface() {
      return ((AppInterface) mInterfaceImplMap.get(APP_MODEL));
   }

}
