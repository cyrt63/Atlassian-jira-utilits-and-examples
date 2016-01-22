package com.atlassian.pocketknife.internal.version;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.pocketknife.api.version.JiraVersionService;
import com.atlassian.pocketknife.api.version.SoftwareVersion;
import com.atlassian.pocketknife.api.version.VersionKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to fetch the current JIRA version
 */
@Service
public class JiraVersionServiceImpl implements JiraVersionService {
    private final SoftwareVersion jiraVersion;

    @Autowired
    public JiraVersionServiceImpl(BuildUtilsInfo buildUtilsInfo) {
        String versionString = buildUtilsInfo.getVersion();
        jiraVersion = VersionKit.parse(versionString);
    }

    @Override
    public SoftwareVersion version() {
        return jiraVersion;
    }
}
