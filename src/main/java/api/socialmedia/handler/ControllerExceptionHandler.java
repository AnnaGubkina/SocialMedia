package api.socialmedia.handler;



import api.socialmedia.dto.responce.ExceptionWebResponse;
import api.socialmedia.exception.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(InputDataException.class)
    public ResponseEntity<ExceptionWebResponse> handleInputDataException(@NonNull final InputDataException exc) {
        log.error(exc.getMessage());
        return new ResponseEntity<>(new ExceptionWebResponse(exc.getMessage(),400), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionWebResponse> handleUnauthorizedException(@NonNull final UnauthorizedException exc) {
        log.error(exc.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionWebResponse(exc.getMessage(), 401));

    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ExceptionWebResponse> handlePostNotFoundException(@NonNull final PostNotFoundException exc) {
        log.error(exc.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionWebResponse(exc.getMessage(), 404));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionWebResponse> handleAccessDeniedException(@NonNull final AccessDeniedException exc) {
        log.error(exc.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ExceptionWebResponse(exc.getMessage(), 403));

    }

    @ExceptionHandler(DuplicateFriendshipRequestException.class)
    public ResponseEntity<ExceptionWebResponse> handleDuplicateFriendshipRequestException(@NonNull final DuplicateFriendshipRequestException exc) {
        log.error(exc.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ExceptionWebResponse(exc.getMessage(), 403));

    }

    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<ExceptionWebResponse> handleRequestNotFoundException(@NonNull final RequestNotFoundException exc) {
        log.error(exc.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionWebResponse(exc.getMessage(), 404));
    }

    @ExceptionHandler(FriendshipNotFoundException.class)
    public ResponseEntity<ExceptionWebResponse> handleFriendshipNotFoundException(@NonNull final FriendshipNotFoundException exc) {
        log.error(exc.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionWebResponse(exc.getMessage(), 404));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionWebResponse> handleUserNotFoundException(@NonNull final UserNotFoundException exc) {
        log.error(exc.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionWebResponse(exc.getMessage(), 404));
    }
}
