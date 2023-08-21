package api.socialmedia.exception;

public class DuplicateFriendshipRequestException extends RuntimeException {

    public DuplicateFriendshipRequestException(String message) {
        super(message);
    }

}
