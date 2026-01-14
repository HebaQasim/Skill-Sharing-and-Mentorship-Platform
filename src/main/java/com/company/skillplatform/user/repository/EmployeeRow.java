package com.company.skillplatform.user.repository;

import java.util.UUID;

public interface EmployeeRow {
    UUID getId();
    String getFirstName();
    String getLastName();
    String getDepartment();
    String getJobTitle();
    String getHeadline();
    String getProfileImageUrl();
}

