const showFormModal = (title, fields, onSubmit) => {
  $('#formModalTitle').text(title);
  
  const form = $('#formModalForm');
  
  form.empty();
  
  fields.forEach(field => {
    form.append(`
        <div class="form-group">
          <label for="${field.id}">${field.label}</label>
          <input type="${field.type}" class="form-control" id="${field.id}" ${field.required
        ? 'required' : ''}>
        </div>
      `);
  });
  
  $('#formModalSubmit')
    .off('click')
    .on('click', () => {
      const formData = {};
      
      fields.forEach(field => {
        formData[field.id] = $(`#${field.id}`).val();
      });
      
      onSubmit(formData);
      
      $('#formModal').modal('hide');
    });
  
  $('#formModal').modal('show');
}

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

  $('#display')
  .empty()
  .css({
    'width': '',
    'height': ''
  });
}