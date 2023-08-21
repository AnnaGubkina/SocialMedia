package api.socialmedia.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for post
 */
@Setter
@Getter
@AllArgsConstructor
public class PostRequestDto {

    @JsonProperty("title")
    @NotBlank(message = "name is required field")
    private String title;

    @JsonProperty("text")
    @Size(min = 10, max = 2000, message = "text should be between 10 and 2000 characters")
    private String text;

}
