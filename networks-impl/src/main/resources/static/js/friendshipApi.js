const friendshipEndpointPrefix = 'http://127.0.0.1:23567/api/v1/friendship';

const onCreateFriendshipClick = () => {
  $('#createFriendshipButton').click(() => {
    const userId1 = prompt("Enter the id of the first user:");
    const userId2 = prompt("Enter the id of the second user:");
    const since = prompt("Enter the friendship date (YYYY-MM-DD):");

    if (userId1 && userId2 && since) {
      startLoad();

      $.post(
          `${friendshipEndpointPrefix}/create`,
          {
            userId1,
            userId2,
            since
          },
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
  });
}

const onGetAllFriendshipsClick = () => {
  $('#getAllFriendshipsButton').click(() => {
    startLoad();

    $.get(`${friendshipEndpointPrefix}/all`, (data) => {
      toastr.success('Friendships retrieved successfully');

      console.log(data);

      if (data.length === 0) {
        alert('No friendships found');

        return;
      }

      let friendshipsList = "Friendships listed below\n\n";

      data.forEach(friendship => {
        friendshipsList += `Id: ${friendship.friendshipId}\nBetween:\n ${friendship.userId1}\n `
            +
            `${friendship.userId2}\nSince: ${friendship.since}\n\n`;
      });

      alert(friendshipsList);
    })
    .fail(onFail)
    .always(endLoad);
  });
}

const onUpdateFriendshipDateClick = () => {
  $('#updateFriendshipDateButton').click(() => {
    const friendshipId = prompt("Enter the friendship id:");
    const newDate = prompt("Enter the new friendship date (YYYY-MM-DD):");

    if (friendshipId && newDate) {
      startLoad();

      $.post(
          `${friendshipEndpointPrefix}/update`,
          {
            friendshipId,
            newDate
          },
          () => onSuccess('Friendship date updated successfully')
      )
      .fail(onFail)
      .always(endLoad);
    } else {
      toastr.error('Friendship id and new date are required');
    }
  });
}

const onDeleteFriendshipClick = () => {
  $('#deleteFriendshipButton').click(() => {
    const friendshipId = prompt("Enter the friendship id to delete:");

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
  });
}