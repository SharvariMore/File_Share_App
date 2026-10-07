package in.sharvarimore.fileshareapi.service;

import in.sharvarimore.fileshareapi.document.UserCredits;
import in.sharvarimore.fileshareapi.respository.UserCreditsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service                        // annotates classes at application's service layer to handle business logic
@RequiredArgsConstructor

public class UserCreditsService {

    private final UserCreditsRepository userCreditsRepository;
    private final ProfileService profileService;

    public UserCredits createInitialCredits(String clerkId) {
        UserCredits userCredits = UserCredits.builder()
                .clerkId(clerkId)
                .credits(5)
                .plan("BASIC")
                .build();

        return userCreditsRepository.save(userCredits);
    }

    // return Optional or else create initial credits
    public UserCredits getUserCredits(String clerkId) {
        return userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));
    }

    public UserCredits getUserCredits() {
        String clerkId = profileService.getCurrentProfile().getClerkId();
        return getUserCredits(clerkId);
    }

    // check if eligible to upload file based on credits
    public Boolean hasEnoughCredits(int requiredCredits) {
        UserCredits userCredits = getUserCredits();
        return userCredits.getCredits() >= requiredCredits;  // check if user has required credits to upload file
    }

    // return user credits
    public UserCredits consumeCredit() {
        UserCredits userCredits = getUserCredits();

        if(userCredits.getCredits() <= 0) {
            return null;
        }

        // if credits > 0 then return new credits
        userCredits.setCredits(userCredits.getCredits() - 1); // for each upload reduce credit by 1
        return userCreditsRepository.save(userCredits);      // return new credits
    }

    public UserCredits addCredits(String clerkId, Integer creditsToAdd, String plan) {
        UserCredits userCredits = userCreditsRepository.findByClerkId(clerkId)
                .orElseGet(() -> createInitialCredits(clerkId));

        // add new credits to existing credits
        userCredits.setCredits(userCredits.getCredits() + creditsToAdd);
        userCredits.setPlan(plan);                                           // create a new plan
        return userCreditsRepository.save(userCredits);
    }
}
