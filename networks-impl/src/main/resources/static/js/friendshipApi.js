const friendshipEndpointPrefix = 'http://127.0.0.1:23567/api/v1/friendship';

const onCreateFriendshipClick = () => {
  $('#createFriendshipButton').click(() => {
    showFormModal(
        'Create Friendship',
        [
          {
            id: 'userId1',
            label: 'Enter the id of the first user:',
            type: 'text',
            required: true
          },
          {
            id: 'userId2',
            label: 'Enter the id of the second user:',
            type: 'text',
            required: true
          },
          {
            id: 'since',
            label: 'Enter the friendship date (YYYY-MM-DD):',
            type: 'date',
            required: true
          }
        ],
        (formData) => {
          const {userId1, userId2, since} = formData;

          if (userId1 && userId2 && since) {
            startLoad();

            $.post(
                `${friendshipEndpointPrefix}/create`,
                {userId1, userId2, since},
                (data) => {
                  console.log(data);
                  onSuccess('Friendship created successfully');
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

const onGetAllFriendshipsClick = () => {
  $('#getAllFriendshipsButton').click(() => {
    startLoad();

    $.get(`${friendshipEndpointPrefix}/all`, (data) => {
      const displayDiv = $('#display');

      if (!displayDiv.is(':empty')) {
        onSuccess('Friendships — hidden');
        
        return;
      }

      toastr.success('Friendships — shown');

      console.log(data);

      displayDiv.empty();

      if (data.length === 0) {
        displayDiv.append('<h3>No friendships found</h3>');
        
        return;
      }

      displayDiv.append('<h3>Friendships</h3>');

      const table = $('<table class="table table-striped"></table>');
      const thead = $('<thead><tr><th>Id</th><th>User Id 1</th><th>User Id 2</th><th>Since</th></tr></thead>');
      const tbody = $('<tbody></tbody>');

      data.forEach(friendship => {
        const row = $('<tr></tr>');

        row.append(`<td>${friendship.friendshipId}</td>`);
        row.append(`<td>${friendship.userId1}</td>`);
        row.append(`<td>${friendship.userId2}</td>`);
        row.append(`<td>${friendship.since}</td>`);

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

const onUpdateFriendshipDateClick = () => {
  $('#updateFriendshipDateButton').click(() => {
    showFormModal(
        'Update Friendship Date',
        [
          {
            id: 'friendshipId',
            label: 'Enter the friendship id:',
            type: 'text',
            required: true
          },
          {
            id: 'newDate',
            label: 'Enter the new friendship date:',
            type: 'date',
            required: true
          }
        ],
        (formData) => {
          const {friendshipId, newDate} = formData;

          if (friendshipId && newDate) {
            startLoad();

            $.post(
                `${friendshipEndpointPrefix}/update`,
                {friendshipId, newDate},
                () => onSuccess('Friendship date updated successfully')
            )
            .fail(onFail)
            .always(endLoad);
          } else {
            toastr.error('Friendship id and new date are required');
          }
        }
    );
  });
}

const onDeleteFriendshipClick = () => {
  $('#deleteFriendshipButton').click(() => {
    showFormModal(
        'Delete Friendship',
        [
          {
            id: 'friendshipId',
            label: 'Enter the friendship id to delete:',
            type: 'text',
            required: true
          }
        ],
        (formData) => {
          const {friendshipId} = formData;

          if (friendshipId) {
            startLoad();

            $.post(
                `${friendshipEndpointPrefix}/delete`,
                {friendshipId},
                () => onSuccess('Friendship deleted successfully')
            )
            .fail(onFail)
            .always(endLoad);
          } else {
            toastr.error('Friendship id is required');
          }
        }
    );
  });
}