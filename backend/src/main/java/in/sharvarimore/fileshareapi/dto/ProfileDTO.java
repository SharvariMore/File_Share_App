package in.sharvarimore.fileshareapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ProfileDTO {

    private String id;
    private String clerkId;
    private String email;
    private String firstName;
    private String lastName;
    private Integer credits;  // When user logs in first time he gets credits
    private String photoUrl;
    private Instant createdAt; // Track user profile created
}
