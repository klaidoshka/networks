const likeEndpointPrefix = 'http://127.0.0.1:23567/api/v1/like';

const onCreateLikeClick = () => {
  $('#createLikeButton').click(() => {
    const userId = prompt("Enter the id of the user:");
    const postId = prompt("Enter the id of the post:");

    if (userId && postId) {
      startLoad();

      $.post(
          `${likeEndpointPrefix}/create`,
          {
            userId,
            postId
          },
          (data) => {
            console.log(data);

            onSuccess('Like created successfully');
          }
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('All fields are required');
    }
  });
}

const onGetAllLikesClick = () => {
  $('#getAllLikesButton').click(() => {
    startLoad();

    $.get(`${likeEndpointPrefix}/all`, (data) => {
      toastr.success('Likes retrieved successfully');

      console.log(data);

      if (data.length === 0) {
        alert('No likes found');

        return;
      }

      let likes = "Likes listed below\n\n";

      data.forEach(like => {
        likes += `Id: ${like.id}\nUser: ${like.userId}\nPost: ${like.postId}\nLiked At: ${like.likedAt}\n\n`;
      });

      alert(likes);
    })
    .fail(onFail)
    .always(endLoad);
  });
}

const onUpdateLikeDateClick = () => {
  $('#updateLikeDateButton').click(() => {
    const id = prompt("Enter the like id:");
    const postId = prompt("Enter the new post id:");

    if (id && postId) {
      startLoad();

      $.post(
          `${likeEndpointPrefix}/update`,
          {
            id,
            postId
          },
          () => onSuccess('Liked post updated successfully')
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('Like id and new post id are required');
    }
  });
}

const onDeleteLikeClick = () => {
  $('#deleteLikeButton').click(() => {
    const id = prompt("Enter the like id to delete:");

    if (id) {
      startLoad();

      $.post(
          `${likeEndpointPrefix}/delete`,
          {id},
          () => onSuccess('Like deleted successfully')
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('Like id is required');
    }
  });
}