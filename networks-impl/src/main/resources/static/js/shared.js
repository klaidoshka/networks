const startLoad = () => {
  $('#loadingModal')
  .modal(
      {
        backdrop: 'static',
        keyboard: false
      }
  )
  .modal('show');
}

const endLoad = () => {
  $('#loadingModal').modal('hide');
}

const onFail = (response) => {
  console.error(response);

  toastr.error(
      `Failed ${response.responseText && `(${response.responseText})`
      || 'to execute. Server error.'}`
  );
}

const onSuccess = (msg) => {
  toastr.success(msg);

  $('#cy-parent').removeClass('bg-light');

  $('#cy')
    .empty()
    .css({
      'width': '',
      'height': ''
    });
}