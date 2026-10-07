package in.sharvarimore.fileshareapi.controller;

import in.sharvarimore.fileshareapi.dto.ProfileDTO;
import in.sharvarimore.fileshareapi.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController             // create RESTful web services
@RequiredArgsConstructor

public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody ProfileDTO profileDTO) {

        // Return HTTP status code based on clerk id
        HttpStatus status = profileService.existsByClerkId(profileDTO.getClerkId()) ? HttpStatus.OK : HttpStatus.CREATED;
        ProfileDTO savedProfile = profileService.createProfile(profileDTO);
        return ResponseEntity.status(status).body(savedProfile);
    }

}
