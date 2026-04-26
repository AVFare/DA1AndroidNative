package com.example.da1androidnative.data.model;

import java.util.List;

public class UserProfileResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String profilePhoto;
    private List<String> preferences;
    private Long reservedActivitiesCount;
    private Long completedActivitiesCount;

    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getProfilePhoto() { return profilePhoto; }
    public List<String> getPreferences() { return preferences; }
    public Long getReservedActivitiesCount() { return reservedActivitiesCount; }
    public Long getCompletedActivitiesCount() { return completedActivitiesCount; }
}