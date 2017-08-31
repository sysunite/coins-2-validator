package com.sysunite.coinsweb.parser.profile.factory;

import com.sysunite.coinsweb.parser.profile.pojo.Bundle;
import com.sysunite.coinsweb.parser.profile.pojo.ProfileFile;
import com.sysunite.coinsweb.parser.profile.pojo.Query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bastbijl, Sysunite 2017
 */
public class ProfileFactory {

  public static Set<String> usedVars(ProfileFile profileFile) {
    HashSet<String> result = new HashSet();
    for(Bundle bundle : profileFile.getBundles()) {
      result.addAll(usedVars(bundle));
    }
    return result;
  }
  public static Set<String> usedVars(Bundle bundle) {
    HashSet<String> result = new HashSet();
    Pattern pattern = Pattern.compile("(?<=\\$\\{)([^\\}]+)(?=\\})");
    for(Query query : bundle.getQueries()) {
      Matcher matcher = pattern.matcher(query.getQuery());
      while(matcher.find()) {
        result.add(matcher.group());
      }
    }
    return result;
  }

  /**
   * Maps inference code to the set of vars needed for the inference
   */
  public static Map<String, Set<String>> inferencesOverVars(ProfileFile profileFile) {
    HashMap<String, Set<String>> result = new HashMap<>();
    for(Bundle bundle : profileFile.getBundles()) {
      if(Bundle.INFERENCE.equals(bundle.getType())) {
        result.put(inferenceCode(profileFile, bundle), usedVars(bundle));
      }
    }
    return result;
  }

  public static String inferenceCode(ProfileFile profileFile, Bundle bundle) {
    return profileFile.getName()+"/"+profileFile.getVersion()+"/"+bundle.getReference();
  }
  public static String inferenceCodeWithoutRef(String inferenceCodeWithHashes) {
    if(inferenceCodeWithHashes.length() - inferenceCodeWithHashes.replace("|","").length() == 0) {
      inferenceCodeWithHashes = "|"+inferenceCodeWithHashes;
    }
    if(inferenceCodeWithHashes.length()-1 != inferenceCodeWithHashes.replace("|","").length()) {
      throw new RuntimeException("The inferenceCode does not have the right amount of | separators");
    }
    if(inferenceCodeWithHashes.length()-2 != inferenceCodeWithHashes.replace("/","").length()) {
      throw new RuntimeException("The inferenceCode does not have the right amount of / separators");
    }
    String fingerPrint = inferenceCodeWithHashes.substring(0, inferenceCodeWithHashes.indexOf("|"));
    String inferenceCode = inferenceCodeWithHashes.substring(inferenceCodeWithHashes.indexOf("|")+1);
    return inferenceCode.substring(0, inferenceCode.lastIndexOf("/"));
  }
}
