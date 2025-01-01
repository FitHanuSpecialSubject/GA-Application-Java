package org.fit.ssapp.ss.smt.preference;

/**
 * Builder for preference list
 */
public interface PreferenceBuilder {

  /**
   * calculate & get individual preference list by function
   *
   * @param index individual position
   * @return PreferenceList
   */
  PreferenceList getPreferenceListByFunction(int index);

  /**
   * calculate & get individual preference list by default
   *
   * @param index individual position
   * @return PreferenceList
   */
  PreferenceList getPreferenceListByDefault(int index);

  /**
   * Combine all Preference List to a big MF list for overall interacting
   *
   * @return PreferenceListWrapper
   */
  PreferenceListWrapper toListWrapper();

}
