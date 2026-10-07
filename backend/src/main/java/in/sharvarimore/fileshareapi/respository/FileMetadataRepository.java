package in.sharvarimore.fileshareapi.respository;

import in.sharvarimore.fileshareapi.document.FileMetadataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileMetadataRepository extends MongoRepository<FileMetadataDocument, String> {

    // return list of files by clerkId
    List<FileMetadataDocument> findByClerkId(String clerkId);

    // count how many files were created for each user
    Long countByClerkId(String clerkId);
}
