package api.socialmedia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegRequest {


    @NotBlank(message = "login must not be null")
    private String login;

    @NotBlank(message = "password must not be null")
    @Size(min = 2,max = 30, message = "Password should be between 2 and 30 characters")
    private String password;

    @NotBlank(message = "email must not be null")
    @Size(min = 7,max = 30, message = "Password should be between 2 and 30 characters")
    private String email;
}
