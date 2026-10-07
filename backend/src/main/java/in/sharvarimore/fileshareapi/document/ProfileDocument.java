package in.sharvarimore.fileshareapi.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@AllArgsConstructor   // constructor with exactly one parameter for every field declared in class
@NoArgsConstructor    // constructor with zero arguments
@Data                // injects @Getter, @Setter, @ToString, @EqualsAndHashCode, and @RequiredArgsConstructor all at once
@Builder             // constructs complex objects step by step without writing messy constructors
@Document(collection = "profiles")  // marks the class as a persistent model/entity mapped to a collection in a document database as MongoDB

public class ProfileDocument {

    @Id
    private String id;
    private String clerkId;
    @Indexed(unique = true)
    private String email;
    private String firstName;
    private String lastName;
    private Integer credits;  // When user logs in first time he gets credits
    private String photoUrl;
    @CreatedDate
    private Instant createdAt; // Track user profile created
}
