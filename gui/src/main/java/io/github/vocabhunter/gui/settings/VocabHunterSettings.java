/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabHunterSettings {
    public static final int DEFAULT_MINIMUM_LETTERS = 2;

    public static final int DEFAULT_MINIMUM_OCCURRENCES = 2;

    public static final boolean DEFAULT_ALLOW_INITIAL_CAPITALS = true;

    private String documentsPath;

    private String sessionsPath;

    private String exportPath;

    private int filterMinimumLetters = DEFAULT_MINIMUM_LETTERS;

    private int filterMinimumOccurrences = DEFAULT_MINIMUM_OCCURRENCES;

    private boolean isAllowInitialCaptials = DEFAULT_ALLOW_INITIAL_CAPITALS;

    public String getDocumentsPath() {
        return documentsPath;
    }

    public void setDocumentsPath(final String documentsPath) {
        this.documentsPath = documentsPath;
    }

    public String getSessionsPath() {
        return sessionsPath;
    }

    public void setSessionsPath(final String sessionsPath) {
        this.sessionsPath = sessionsPath;
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(final String exportPath) {
        this.exportPath = exportPath;
    }

    public int getFilterMinimumLetters() {
        return filterMinimumLetters;
    }

    public void setFilterMinimumLetters(final int filterMinimumLetters) {
        this.filterMinimumLetters = filterMinimumLetters;
    }

    public int getFilterMinimumOccurrences() {
        return filterMinimumOccurrences;
    }

    public void setFilterMinimumOccurrences(final int filterMinimumOccurrences) {
        this.filterMinimumOccurrences = filterMinimumOccurrences;
    }

    public boolean isAllowInitialCaptials() {
        return isAllowInitialCaptials;
    }

    public void setAllowInitialCaptials(final boolean allowInitialCaptials) {
        isAllowInitialCaptials = allowInitialCaptials;
    }
}
