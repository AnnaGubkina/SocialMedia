package api.socialmedia.mapper;


import api.socialmedia.dto.request.PostRequestDto;
import api.socialmedia.dto.responce.PostResponseDto;
import api.socialmedia.entity.Post;
import org.mapstruct.Mapper;



@Mapper(componentModel = "spring")
public interface PostMapper {

    Post postRequestDtoToPostEntity(PostRequestDto postRequestDto);

    PostResponseDto postEntityToPostResponseDto(Post post);


}
