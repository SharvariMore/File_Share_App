package in.sharvarimore.fileshareapi.service;

import com.mongodb.DuplicateKeyException;
import in.sharvarimore.fileshareapi.document.ProfileDocument;
import in.sharvarimore.fileshareapi.dto.ProfileDTO;
import in.sharvarimore.fileshareapi.respository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service                    // registers class as a managed Spring bean
@RequiredArgsConstructor    // generates constructors for all final and non-null fields(@NonNull), boilerplate-free constructor-based DI without explicitly writing @Autowired

public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileDTO createProfile(ProfileDTO profileDTO) {

        // Convert DTO to document
        ProfileDocument profile = ProfileDocument.builder()
                .clerkId(profileDTO.getClerkId())
                .email(profileDTO.getEmail())
                .firstName(profileDTO.getFirstName())
                .lastName(profileDTO.getLastName())
                .photoUrl(profileDTO.getPhotoUrl())
                .credits(5)
                .createdAt(Instant.now())
                .build();

        // save to database
        profile = profileRepository.save(profile);

        // return saved profile
        return ProfileDTO.builder()
                .id(profile.getId())
                .clerkId(profile.getClerkId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .photoUrl(profile.getPhotoUrl())
                .credits(profile.getCredits())
                .createdAt(profile.getCreatedAt())
                .build();
    }

    public ProfileDTO updateProfile(ProfileDTO profileDTO) {
        ProfileDocument existingProfile = profileRepository.findByClerkId(profileDTO.getClerkId());

        if (existingProfile != null) {
            if (profileDTO.getEmail() != null && !profileDTO.getEmail().isEmpty()) {
                existingProfile.setEmail(profileDTO.getEmail());
            }

            if (profileDTO.getFirstName() != null && !profileDTO.getFirstName().isEmpty()) {
                existingProfile.setFirstName(profileDTO.getFirstName());
            }

            if (profileDTO.getLastName() != null && !profileDTO.getLastName().isEmpty()) {
                existingProfile.setLastName(profileDTO.getLastName());
            }

            if (profileDTO.getPhotoUrl() != null && !profileDTO.getPhotoUrl().isEmpty()) {
                existingProfile.setPhotoUrl(profileDTO.getPhotoUrl());
            }

            profileRepository.save(existingProfile);

            return ProfileDTO.builder()
                    .id(existingProfile.getId())
                    .email(existingProfile.getEmail())
                    .clerkId(existingProfile.getClerkId())
                    .firstName(existingProfile.getFirstName())
                    .lastName(existingProfile.getLastName())
                    .credits(existingProfile.getCredits())
                    .createdAt(existingProfile.getCreatedAt())
                    .photoUrl(existingProfile.getPhotoUrl())
                    .build();
        }
        return  null;
    }

    // check profile existence based on clerk id
    public boolean existsByClerkId(String clerkId) {
        return profileRepository.existsByClerkId(clerkId);
    }

    public void deleteProfile(String clerkId) {
        ProfileDocument existingProfile = profileRepository.findByClerkId(clerkId);
        if(existingProfile != null) {
            profileRepository.delete(existingProfile);
        }
    }

    public ProfileDocument getCurrentProfile() {
        if(SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new UsernameNotFoundException("User Not Authenticated!");
        }

        // if auth object is present in context
        String clerkId = SecurityContextHolder.getContext().getAuthentication().getName();
        return profileRepository.findByClerkId(clerkId);
    }
}
