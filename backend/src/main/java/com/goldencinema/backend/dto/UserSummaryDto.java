package com.goldencinema.backend.dto;

public class UserSummaryDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean isActive;
    private String role;

    public UserSummaryDto() {}

    public UserSummaryDto(Long id, String firstName, String lastName, String email,
                          String phone, Boolean isActive, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.isActive = isActive;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Boolean getIsActive() { return isActive; }
    public String getRole() { return role; }
}
