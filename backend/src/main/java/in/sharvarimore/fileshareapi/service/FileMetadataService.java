package in.sharvarimore.fileshareapi.service;

import in.sharvarimore.fileshareapi.document.FileMetadataDocument;
import in.sharvarimore.fileshareapi.document.ProfileDocument;
import in.sharvarimore.fileshareapi.dto.FileMetadataDTO;
import in.sharvarimore.fileshareapi.respository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class FileMetadataService {

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final FileMetadataRepository fileMetadataRepository;

    public List<FileMetadataDTO> uploadFile(MultipartFile files[]) throws IOException {
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        List<FileMetadataDocument> savedFiles = new ArrayList<>();

        if (!userCreditsService.hasEnoughCredits(files.length)) {
            throw new RuntimeException("Not enough credits to upload file! Please purchase more credits.");
        }

        Path uploadPath = Paths.get("upload").toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        for (MultipartFile file : files) {
            String fileName = UUID.randomUUID()+"."+ StringUtils.getFilenameExtension(file.getOriginalFilename()); // get extension of given and generate random uuid for that file
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            FileMetadataDocument fileMetadata = FileMetadataDocument.builder()
                    .fileLocation(targetLocation.toString())
                    .name(file.getOriginalFilename())
                    .size(file.getSize())
                    .type(file.getContentType())
                    .clerkId(currentProfile.getClerkId())
                    .isPublic(false)
                    .uploadedAt(LocalDateTime.now())
                    .build();


            userCreditsService.consumeCredit();

            savedFiles.add(fileMetadataRepository.save(fileMetadata));  // reduce 1 credit after each upload
        }

        return savedFiles.stream().map(fileMetadataDocument -> mapToDTO(fileMetadataDocument))
                .collect(Collectors.toList());
    }

    private FileMetadataDTO mapToDTO(FileMetadataDocument fileMetadataDocument) {
        return FileMetadataDTO.builder()
                .id(fileMetadataDocument.getId())
                .fileLocation(fileMetadataDocument.getFileLocation())
                .name(fileMetadataDocument.getName())
                .size(fileMetadataDocument.getSize())
                .type(fileMetadataDocument.getType())
                .clerkId(fileMetadataDocument.getClerkId())
                .isPublic(fileMetadataDocument.isPublic())
                .uploadedAt(fileMetadataDocument.getUploadedAt())
                .build();
    }

    // read all files of current user
    public List<FileMetadataDTO> getFiles() {
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        List<FileMetadataDocument> files = fileMetadataRepository.findByClerkId(currentProfile.getClerkId());
        return files.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public FileMetadataDTO getPublicFile(String id) {
        Optional<FileMetadataDocument> fileOptional = fileMetadataRepository.findById(id);
        // if file is empty or not public
        if(fileOptional.isEmpty() || !fileOptional.get().isPublic()) {
            throw new RuntimeException("Unable to Locate File!");
        }

        FileMetadataDocument document = fileOptional.get();
        return mapToDTO(document);
    }

    public FileMetadataDTO getDownloadableFile(String id) {
        FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File Not Found!"));
        return mapToDTO(file);
    }

    public void deleteFile(String id) {
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File Not Found!"));

            if(!file.getClerkId().equals(currentProfile.getClerkId())) {
                throw new RuntimeException("File Doesn't Belong to Current User!");
            }

            // If file belongs to current user, then delete from storage
            Path filepath = Paths.get(file.getFileLocation());
            Files.deleteIfExists(filepath);

            fileMetadataRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error Deleting the File!");
        }
    }

    // make public file -> private and private file -> public
    public FileMetadataDTO togglePublic(String id) {
        FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File Not Found!"));
        file.setPublic(!file.isPublic());
        fileMetadataRepository.save(file);
        return mapToDTO(file);
    }
}
