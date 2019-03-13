/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.analysis.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class FileList {
    private int formatVersion = FileListVersion.LATEST_VERSION;

    private List<BaseListedFile> filterFiles = List.of();

    public int getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(final int formatVersion) {
        this.formatVersion = formatVersion;
    }

    public List<BaseListedFile> getFilterFiles() {
        return List.copyOf(filterFiles);
    }

    public void setFilterFiles(final List<BaseListedFile> filterFiles) {
        this.filterFiles = List.copyOf(filterFiles);
    }
}
