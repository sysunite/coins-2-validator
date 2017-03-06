package com.sysunite.coinsweb;


import com.sysunite.coinsweb.plugin.Plugin;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Application {

  public static void main(String[] args) {

    System.out.println("list all found plugins");

    String basePath = "/Users/bastiaanbijl/Documents/Sysunite/GitHub/Sysunite/coins-2-validator/";

    String pluginPath = basePath + "plugins";
    List<File> candidates = Arrays.asList(new File(pluginPath).listFiles());
    for(File candidate : candidates) {
      if(candidate.isFile() && candidate.getName().endsWith(".jar")) {

        System.out.println("try "+candidate.toString());

        try {

          URLClassLoader child = new URLClassLoader(new URL[]{candidate.toURI().toURL()});
          Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("com.sysunite.coinsweb.plugin", child)).addClassLoader(child));

          Set<Class<? extends Plugin>> pluginClasses = reflections.getSubTypesOf(Plugin.class);

          for(Class pluginClass : pluginClasses) {
            Plugin plugin = (Plugin) pluginClass.newInstance ();
            System.out.println(plugin.getClass().getName());
            plugin.init();
          }


        } catch (MalformedURLException e) {
          e.printStackTrace();
        } catch (InstantiationException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
