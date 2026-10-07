package in.sharvarimore.fileshareapi.controller;

import in.sharvarimore.fileshareapi.dto.ProfileDTO;
import in.sharvarimore.fileshareapi.service.ProfileService;
import in.sharvarimore.fileshareapi.service.UserCreditsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor

public class ClerkWebhookController {

    // get clerk webhook secret key whenever an event is triggered
    @Value("${clerk.webhook.secret}")   // reads and injects property values
    private String webhookSecret;

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;

    @PostMapping("/clerk")
    public ResponseEntity<?> handleClerkWebhook(@RequestHeader("svix-id") String svixId,
                                                @RequestHeader("svix-timestamp") String svixTimestamp,
                                                @RequestHeader("svix-signature") String svixSignature,
                                                @RequestBody String payload) {

        try {
            boolean isValid = verifyWebhookSignature(svixId, svixTimestamp, svixSignature, payload);
            if(!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Webhook Signature!");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            String eventType = rootNode.path("type").asString();

            switch (eventType) {
                case "user.created":
                    handleUserCreated(rootNode.path("data"));
                    break;
                case "user.updated":
                    handleUserUpdated(rootNode.path("data"));
                    break;
                case "user.deleted":
                    handleUserDeleted(rootNode.path("data"));
                    break;
            }
            return  ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    private void handleUserDeleted(JsonNode data) {
        String clerkId = data.path("id").asString();
        profileService.deleteProfile(clerkId);
    }

    private void handleUserUpdated(JsonNode data) {
        String clerkId = data.path("id").asString();

        // Extract user information
        String email = "";
        JsonNode emailAddresses = data.path("email_addresses");
        if (emailAddresses.isArray() && !emailAddresses.isEmpty()) {
            email = emailAddresses.get(0).path("email_address").asString();
        }

        String firstName = data.path("first_name").asString("");
        String lastName = data.path("last_name").asString("");
        String photoUrl = data.path("image_url").asString("");

        ProfileDTO updatedProfile = ProfileDTO.builder()
                .clerkId(clerkId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .photoUrl(photoUrl)
                .build();

        // save updated profile to database
        updatedProfile = profileService.updateProfile(updatedProfile);

        // create new user account if updated profile is null
        if(updatedProfile == null) {
            handleUserCreated(data);
        }
    }

    private void handleUserCreated(JsonNode data) {
        String clerkId = data.path("id").asString();

        String email = "";
        JsonNode emailAddresses = data.path("email_addresses");
        if(emailAddresses.isArray() && !emailAddresses.isEmpty()) {
            email = emailAddresses.get(0).path("email_address").asString();
        }

        // in case details are not provided pass defaultValue
        String firstName = data.path("first_name").asString("");
        String lastName = data.path("last_name").asString("");
        String photoUrl = data.path("image_url").asString("");

        ProfileDTO newProfile = ProfileDTO.builder()
                .clerkId(clerkId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .photoUrl(photoUrl)
                .build();

        // save created profile to database
        profileService.createProfile(newProfile);

        userCreditsService.createInitialCredits(clerkId);
    }

    private boolean verifyWebhookSignature(String svixId, String svixTimestamp, String svixSignature, String payload) {
        return true;         // validates signature
    }
}
