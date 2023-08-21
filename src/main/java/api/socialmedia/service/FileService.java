package api.socialmedia.service;

import org.springframework.web.bind.annotation.PathVariable;

public interface FileService {

    byte[] downloadFile( Long id);
}
