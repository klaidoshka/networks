const likeEndpointPrefix = 'http://127.0.0.1:23567/api/v1/like';

const onCreateLikeClick = () => {
  $('#createLikeButton').click(() => {
    showFormModal(
        'Create Like',
        [
          {
            id: 'userId',
            label: 'Enter the id of the user:',
            type: 'text',
            required: true
          },
          {
            id: 'postId',
            label: 'Enter the id of the post:',
            type: 'text',
            required: true
          }
        ],
        (formData) => {
          const {userId, postId} = formData;

          if (userId && postId) {
            startLoad();

            $.post(
                `${likeEndpointPrefix}/create`,
                {userId, postId},
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
        }
    );
  });
}

const onGetAllLikesClick = () => {
  $('#getAllLikesButton').click(() => {
    startLoad();

    $.get(`${likeEndpointPrefix}/all`, (data) => {
      const displayDiv = $('#display');

      if (!displayDiv.is(':empty')) {
        onSuccess('Likes — hidden');

        return;
      }

      toastr.success('Likes — shown');

      console.log(data);

      displayDiv.empty();

      if (data.length === 0) {
        displayDiv.append('<h3>No likes found</h3>');

        return;
      }

      displayDiv.append('<h3>Likes</h3>');

      const table = $('<table class="table table-striped"></table>');
      const thead = $('<thead><tr><th>Id</th><th>User Id</th><th>Post Id</th><th>Liked At</th></tr></thead>');
      const tbody = $('<tbody></tbody>');

      data.forEach(like => {
        const row = $('<tr></tr>');

        row.append(`<td>${like.id}</td>`);
        row.append(`<td>${like.userId}</td>`);
        row.append(`<td>${like.postId}</td>`);
        row.append(`<td>${like.likedAt}</td>`);

        tbody.append(row);
      });

      table
      .append(thead)
      .append(tbody)
      .appendTo(displayDiv);

      displayDiv.css({
        'max-height': '400px',
        'overflow-y': 'auto'
      });
    })
    .fail(onFail)
    .always(endLoad);
  });
}

const onUpdateLikeDateClick = () => {
  $('#updateLikeDateButton').click(() => {
    showFormModal(
        'Update Like Post',
        [
          {
            id: 'id',
            label: 'Enter the like id:',
            type: 'text',
            required: true
          },
          {
            id: 'postId',
            label: 'Enter the new post id:',
            type: 'text',
            required: true
          }
        ],
        (formData) => {
          const {id, postId} = formData;

          if (id && postId) {
            startLoad();

            $.post(
                `${likeEndpointPrefix}/update`,
                {id, postId},
                () => onSuccess('Liked post updated successfully')
            )
            .fail(onFail)
            .always(endLoad);
          } else {
            toastr.error('Like id and new post id are required');
          }
        }
    );
  });
}

const onDeleteLikeClick = () => {
  $('#deleteLikeButton').click(() => {
    showFormModal(
        'Delete Like',
        [
          {
            id: 'id',
            label: 'Enter the like id to delete:',
            type: 'text',
            required: true
          }
        ],
        (formData) => {
          const {id} = formData;

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
        }
    );
  });
}